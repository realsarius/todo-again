package yapilacaklarListesi;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import yapilacaklarListesi.veriler.Oncelik;
import yapilacaklarListesi.veriler.Yapilacak;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EisenhowerController {

    private static final String DRAG_TARGET_CLASS = "eisenhower-drag-target";

    private final ListView<Yapilacak> q1ListView;
    private final ListView<Yapilacak> q2ListView;
    private final ListView<Yapilacak> q3ListView;
    private final ListView<Yapilacak> q4ListView;
    private final Label q1CountBadge;
    private final Label q2CountBadge;
    private final Label q3CountBadge;
    private final Label q4CountBadge;

    private ObservableList<Yapilacak> tumGorevler = FXCollections.observableArrayList();
    private Consumer<Yapilacak> secimHandler = gorev -> { };
    private Consumer<Yapilacak> guncellemeHandler = gorev -> { };

    public EisenhowerController(ListView<Yapilacak> q1ListView,
                                ListView<Yapilacak> q2ListView,
                                ListView<Yapilacak> q3ListView,
                                ListView<Yapilacak> q4ListView,
                                Label q1CountBadge,
                                Label q2CountBadge,
                                Label q3CountBadge,
                                Label q4CountBadge) {
        this.q1ListView = Objects.requireNonNull(q1ListView, "q1ListView");
        this.q2ListView = Objects.requireNonNull(q2ListView, "q2ListView");
        this.q3ListView = Objects.requireNonNull(q3ListView, "q3ListView");
        this.q4ListView = Objects.requireNonNull(q4ListView, "q4ListView");
        this.q1CountBadge = Objects.requireNonNull(q1CountBadge, "q1CountBadge");
        this.q2CountBadge = Objects.requireNonNull(q2CountBadge, "q2CountBadge");
        this.q3CountBadge = Objects.requireNonNull(q3CountBadge, "q3CountBadge");
        this.q4CountBadge = Objects.requireNonNull(q4CountBadge, "q4CountBadge");
    }

    public void initialize(ObservableList<Yapilacak> kaynak,
                           Consumer<Yapilacak> secimHandler,
                           Consumer<Yapilacak> guncellemeHandler) {
        this.tumGorevler = Objects.requireNonNull(kaynak, "kaynak");
        this.secimHandler = secimHandler == null ? gorev -> { } : secimHandler;
        this.guncellemeHandler = guncellemeHandler == null ? gorev -> { } : guncellemeHandler;

        configureQuadrant(q1ListView, q1CountBadge, Quadrant.Q1, this::q1Predicate);
        configureQuadrant(q2ListView, q2CountBadge, Quadrant.Q2, this::q2Predicate);
        configureQuadrant(q3ListView, q3CountBadge, Quadrant.Q3, this::q3Predicate);
        configureQuadrant(q4ListView, q4CountBadge, Quadrant.Q4, this::q4Predicate);

        tumGorevler.addListener((ListChangeListener<Yapilacak>) change -> guncelleBadgeSayilari());
        guncelleBadgeSayilari();
    }

    public void goreviSec(Yapilacak gorev) {
        if (gorev == null) {
            q1ListView.getSelectionModel().clearSelection();
            q2ListView.getSelectionModel().clearSelection();
            q3ListView.getSelectionModel().clearSelection();
            q4ListView.getSelectionModel().clearSelection();
            return;
        }

        Quadrant quadrant = gorevIcinKadran(gorev);
        q1ListView.getSelectionModel().clearSelection();
        q2ListView.getSelectionModel().clearSelection();
        q3ListView.getSelectionModel().clearSelection();
        q4ListView.getSelectionModel().clearSelection();
        switch (quadrant) {
            case Q1 -> q1ListView.getSelectionModel().select(gorev);
            case Q2 -> q2ListView.getSelectionModel().select(gorev);
            case Q3 -> q3ListView.getSelectionModel().select(gorev);
            case Q4 -> q4ListView.getSelectionModel().select(gorev);
        }
    }

    private void configureQuadrant(ListView<Yapilacak> listView,
                                   Label countBadge,
                                   Quadrant targetQuadrant,
                                   Predicate<Yapilacak> predicate) {
        FilteredList<Yapilacak> filtered = new FilteredList<>(tumGorevler, predicate);
        SortedList<Yapilacak> sorted = new SortedList<>(filtered, gorevComparator());
        listView.setItems(sorted);
        listView.setCellFactory(view -> createTaskCell(targetQuadrant));
        listView.getSelectionModel().selectedItemProperty().addListener((obs, eski, yeni) -> {
            if (yeni != null) {
                secimHandler.accept(yeni);
            }
        });
        configureDropTarget(listView, targetQuadrant);
        filtered.addListener((ListChangeListener<Yapilacak>) change -> countBadge.setText(String.valueOf(filtered.size())));
        countBadge.setText(String.valueOf(filtered.size()));
    }

    private ListCell<Yapilacak> createTaskCell(Quadrant quadrant) {
        return new ListCell<>() {
            private final Circle oncelikNoktasi = new Circle(4.0);
            private final Label gorevBasligi = new Label();
            private final HBox satir = new HBox(8.0, oncelikNoktasi, gorevBasligi);

            {
                satir.setAlignment(Pos.CENTER_LEFT);
                satir.getStyleClass().add("eisenhower-item-box");
                gorevBasligi.getStyleClass().add("eisenhower-item-text");
                gorevBasligi.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(gorevBasligi, javafx.scene.layout.Priority.ALWAYS);
                setOnDragDetected(event -> {
                    Yapilacak gorev = getItem();
                    if (gorev == null) {
                        return;
                    }
                    Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(gorev.getId());
                    dragboard.setContent(content);
                    event.consume();
                });
                setOnMouseClicked(event -> {
                    Yapilacak gorev = getItem();
                    if (gorev != null) {
                        secimHandler.accept(gorev);
                    }
                });
            }

            @Override
            protected void updateItem(Yapilacak item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                oncelikNoktasi.setFill(oncelikRengi(item.getOncelik()));
                gorevBasligi.setText(item.getAciklama());
                setText(null);
                setGraphic(satir);
            }
        };
    }

    private void configureDropTarget(ListView<Yapilacak> listView, Quadrant quadrant) {
        listView.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (!db.hasString()) {
                return;
            }
            Yapilacak suruklenen = goreviBul(db.getString());
            if (suruklenen == null || gorevIcinKadran(suruklenen) == quadrant) {
                return;
            }
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });

        listView.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                Yapilacak suruklenen = goreviBul(db.getString());
                if (suruklenen != null && gorevIcinKadran(suruklenen) != quadrant
                        && !listView.getStyleClass().contains(DRAG_TARGET_CLASS)) {
                    listView.getStyleClass().add(DRAG_TARGET_CLASS);
                }
            }
        });

        listView.setOnDragExited(event -> listView.getStyleClass().remove(DRAG_TARGET_CLASS));

        listView.setOnDragDropped(event -> {
            boolean completed = false;
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                Yapilacak suruklenen = goreviBul(db.getString());
                if (suruklenen != null) {
                    goreviKadranaTasi(suruklenen, quadrant);
                    guncellemeHandler.accept(suruklenen);
                    completed = true;
                }
            }
            listView.getStyleClass().remove(DRAG_TARGET_CLASS);
            event.setDropCompleted(completed);
            event.consume();
        });
    }

    private void goreviKadranaTasi(Yapilacak gorev, Quadrant hedef) {
        gorev.setUrgent(hedef.urgent);
        if (hedef.important) {
            if (gorev.getOncelik() == Oncelik.LOW) {
                gorev.setOncelik(Oncelik.MEDIUM);
            }
        } else {
            gorev.setOncelik(Oncelik.LOW);
        }
    }

    private Yapilacak goreviBul(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (Yapilacak gorev : tumGorevler) {
            if (id.equals(gorev.getId())) {
                return gorev;
            }
        }
        return null;
    }

    private Comparator<Yapilacak> gorevComparator() {
        return Comparator
                .comparing(Yapilacak::getTarih, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(gorev -> gorev.isAllDay() ? LocalTime.MIN : gorev.getStartTime(),
                        Comparator.nullsLast(LocalTime::compareTo))
                .thenComparing(Yapilacak::getAciklama, String.CASE_INSENSITIVE_ORDER);
    }

    private boolean q1Predicate(Yapilacak gorev) {
        return onemliMi(gorev) && gorev.isUrgent();
    }

    private boolean q2Predicate(Yapilacak gorev) {
        return onemliMi(gorev) && !gorev.isUrgent();
    }

    private boolean q3Predicate(Yapilacak gorev) {
        return !onemliMi(gorev) && gorev.isUrgent();
    }

    private boolean q4Predicate(Yapilacak gorev) {
        return !onemliMi(gorev) && !gorev.isUrgent();
    }

    private boolean onemliMi(Yapilacak gorev) {
        return gorev.getOncelik() != Oncelik.LOW;
    }

    private Quadrant gorevIcinKadran(Yapilacak gorev) {
        if (onemliMi(gorev) && gorev.isUrgent()) {
            return Quadrant.Q1;
        }
        if (onemliMi(gorev)) {
            return Quadrant.Q2;
        }
        if (gorev.isUrgent()) {
            return Quadrant.Q3;
        }
        return Quadrant.Q4;
    }

    private void guncelleBadgeSayilari() {
        q1CountBadge.setText(String.valueOf(q1ListView.getItems().size()));
        q2CountBadge.setText(String.valueOf(q2ListView.getItems().size()));
        q3CountBadge.setText(String.valueOf(q3ListView.getItems().size()));
        q4CountBadge.setText(String.valueOf(q4ListView.getItems().size()));
    }

    private Color oncelikRengi(Oncelik oncelik) {
        return switch (oncelik) {
            case HIGH -> Color.web("#FF3B30");
            case MEDIUM -> Color.web("#4A7CFF");
            case LOW -> Color.web("#34C759");
        };
    }

    private enum Quadrant {
        Q1(true, true),
        Q2(true, false),
        Q3(false, true),
        Q4(false, false);

        private final boolean important;
        private final boolean urgent;

        Quadrant(boolean important, boolean urgent) {
            this.important = important;
            this.urgent = urgent;
        }
    }
}

package yapilacaklarListesi;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import yapilacaklarListesi.service.TaskService;
import yapilacaklarListesi.veriler.Oncelik;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;

import java.io.IOException;
import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CalendarController {

    private static final Locale TR_LOCALE = Locale.forLanguageTag("tr-TR");
    private static final DateTimeFormatter AY_BASLIK_FORMATI = DateTimeFormatter.ofPattern("LLLL yyyy", TR_LOCALE);
    private static final DateTimeFormatter HAFTA_BASLIK_FORMATI = DateTimeFormatter.ofPattern("d MMM", TR_LOCALE);
    private static final DateTimeFormatter GUN_BASLIK_FORMATI = DateTimeFormatter.ofPattern("EEE d", TR_LOCALE);
    private static final DateTimeFormatter SAAT_FORMATI = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private VBox ayGorunumuKutu;
    @FXML private VBox haftaGorunumuKutu;
    @FXML private GridPane ayGunBaslikGrid;
    @FXML private GridPane ayTakvimGrid;
    @FXML private GridPane haftaGunBaslikGrid;
    @FXML private GridPane haftaTakvimGrid;
    @FXML private Button donemBaslikButton;
    @FXML private ToggleButton ayGorunumuButton;
    @FXML private ToggleButton haftaGorunumuButton;

    @FXML private VBox detayBosDurumBox;
    @FXML private VBox detayEditorBox;
    @FXML private TextField detayBaslikField;
    @FXML private DatePicker detayTarihPicker;
    @FXML private CheckBox tumGunCheckBox;
    @FXML private ComboBox<String> baslangicSaatComboBox;
    @FXML private ComboBox<String> bitisSaatComboBox;
    @FXML private ToggleButton oncelikDusukButton;
    @FXML private ToggleButton oncelikOrtaButton;
    @FXML private ToggleButton oncelikYuksekButton;
    @FXML private TextArea detayNotArea;

    private final TaskService taskService = new TaskService(YapilacakVeri.getInstance());
    private final ToggleGroup gorunumToggleGroup = new ToggleGroup();
    private final ToggleGroup oncelikToggleGroup = new ToggleGroup();
    private LocalDate aktifAy = LocalDate.now().withDayOfMonth(1);
    private LocalDate aktifHafta = LocalDate.now();
    private Yapilacak seciliGorev;
    private boolean detayGuncelleniyor;

    @FXML
    public void initialize() {
        gorunumToggleGroup.getToggles().addAll(ayGorunumuButton, haftaGorunumuButton);
        ayGorunumuButton.setSelected(true);

        oncelikDusukButton.setToggleGroup(oncelikToggleGroup);
        oncelikOrtaButton.setToggleGroup(oncelikToggleGroup);
        oncelikYuksekButton.setToggleGroup(oncelikToggleGroup);

        oncelikDusukButton.setUserData(Oncelik.LOW);
        oncelikOrtaButton.setUserData(Oncelik.MEDIUM);
        oncelikYuksekButton.setUserData(Oncelik.HIGH);
        saatSecenekleriniHazirla();

        detayBaslikField.textProperty().addListener((obs, eski, yeni) -> seciliBasligiGuncelle(yeni));
        detayTarihPicker.valueProperty().addListener((obs, eski, yeni) -> seciliTarihiGuncelle(yeni));
        tumGunCheckBox.selectedProperty().addListener((obs, eski, yeni) -> seciliTumGunDurumunuGuncelle(yeni));
        baslangicSaatComboBox.valueProperty().addListener((obs, eski, yeni) -> seciliBaslangicSaatiniGuncelle(yeni));
        bitisSaatComboBox.valueProperty().addListener((obs, eski, yeni) -> seciliBitisSaatiniGuncelle(yeni));
        detayNotArea.textProperty().addListener((obs, eski, yeni) -> seciliNotuGuncelle(yeni));
        oncelikToggleGroup.selectedToggleProperty().addListener((obs, eski, yeni) -> seciliOnceligiGuncelle());

        taskService.tumGorevler().addListener((ListChangeListener<Yapilacak>) change -> {
            boolean secimHalaVar = seciliGorev == null || taskService.tumGorevler().contains(seciliGorev);
            if (!secimHalaVar) {
                gorevSec(null);
            }
            takvimiYenile();
        });

        gorevSec(null);
        takvimiYenile();
    }

    private void saatSecenekleriniHazirla() {
        List<String> saatSecenekleri = new ArrayList<>();
        for (int saat = 0; saat < 24; saat++) {
            for (int dakika = 0; dakika < 60; dakika += 15) {
                saatSecenekleri.add(String.format("%02d:%02d", saat, dakika));
            }
        }
        baslangicSaatComboBox.getItems().setAll(saatSecenekleri);
        bitisSaatComboBox.getItems().setAll(saatSecenekleri);
        baslangicSaatComboBox.setEditable(false);
        bitisSaatComboBox.setEditable(false);
    }

    @FXML
    public void ayGorunumuSec() {
        ayGorunumuButton.setSelected(true);
        takvimiYenile();
    }

    @FXML
    public void haftaGorunumuSec() {
        haftaGorunumuButton.setSelected(true);
        takvimiYenile();
    }

    @FXML
    public void oncekiPeriyot() {
        if (haftaGorunumuButton.isSelected()) {
            aktifHafta = aktifHafta.minusWeeks(1);
        } else {
            aktifAy = aktifAy.minusMonths(1);
        }
        takvimiYenile();
    }

    @FXML
    public void sonrakiPeriyot() {
        if (haftaGorunumuButton.isSelected()) {
            aktifHafta = aktifHafta.plusWeeks(1);
        } else {
            aktifAy = aktifAy.plusMonths(1);
        }
        takvimiYenile();
    }

    @FXML
    public void yilSeciciyiAc() {
        LocalDate referans = haftaGorunumuButton.isSelected() ? aktifHafta : aktifAy;
        TextInputDialog dialog = new TextInputDialog(String.valueOf(referans.getYear()));
        dialog.setTitle("Yıl Seç");
        dialog.setHeaderText("Yıl girin");
        dialog.setContentText("Yıl:");

        Optional<String> sonuc = dialog.showAndWait();
        if (sonuc.isEmpty()) {
            return;
        }

        try {
            int yil = Integer.parseInt(sonuc.get().trim());
            if (yil < 1900 || yil > 2200) {
                throw new NumberFormatException();
            }
            if (haftaGorunumuButton.isSelected()) {
                aktifHafta = aktifHafta.withYear(yil);
            } else {
                aktifAy = aktifAy.withYear(yil);
            }
            takvimiYenile();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Geçersiz yıl");
            alert.setHeaderText("Yıl okunamadı");
            alert.setContentText("Lütfen 1900 ile 2200 arasında bir yıl girin.");
            alert.showAndWait();
        }
    }

    private void takvimiYenile() {
        boolean ayModu = ayGorunumuButton.isSelected();
        ayGorunumuKutu.setVisible(ayModu);
        ayGorunumuKutu.setManaged(ayModu);
        haftaGorunumuKutu.setVisible(!ayModu);
        haftaGorunumuKutu.setManaged(!ayModu);

        donemBasliginiGuncelle(ayModu);
        if (ayModu) {
            aylikGorunumuCiz();
        } else {
            haftalikGorunumuCiz();
        }
    }

    private void donemBasliginiGuncelle(boolean ayModu) {
        if (ayModu) {
            String baslik = aktifAy.format(AY_BASLIK_FORMATI);
            donemBaslikButton.setText(StringUtils.capitalize(baslik));
            return;
        }

        LocalDate haftaBaslangic = aktifHafta.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate haftaBitis = haftaBaslangic.plusDays(6);
        String baslik = haftaBaslangic.format(HAFTA_BASLIK_FORMATI)
                + " - "
                + haftaBitis.format(DateTimeFormatter.ofPattern("d MMM yyyy", TR_LOCALE));
        donemBaslikButton.setText(StringUtils.capitalize(baslik));
    }

    private void aylikGorunumuCiz() {
        ayGunBaslikGrid.getChildren().clear();
        ayGunBaslikGrid.getColumnConstraints().clear();
        ayTakvimGrid.getChildren().clear();
        ayTakvimGrid.getColumnConstraints().clear();
        ayTakvimGrid.getRowConstraints().clear();

        List<String> gunAdlari = List.of("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz");
        for (int col = 0; col < 7; col++) {
            ColumnConstraints sutun = new ColumnConstraints();
            sutun.setPercentWidth(100.0 / 7.0);
            sutun.setHgrow(Priority.ALWAYS);
            ayGunBaslikGrid.getColumnConstraints().add(sutun);
            ayTakvimGrid.getColumnConstraints().add(sutunKopyasi());

            Label baslik = new Label(gunAdlari.get(col));
            baslik.getStyleClass().add("calendar-day-header");
            GridPane.setHalignment(baslik, HPos.CENTER);
            ayGunBaslikGrid.add(baslik, col, 0);
        }

        for (int row = 0; row < 6; row++) {
            RowConstraints satir = new RowConstraints();
            satir.setVgrow(Priority.ALWAYS);
            satir.setMinHeight(80);
            ayTakvimGrid.getRowConstraints().add(satir);
        }

        LocalDate ayBaslangici = aktifAy.withDayOfMonth(1);
        LocalDate ilkHucre = ayBaslangici.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                LocalDate gun = ilkHucre.plusDays((long) row * 7 + col);
                VBox hucre = aylikGunHucresiOlustur(gun, gun.getMonthValue() == aktifAy.getMonthValue());
                ayTakvimGrid.add(hucre, col, row);
            }
        }
    }

    private VBox aylikGunHucresiOlustur(LocalDate gun, boolean aktifAyda) {
        VBox hucre = new VBox(5);
        hucre.getStyleClass().addAll("calendar-cell", "calendar-month-cell");
        hucre.setPadding(new Insets(6, 6, 6, 6));

        HBox ustSatir = new HBox();
        ustSatir.setAlignment(Pos.CENTER_LEFT);

        Label gunLabel = new Label(String.valueOf(gun.getDayOfMonth()));
        if (gun.equals(LocalDate.now())) {
            gunLabel.getStyleClass().add("calendar-today-badge");
        } else {
            gunLabel.getStyleClass().add("calendar-day-number");
            if (!aktifAyda) {
                gunLabel.getStyleClass().add("calendar-day-number-muted");
            }
        }
        ustSatir.getChildren().add(gunLabel);

        hucre.getChildren().add(ustSatir);

        List<Yapilacak> gunGorevleri = gununGorevleri(gun);
        int limit = Math.min(gunGorevleri.size(), 3);
        for (int i = 0; i < limit; i++) {
            hucre.getChildren().add(gorevPillOlustur(gunGorevleri.get(i), false));
        }

        if (gunGorevleri.size() > 3) {
            Label dahaFazla = new Label("+" + (gunGorevleri.size() - 3) + " daha");
            dahaFazla.getStyleClass().add("calendar-more-label");
            dahaFazla.setOnMouseClicked(event -> {
                gorevSec(gunGorevleri.get(0));
                event.consume();
            });
            hucre.getChildren().add(dahaFazla);
        }

        hucre.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !event.isConsumed()) {
                yeniGorevDialoguAc(gun, null);
            }
        });

        dropHedefiBagla(hucre, gun);
        return hucre;
    }

    private void haftalikGorunumuCiz() {
        haftaGunBaslikGrid.getChildren().clear();
        haftaGunBaslikGrid.getColumnConstraints().clear();
        haftaTakvimGrid.getChildren().clear();
        haftaTakvimGrid.getColumnConstraints().clear();
        haftaTakvimGrid.getRowConstraints().clear();

        LocalDate haftaBaslangic = aktifHafta.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        ColumnConstraints zamanSutunu = new ColumnConstraints();
        zamanSutunu.setPrefWidth(58);
        zamanSutunu.setMinWidth(58);
        haftaGunBaslikGrid.getColumnConstraints().add(zamanSutunu);
        haftaTakvimGrid.getColumnConstraints().add(sabitSutunKopyasi(58));

        Label bosHeader = new Label(" ");
        bosHeader.getStyleClass().add("calendar-week-header-corner");
        haftaGunBaslikGrid.add(bosHeader, 0, 0);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints sutun = new ColumnConstraints();
            sutun.setPercentWidth(100.0 / 7.0);
            sutun.setHgrow(Priority.ALWAYS);
            haftaGunBaslikGrid.getColumnConstraints().add(sutun);
            haftaTakvimGrid.getColumnConstraints().add(sutunKopyasi());

            LocalDate gun = haftaBaslangic.plusDays(i);
            Label header = new Label(StringUtils.capitalize(gun.format(GUN_BASLIK_FORMATI)));
            header.getStyleClass().add("calendar-week-header");
            if (gun.equals(LocalDate.now())) {
                header.getStyleClass().add("calendar-week-header-today");
            }
            GridPane.setHalignment(header, HPos.CENTER);
            haftaGunBaslikGrid.add(header, i + 1, 0);
        }

        RowConstraints tumGunSatiri = new RowConstraints();
        tumGunSatiri.setMinHeight(64);
        tumGunSatiri.setVgrow(Priority.NEVER);
        haftaTakvimGrid.getRowConstraints().add(tumGunSatiri);

        Label tumGunLabel = new Label("Tüm gün");
        tumGunLabel.getStyleClass().add("calendar-time-label");
        haftaTakvimGrid.add(tumGunLabel, 0, 0);

        for (int saat = 8; saat <= 22; saat++) {
            RowConstraints saatSatiri = new RowConstraints();
            saatSatiri.setMinHeight(44);
            saatSatiri.setVgrow(Priority.ALWAYS);
            haftaTakvimGrid.getRowConstraints().add(saatSatiri);

            Label saatLabel = new Label(String.format("%02d:00", saat));
            saatLabel.getStyleClass().add("calendar-time-label");
            haftaTakvimGrid.add(saatLabel, 0, saat - 7);
        }

        for (int gunIndex = 0; gunIndex < 7; gunIndex++) {
            LocalDate gun = haftaBaslangic.plusDays(gunIndex);

            VBox tumGunKutusu = new VBox(4);
            tumGunKutusu.getStyleClass().addAll("calendar-cell", "calendar-all-day-cell");
            tumGunKutusu.setPadding(new Insets(4));
            List<Yapilacak> tumGunGorevleri = gununGorevleri(gun).stream()
                    .filter(Yapilacak::isAllDay)
                    .toList();
            gorevPilleriEkle(tumGunKutusu, tumGunGorevleri, true);
            tumGunKutusu.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && !event.isConsumed()) {
                    yeniGorevDialoguAc(gun, null);
                }
            });
            dropHedefiBagla(tumGunKutusu, gun);
            haftaTakvimGrid.add(tumGunKutusu, gunIndex + 1, 0);

            for (int saat = 8; saat <= 22; saat++) {
                final int saatDegeri = saat;
                StackPane saatKutusu = new StackPane();
                saatKutusu.getStyleClass().addAll("calendar-cell", "calendar-week-cell");
                if (gun.equals(LocalDate.now())) {
                    saatKutusu.getStyleClass().add("calendar-week-today-cell");
                }
                List<Yapilacak> saatGorevleri = gununGorevleri(gun).stream()
                        .filter(gorev -> !gorev.isAllDay()
                                && gorev.getStartTime() != null
                                && gorev.getStartTime().getHour() == saatDegeri)
                        .toList();
                if (!saatGorevleri.isEmpty()) {
                    VBox kutu = new VBox(4);
                    kutu.setPadding(new Insets(4));
                    gorevPilleriEkle(kutu, saatGorevleri, true);
                    saatKutusu.getChildren().add(kutu);
                }
                int satir = saatDegeri - 7;
                saatKutusu.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && !event.isConsumed()) {
                        yeniGorevDialoguAc(gun, LocalTime.of(saatDegeri, 0));
                    }
                });
                dropHedefiBagla(saatKutusu, gun);
                haftaTakvimGrid.add(saatKutusu, gunIndex + 1, satir);
            }
        }
    }

    private void gorevPilleriEkle(VBox hedef, List<Yapilacak> gorevler, boolean kompakt) {
        int limit = Math.min(gorevler.size(), 3);
        for (int i = 0; i < limit; i++) {
            hedef.getChildren().add(gorevPillOlustur(gorevler.get(i), kompakt));
        }
        if (gorevler.size() > 3) {
            Label dahaFazla = new Label("+" + (gorevler.size() - 3) + " daha");
            dahaFazla.getStyleClass().add("calendar-more-label");
            dahaFazla.setOnMouseClicked(event -> {
                gorevSec(gorevler.get(0));
                event.consume();
            });
            hedef.getChildren().add(dahaFazla);
        }
    }

    private Label gorevPillOlustur(Yapilacak gorev, boolean kompakt) {
        Label pill = new Label(StringUtils.abbreviate(gorevPillMetni(gorev), kompakt ? 18 : 22));
        pill.getStyleClass().add("calendar-pill");
        if (kompakt) {
            pill.getStyleClass().add("calendar-pill-compact");
        }
        if (!gorev.isAllDay() && gorev.getStartTime() != null) {
            pill.getStyleClass().add("calendar-pill-timed");
            if (kompakt) {
                int sureDakika = gorevSuresiDakika(gorev);
                double yukseklik = Math.min(52, Math.max(16, 14 + (sureDakika / 60.0) * 18));
                pill.setMinHeight(yukseklik);
                pill.setPrefHeight(yukseklik);
                if (sureDakika >= 120) {
                    pill.getStyleClass().add("calendar-pill-long");
                }
            }
            Tooltip.install(pill, new Tooltip(gorevSaatAraligiMetni(gorev)));
        }
        pill.getStyleClass().add(oncelikStilSinifi(gorev.getOncelik()));
        pill.setMaxWidth(Double.MAX_VALUE);

        pill.setOnMouseClicked(event -> {
            gorevSec(gorev);
            event.consume();
        });

        pill.setOnDragDetected(event -> {
            Dragboard dragboard = pill.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(gorev.getId());
            dragboard.setContent(content);
            event.consume();
        });
        return pill;
    }

    private int gorevSuresiDakika(Yapilacak gorev) {
        if (gorev == null || gorev.isAllDay() || gorev.getStartTime() == null) {
            return 0;
        }
        if (gorev.getEndTime() == null) {
            return 60;
        }
        long dakika = Duration.between(gorev.getStartTime(), gorev.getEndTime()).toMinutes();
        return (int) Math.max(15, dakika);
    }

    private String gorevSaatAraligiMetni(Yapilacak gorev) {
        if (gorev == null || gorev.getStartTime() == null) {
            return "Tüm gün";
        }
        String baslangic = gorev.getStartTime().format(SAAT_FORMATI);
        if (gorev.getEndTime() == null) {
            return baslangic;
        }
        return baslangic + " - " + gorev.getEndTime().format(SAAT_FORMATI);
    }

    private String gorevPillMetni(Yapilacak gorev) {
        if (gorev == null) {
            return "";
        }
        if (gorev.isAllDay() || gorev.getStartTime() == null) {
            return gorev.getAciklama();
        }
        String saatAraligi = gorev.getStartTime().format(SAAT_FORMATI);
        if (gorev.getEndTime() != null) {
            saatAraligi += "-" + gorev.getEndTime().format(SAAT_FORMATI);
        }
        return saatAraligi + " " + gorev.getAciklama();
    }

    private String oncelikStilSinifi(Oncelik oncelik) {
        if (oncelik == null) {
            return "medium";
        }
        return switch (oncelik) {
            case LOW -> "low";
            case MEDIUM -> "medium";
            case HIGH -> "high";
        };
    }

    private void dropHedefiBagla(Node hedef, LocalDate tarih) {
        hedef.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString() && event.getGestureSource() != hedef) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        hedef.setOnDragDropped(event -> {
            boolean basarili = false;
            String id = event.getDragboard().getString();
            if (id != null) {
                Yapilacak gorev = gorevBul(id);
                if (gorev != null) {
                    gorev.setTarih(tarih);
                    if (seciliGorev == gorev) {
                        detayPaneliniDoldur(gorev);
                    }
                    takvimiYenile();
                    basarili = true;
                }
            }
            event.setDropCompleted(basarili);
            event.consume();
        });
    }

    private Yapilacak gorevBul(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return taskService.tumGorevler().stream()
                .filter(gorev -> id.equals(gorev.getId()))
                .findFirst()
                .orElse(null);
    }

    private List<Yapilacak> gununGorevleri(LocalDate gun) {
        return taskService.tumGorevler().stream()
                .filter(gorev -> gun.equals(gorev.getTarih()))
                .sorted(
                        Comparator
                                .comparing((Yapilacak gorev) -> gorev.isAllDay() ? 0 : 1)
                                .thenComparing(gorev -> gorev.getStartTime() == null ? LocalTime.MAX : gorev.getStartTime())
                                .thenComparing(gorev -> gorev.getAciklama().toLowerCase(TR_LOCALE))
                )
                .toList();
    }

    private void yeniGorevDialoguAc(LocalDate tarih, LocalTime varsayilanSaat) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Yeni Yapılacak");
        dialog.setHeaderText("Yeni görevi seçilen tarihe ekleyin.");
        if (donemBaslikButton.getScene() != null) {
            dialog.initOwner(donemBaslikButton.getScene().getWindow());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("yapilacakDialogEkrani.fxml"));
        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Yeni görev penceresi açılamadı");
            alert.setContentText("Lütfen tekrar deneyin.");
            alert.showAndWait();
            return;
        }

        DialogController dialogController = loader.getController();
        dialogController.varsayilanTarihVeSaatAyarla(tarih, varsayilanSaat);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> cikti = dialog.showAndWait();
        if (cikti.isEmpty() || cikti.get() != ButtonType.OK) {
            return;
        }

        Yapilacak yeni = dialogController.ciktiyiGoster();
        if (yeni == null) {
            return;
        }

        taskService.gorevEkle(yeni);
        gorevSec(yeni);
        takvimiYenile();
    }

    private void gorevSec(Yapilacak gorev) {
        seciliGorev = gorev;
        detayPaneliniDoldur(gorev);
    }

    private void detayPaneliniDoldur(Yapilacak gorev) {
        detayGuncelleniyor = true;
        try {
            boolean secimVar = gorev != null;
            detayBosDurumBox.setVisible(!secimVar);
            detayBosDurumBox.setManaged(!secimVar);
            detayEditorBox.setVisible(secimVar);
            detayEditorBox.setManaged(secimVar);

            if (!secimVar) {
                detayBaslikField.clear();
                detayTarihPicker.setValue(null);
                tumGunCheckBox.setSelected(true);
                baslangicSaatComboBox.setValue(null);
                bitisSaatComboBox.setValue(null);
                saatAlanlariniGuncelle(true);
                detayNotArea.clear();
                oncelikToggleGroup.selectToggle(null);
                return;
            }

            detayBaslikField.setText(gorev.getAciklama());
            detayTarihPicker.setValue(gorev.getTarih());
            tumGunCheckBox.setSelected(gorev.isAllDay());
            baslangicSaatComboBox.setValue(saatMetniniFormatla(gorev.getStartTime()));
            bitisSaatComboBox.setValue(saatMetniniFormatla(gorev.getEndTime()));
            saatAlanlariniGuncelle(gorev.isAllDay());
            detayNotArea.setText(gorev.getDetay());
            oncelikButonunuSec(gorev.getOncelik());
        } finally {
            detayGuncelleniyor = false;
        }
    }

    private void saatAlanlariniGuncelle(boolean tumGunSecili) {
        baslangicSaatComboBox.setDisable(tumGunSecili);
        bitisSaatComboBox.setDisable(tumGunSecili);
    }

    private String saatMetniniFormatla(LocalTime saat) {
        if (saat == null) {
            return null;
        }
        return saat.format(SAAT_FORMATI);
    }

    private LocalTime saatMetniniParseEt(String saatMetni) {
        if (saatMetni == null || saatMetni.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(saatMetni, SAAT_FORMATI);
        } catch (DateTimeParseException e) {
            try {
                return LocalTime.parse(saatMetni);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private void oncelikButonunuSec(Oncelik oncelik) {
        if (oncelik == null) {
            oncelikToggleGroup.selectToggle(oncelikOrtaButton);
            return;
        }
        switch (oncelik) {
            case LOW -> oncelikToggleGroup.selectToggle(oncelikDusukButton);
            case MEDIUM -> oncelikToggleGroup.selectToggle(oncelikOrtaButton);
            case HIGH -> oncelikToggleGroup.selectToggle(oncelikYuksekButton);
        }
    }

    private void seciliBasligiGuncelle(String baslik) {
        if (detayGuncelleniyor || seciliGorev == null) {
            return;
        }
        String yeniBaslik = baslik == null ? "" : baslik.trim();
        if (yeniBaslik.isEmpty() || yeniBaslik.equals(seciliGorev.getAciklama())) {
            return;
        }
        seciliGorev.setAciklama(yeniBaslik);
        takvimiYenile();
    }

    private void seciliTarihiGuncelle(LocalDate tarih) {
        if (detayGuncelleniyor || seciliGorev == null || tarih == null) {
            return;
        }
        if (tarih.equals(seciliGorev.getTarih())) {
            return;
        }
        seciliGorev.setTarih(tarih);
        takvimiYenile();
    }

    private void seciliTumGunDurumunuGuncelle(boolean tumGunSecili) {
        if (detayGuncelleniyor || seciliGorev == null) {
            return;
        }

        LocalTime baslangic = saatMetniniParseEt(baslangicSaatComboBox.getValue());
        LocalTime bitis = saatMetniniParseEt(bitisSaatComboBox.getValue());

        if (tumGunSecili) {
            baslangic = null;
            bitis = null;
        } else {
            if (baslangic == null) {
                baslangic = LocalTime.of(9, 0);
            }
            if (bitis != null && bitis.isBefore(baslangic)) {
                bitis = baslangic;
            }
        }

        taskService.gorevZamaniniGuncelle(seciliGorev, tumGunSecili, baslangic, bitis);
        detayPaneliniDoldur(seciliGorev);
        takvimiYenile();
    }

    private void seciliBaslangicSaatiniGuncelle(String yeniSaat) {
        if (detayGuncelleniyor || seciliGorev == null || tumGunCheckBox.isSelected()) {
            return;
        }

        LocalTime baslangic = saatMetniniParseEt(yeniSaat);
        if (baslangic == null) {
            return;
        }
        LocalTime bitis = saatMetniniParseEt(bitisSaatComboBox.getValue());
        if (bitis != null && bitis.isBefore(baslangic)) {
            bitis = baslangic;
        }

        taskService.gorevZamaniniGuncelle(seciliGorev, false, baslangic, bitis);
        detayPaneliniDoldur(seciliGorev);
        takvimiYenile();
    }

    private void seciliBitisSaatiniGuncelle(String yeniSaat) {
        if (detayGuncelleniyor || seciliGorev == null || tumGunCheckBox.isSelected()) {
            return;
        }

        LocalTime baslangic = saatMetniniParseEt(baslangicSaatComboBox.getValue());
        if (baslangic == null) {
            baslangic = LocalTime.of(9, 0);
        }
        LocalTime bitis = saatMetniniParseEt(yeniSaat);
        if (bitis != null && bitis.isBefore(baslangic)) {
            bitis = baslangic;
        }

        taskService.gorevZamaniniGuncelle(seciliGorev, false, baslangic, bitis);
        detayPaneliniDoldur(seciliGorev);
        takvimiYenile();
    }

    private void seciliNotuGuncelle(String not) {
        if (detayGuncelleniyor || seciliGorev == null) {
            return;
        }
        String yeniNot = not == null ? "" : not;
        if (yeniNot.equals(seciliGorev.getDetay())) {
            return;
        }
        seciliGorev.setDetay(yeniNot);
    }

    private void seciliOnceligiGuncelle() {
        if (detayGuncelleniyor || seciliGorev == null) {
            return;
        }
        Toggle seciliToggle = oncelikToggleGroup.getSelectedToggle();
        if (seciliToggle == null || seciliToggle.getUserData() == null) {
            return;
        }
        Oncelik oncelik = (Oncelik) seciliToggle.getUserData();
        if (oncelik == seciliGorev.getOncelik()) {
            return;
        }
        seciliGorev.setOncelik(oncelik);
        takvimiYenile();
    }

    private ColumnConstraints sutunKopyasi() {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(100.0 / 7.0);
        constraints.setHgrow(Priority.ALWAYS);
        return constraints;
    }

    private ColumnConstraints sabitSutunKopyasi(double width) {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPrefWidth(width);
        constraints.setMinWidth(width);
        constraints.setMaxWidth(width);
        return constraints;
    }
}

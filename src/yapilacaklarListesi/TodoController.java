package yapilacaklarListesi;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import yapilacaklarListesi.mediator.PomodoroGosterge;
import yapilacaklarListesi.muzik.Muzik;
import yapilacaklarListesi.muzik.MuzikOynatici;
import yapilacaklarListesi.pomodoro.model.Pomodoro;
import yapilacaklarListesi.pomodoro.model.PomodoroEnum;
import yapilacaklarListesi.service.TaskService;
import yapilacaklarListesi.settings.SettingsManager;
import yapilacaklarListesi.settings.UpdateChecker;
import yapilacaklarListesi.veriler.Oncelik;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;
import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.prefs.Preferences;

// Todo ekrani controller'i.
public class TodoController {

    @FXML private MenuItem yeniFXML;
    @FXML private MenuItem farkliKaydetFXML;
    @FXML private MenuItem kaydetFXML;
    @FXML private MenuItem silFXML;
    @FXML private MenuItem kesFXML;
    @FXML private MenuItem kopyalaFXML;
    @FXML private MenuItem yapistirFXML;
    @FXML private ToggleButton pomodoroToggleButtonFXML;
    @FXML private VBox vbox;
    @FXML private ToggleButton bugunToggleButton;
    @FXML private Button temaToggleButton;
    @FXML private ComboBox<String> oncelikFiltreFXML;
    @FXML private TextField aramaFXML;
    @FXML private ListView<Yapilacak> yapilacakListeFXML;
    @FXML private ListView<Yapilacak> q1ListView;
    @FXML private ListView<Yapilacak> q2ListView;
    @FXML private ListView<Yapilacak> q3ListView;
    @FXML private ListView<Yapilacak> q4ListView;
    @FXML private Label gorevSayaciLabel;
    @FXML private Label q1CountBadge;
    @FXML private Label q2CountBadge;
    @FXML private Label q3CountBadge;
    @FXML private Label q4CountBadge;
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
    @FXML private TextArea detayFXML;
    @FXML private Button hizliEkleButton;

    private FilteredList<Yapilacak> yapilacakFilteredList;
    private Predicate<Yapilacak> tumYapilacaklarPredicate;
    private Predicate<Yapilacak> bugunYapilacaklarPredicate;
    private Predicate<Yapilacak> aramaPredicate;
    private Predicate<Yapilacak> oncelikPredicate;
    Clipboard sistemPanosu = Clipboard.getSystemClipboard();

    private Pomodoro suankiPomodoro;
    private final StringProperty zamanlayiciText;
    private final TaskService taskService;
    private final Preferences preferences;
    private ToggleGroup oncelikToggleGroup;
    private boolean detayAlanlariGuncelleniyor;
    private Timeline timeLine;
    private Runnable ayarlarNavigasyonHandler;
    private EisenhowerController eisenhowerController;

    private static final String DARK_MODE_CLASS = "dark-mode";
    private static final String PREF_DARK_MODE = "theme.darkModeEnabled";
    private static final DateTimeFormatter SAAT_FORMATI = DateTimeFormatter.ofPattern("HH:mm");

    public TodoController(){
        zamanlayiciText = new SimpleStringProperty();
        taskService = new TaskService(YapilacakVeri.getInstance());
        preferences = Preferences.userNodeForPackage(TodoController.class);
        setTimerText(0);
    }

    // Program başlarken yapılacaklar
    public void initialize() {
        yeniFXML.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        kaydetFXML.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        silFXML.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        oncelikFiltreFXML.getItems().setAll("Tümü", "Yüksek", "Orta", "Düşük");
        oncelikFiltreFXML.setValue("Tümü");
        detayPaneliniHazirla();
        boolean koyuTemaAktif = preferences.getBoolean(PREF_DARK_MODE, false);
        darkModeUygula(koyuTemaAktif);
        Platform.runLater(() -> darkModeUygula(koyuTemaAktif));
        otomatikGuncellemeKontrolunuBaslat();

        farkliKaydetFXML.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Text");
            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                return;
            }
            final String content = detayFXML.getText();
            try (final BufferedWriter writer = Files.newBufferedWriter(file.getAbsoluteFile().toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                writer.write(content);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Yapılacak seçildiğinde detayın da gelmesi için kullanılan metod
        yapilacakListeFXML.getSelectionModel().selectedItemProperty().addListener((observable, eskiDeger, yeniDeger) -> {
            detayPaneliniDoldur(yeniDeger);
            if (eisenhowerController != null) {
                eisenhowerController.goreviSec(yeniDeger);
            }
        });

        detayFXML.setOnKeyPressed(keyEvent -> {
            if (keyEvent.isShortcutDown() && keyEvent.getCode().equals(KeyCode.S)) {
                try {
                    Yapilacak yapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
                    if (yapilacak == null) {
                        return;
                    }
                    taskService.gorevDetayiGuncelleVeKaydet(yapilacak, detayFXML.getText());
                    keyEvent.consume();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        silFXML.setOnAction((ActionEvent e) -> {
            Yapilacak yapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
            if (yapilacak == null) {
                return;
            }
            yapilacakSil(yapilacak);
        });

        kaydetFXML.setOnAction((ActionEvent e) -> {
            Yapilacak yapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
            if (yapilacak == null) {
                return;
            }
            taskService.gorevDetayiGuncelle(yapilacak, detayFXML.getText());
            yapilacakKaydet();
        });

        tumYapilacaklarPredicate = taskService.tumGorevlerFiltresi();
        bugunYapilacaklarPredicate = taskService.bugunGorevleriFiltresi();
        aramaPredicate = taskService.tumGorevlerFiltresi();
        oncelikPredicate = taskService.tumGorevlerFiltresi();

        yapilacakFilteredList = new FilteredList<>(taskService.tumGorevler(), tumYapilacaklarPredicate);
        aramaFXML.textProperty().addListener((observableValue, eskiDeger, yeniDeger) -> filtreleriUygula());
        oncelikFiltreFXML.getSelectionModel().selectedItemProperty().addListener((obs, eski, yeni) -> filtreleriUygula());

        SortedList<Yapilacak> sortedList = new SortedList<>(yapilacakFilteredList, Comparator.comparing(Yapilacak::getTarih));

        pomodoroToggleButtonFXML.setOnAction(observable -> {
            if (pomodoroToggleButtonFXML.isSelected()){
                baslat();
            } else {
                durdur();
            }
        });

        taskService.tumGorevler().addListener((ListChangeListener<Yapilacak>) change -> gorevSayaciniGuncelle());

        yapilacakListeFXML.setItems(sortedList);
        yapilacakListeFXML.setCellFactory(list -> new ListCell<>() {
            private final Circle oncelikNoktasi = new Circle(4.5);
            private final Label gorevBasligi = new Label();
            private final HBox satir = new HBox(10, oncelikNoktasi, gorevBasligi);

            {
                satir.setAlignment(Pos.CENTER_LEFT);
                gorevBasligi.getStyleClass().add("task-title");
            }

            @Override
            protected void updateItem(Yapilacak item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                oncelikNoktasi.setFill(oncelikNoktasiRengi(item.getOncelik()));
                gorevBasligi.setText(item.getAciklama());
                setText(null);
                setGraphic(satir);
            }
        });
        yapilacakListeFXML.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        eisenhowerController = new EisenhowerController(
                q1ListView,
                q2ListView,
                q3ListView,
                q4ListView,
                q1CountBadge,
                q2CountBadge,
                q3CountBadge,
                q4CountBadge
        );
        eisenhowerController.initialize(taskService.tumGorevler(), this::matristenGorevSec, this::matristenGorevGuncelle);
        yapilacakListeFXML.getSelectionModel().selectFirst();
        filtreleriUygula();

    }

    private void detayPaneliniHazirla() {
        oncelikToggleGroup = new ToggleGroup();
        oncelikDusukButton.setToggleGroup(oncelikToggleGroup);
        oncelikOrtaButton.setToggleGroup(oncelikToggleGroup);
        oncelikYuksekButton.setToggleGroup(oncelikToggleGroup);

        oncelikDusukButton.setUserData(Oncelik.LOW);
        oncelikOrtaButton.setUserData(Oncelik.MEDIUM);
        oncelikYuksekButton.setUserData(Oncelik.HIGH);
        saatSecenekleriniHazirla();

        detayBaslikField.textProperty().addListener((obs, eski, yeni) -> seciliGorevBasliginiGuncelle(yeni));
        detayTarihPicker.valueProperty().addListener((obs, eski, yeni) -> seciliGorevTarihiniGuncelle(yeni));
        tumGunCheckBox.selectedProperty().addListener((obs, eski, yeni) -> seciliGorevTumGunDurumunuGuncelle(yeni));
        baslangicSaatComboBox.valueProperty().addListener((obs, eski, yeni) -> seciliGorevBaslangicSaatiniGuncelle(yeni));
        bitisSaatComboBox.valueProperty().addListener((obs, eski, yeni) -> seciliGorevBitisSaatiniGuncelle(yeni));
        detayFXML.textProperty().addListener((obs, eski, yeni) -> seciliGorevNotunuGuncelle(yeni));
        oncelikToggleGroup.selectedToggleProperty().addListener((obs, eski, yeni) -> seciliGorevOnceliginiGuncelle());
        detayPaneliniDoldur(null);
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

    private void detayPaneliniDoldur(Yapilacak gorev) {
        detayAlanlariGuncelleniyor = true;
        try {
            boolean secimVar = gorev != null;
            detayBosDurumBox.setVisible(!secimVar);
            detayBosDurumBox.setManaged(!secimVar);
            detayEditorBox.setVisible(secimVar);
            detayEditorBox.setManaged(secimVar);
            if (eisenhowerController != null) {
                eisenhowerController.goreviSec(gorev);
            }

            if (!secimVar) {
                detayBaslikField.clear();
                detayTarihPicker.setValue(null);
                tumGunCheckBox.setSelected(true);
                baslangicSaatComboBox.setValue(null);
                bitisSaatComboBox.setValue(null);
                saatAlanlariniGuncelle(true);
                detayFXML.clear();
                oncelikToggleGroup.selectToggle(null);
                return;
            }

            detayBaslikField.setText(gorev.getAciklama());
            detayTarihPicker.setValue(gorev.getTarih());
            tumGunCheckBox.setSelected(gorev.isAllDay());
            baslangicSaatComboBox.setValue(saatMetniniFormatla(gorev.getStartTime()));
            bitisSaatComboBox.setValue(saatMetniniFormatla(gorev.getEndTime()));
            saatAlanlariniGuncelle(gorev.isAllDay());
            detayFXML.setText(gorev.getDetay());
            oncelikButonunuSec(gorev.getOncelik());
        } finally {
            detayAlanlariGuncelleniyor = false;
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

    private void seciliGorevBasliginiGuncelle(String yeniBaslik) {
        if (detayAlanlariGuncelleniyor) {
            return;
        }
        Yapilacak secili = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (secili == null) {
            return;
        }
        String temizBaslik = yeniBaslik == null ? "" : yeniBaslik.trim();
        if (temizBaslik.isEmpty() || temizBaslik.equals(secili.getAciklama())) {
            return;
        }
        secili.setAciklama(temizBaslik);
        gorevSatiriniYenile(secili);
        if (aramaAktifMi()) {
            filtreleriUygula();
        }
    }

    private void seciliGorevTarihiniGuncelle(LocalDate yeniTarih) {
        if (detayAlanlariGuncelleniyor || yeniTarih == null) {
            return;
        }
        Yapilacak secili = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (secili == null || yeniTarih.equals(secili.getTarih())) {
            return;
        }
        secili.setTarih(yeniTarih);
        gorevSatiriniYenile(secili);
        if (bugunToggleButton.isSelected()) {
            filtreleriUygula();
        }
    }

    private void seciliGorevTumGunDurumunuGuncelle(boolean tumGunSecili) {
        if (detayAlanlariGuncelleniyor) {
            return;
        }
        Yapilacak secili = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (secili == null) {
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

        taskService.gorevZamaniniGuncelle(secili, tumGunSecili, baslangic, bitis);
        detayPaneliniDoldur(secili);
    }

    private void seciliGorevBaslangicSaatiniGuncelle(String yeniSaat) {
        if (detayAlanlariGuncelleniyor || tumGunCheckBox.isSelected()) {
            return;
        }
        Yapilacak secili = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (secili == null) {
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

        taskService.gorevZamaniniGuncelle(secili, false, baslangic, bitis);
        detayPaneliniDoldur(secili);
    }

    private void seciliGorevBitisSaatiniGuncelle(String yeniSaat) {
        if (detayAlanlariGuncelleniyor || tumGunCheckBox.isSelected()) {
            return;
        }
        Yapilacak secili = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (secili == null) {
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

        taskService.gorevZamaniniGuncelle(secili, false, baslangic, bitis);
        detayPaneliniDoldur(secili);
    }

    private void seciliGorevOnceliginiGuncelle() {
        if (detayAlanlariGuncelleniyor) {
            return;
        }
        Yapilacak secili = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (secili == null) {
            return;
        }
        Toggle seciliToggle = oncelikToggleGroup.getSelectedToggle();
        if (seciliToggle == null || seciliToggle.getUserData() == null) {
            return;
        }
        Oncelik yeniOncelik = (Oncelik) seciliToggle.getUserData();
        if (yeniOncelik == secili.getOncelik()) {
            return;
        }
        secili.setOncelik(yeniOncelik);
        gorevSatiriniYenile(secili);
        if (oncelikFiltreAktifMi()) {
            filtreleriUygula();
        }
    }

    private void seciliGorevNotunuGuncelle(String yeniNot) {
        if (detayAlanlariGuncelleniyor) {
            return;
        }
        Yapilacak secili = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (secili == null) {
            return;
        }
        String guncelNot = yeniNot == null ? "" : yeniNot;
        if (guncelNot.equals(secili.getDetay())) {
            return;
        }
        taskService.gorevDetayiGuncelle(secili, guncelNot);
        if (aramaAktifMi()) {
            filtreleriUygula();
        }
    }

    private void gorevSatiriniYenile(Yapilacak gorev) {
        int index = taskService.tumGorevler().indexOf(gorev);
        if (index >= 0) {
            taskService.tumGorevler().set(index, gorev);
            yapilacakListeFXML.getSelectionModel().select(gorev);
        }
        yapilacakListeFXML.refresh();
    }

    private void matristenGorevSec(Yapilacak gorev) {
        if (gorev == null) {
            return;
        }
        yapilacakListeFXML.getSelectionModel().select(gorev);
        detayPaneliniDoldur(gorev);
    }

    private void matristenGorevGuncelle(Yapilacak gorev) {
        if (gorev == null) {
            return;
        }
        gorevSatiriniYenile(gorev);
        filtreleriUygula();
    }

    private void gorevSayaciniGuncelle() {
        if (gorevSayaciLabel == null || yapilacakFilteredList == null) {
            return;
        }
        int toplam = taskService.tumGorevler().size();
        int gosterilen = yapilacakFilteredList.size();

        if (filtreAktifMi()) {
            gorevSayaciLabel.setText(String.format("%d / %d görev gösteriliyor", gosterilen, toplam));
            return;
        }
        gorevSayaciLabel.setText(String.format("%d görev", toplam));
    }

    private boolean filtreAktifMi() {
        boolean bugunAktif = bugunToggleButton.isSelected();
        return bugunAktif || aramaAktifMi() || oncelikFiltreAktifMi();
    }

    private boolean aramaAktifMi() {
        return aramaFXML.getText() != null && !aramaFXML.getText().isBlank();
    }

    private boolean oncelikFiltreAktifMi() {
        String secim = oncelikFiltreFXML.getValue();
        return secim != null && !secim.equals("Tümü");
    }

    @FXML
    public void temaDegistir() {
        Parent hedef = temaHedefiniBul();
        boolean aktif = !hedef.getStyleClass().contains(DARK_MODE_CLASS);
        darkModeUygula(aktif);
        preferences.putBoolean(PREF_DARK_MODE, aktif);
    }

    private void darkModeUygula(boolean aktif) {
        Parent hedef = temaHedefiniBul();
        if (aktif) {
            if (!hedef.getStyleClass().contains(DARK_MODE_CLASS)) {
                hedef.getStyleClass().add(DARK_MODE_CLASS);
            }
            temaToggleButton.setText("☀");
            return;
        }
        hedef.getStyleClass().remove(DARK_MODE_CLASS);
        temaToggleButton.setText("☾");
    }

    private Parent temaHedefiniBul() {
        if (vbox.getScene() != null && vbox.getScene().getRoot() != null) {
            return vbox.getScene().getRoot();
        }
        return vbox;
    }

    @FXML
    public void yapilacakAlertsizKaydet() {
        try {
            taskService.kaydet();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void yapilacakKaydet() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Yapilacakları Kaydet");
        alert.setContentText("Yapılacakları kaydetmek istediğinizden emin misiniz?");
        Optional<ButtonType> sonuc = alert.showAndWait();
        if (sonuc.isPresent() && (sonuc.get() == ButtonType.OK)) {
            try {
                taskService.kaydet();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void prepareAttempt(PomodoroEnum pomodoroHatirlatici) {
        reset();

        suankiPomodoro = new Pomodoro(pomodoroHatirlatici);
        timeLine = new Timeline();
        timeLine.setCycleCount(pomodoroHatirlatici.getToplamSaniye());
        timeLine.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            suankiPomodoro.tik();
//            pomodoroToggleButtonFXML.setText(suankiPomodoro.toString());
            pomodoroToggleButtonFXML.setText(PomodoroGosterge.pomodoroGosterge(suankiPomodoro));
        }));
        timeLine.setOnFinished(e -> {
            zamanlayiciyiKaydet();
            prepareAttempt((suankiPomodoro.getPomodoroEnum() == PomodoroEnum.FOCUS) ? PomodoroEnum.BREAK : PomodoroEnum.FOCUS);
        });
    }

    private void zamanlayiciyiKaydet() {
        suankiPomodoro.kaydet();
    }

    public void setZamanlayiciText(String zamanlayiciText) {
        this.zamanlayiciText.set(zamanlayiciText);
    }

    public void setTimerText(int geriyeKalanSaniye) {
        int dakika = geriyeKalanSaniye / 60;
        int saniye = geriyeKalanSaniye % 60;
        setZamanlayiciText(String.format("%02d:%02d", dakika, saniye));
    }

    private void reset() {
        if (timeLine != null && timeLine.getStatus() == Animation.Status.RUNNING) {
            timeLine.stop();
        }
    }

    public void zamanlayiciyiBaslat() {
        timeLine.play();
    }

    public void zamanlayiciyiDuraklat() {
        timeLine.pause();
    }

    public void zamanlayiciRestart() {
        prepareAttempt(PomodoroEnum.FOCUS);
        zamanlayiciyiBaslat();
    }

    public void baslat() {
        if (suankiPomodoro == null) {
            zamanlayiciRestart();
        } else {
            zamanlayiciyiBaslat();
        }
    }

    public void durdur() {
        zamanlayiciyiDuraklat();
    }

    // Yeni yapılacak eklemek için yapılmış yeni bir dialog
    @FXML
    public void yeniYapilacakDialog() {
        MuzikOynatici.dialogMuzikOynat();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Yeni Yapılacak");
        dialog.setHeaderText("Yeni yapılacak aktiviteyi buradan oluşturabilirsiniz.");
        dialog.initOwner(vbox.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("yapilacakDialogEkrani.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Dialog'u yükleyemedik.");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> cikti = dialog.showAndWait();
        if (cikti.isPresent() && cikti.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();
            Yapilacak yeniYapilacak = controller.ciktiyiGoster();
            if (yeniYapilacak == null) {
                return;
            }
            taskService.gorevEkle(yeniYapilacak);
            yapilacakListeFXML.getSelectionModel().select(yeniYapilacak);
            MuzikOynatici.okMuzigiOynat();
        } else {
            MuzikOynatici.cancelMuzigiOynat();
        }
    }


    @FXML
    public void ayarlariAc() {
        if (ayarlarNavigasyonHandler != null) {
            ayarlarNavigasyonHandler.run();
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("settings.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root, 560, 480);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("app.css")).toExternalForm());
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("dark-mode.css")).toExternalForm());

            Parent temaHedefi = temaHedefiniBul();
            if (temaHedefi.getStyleClass().contains(DARK_MODE_CLASS) && !root.getStyleClass().contains(DARK_MODE_CLASS)) {
                root.getStyleClass().add(DARK_MODE_CLASS);
            }

            Stage stage = new Stage();
            stage.setTitle("Ayarlar");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(vbox.getScene().getWindow());
            stage.setResizable(false);
            stage.setScene(scene);
            stage.showAndWait();

            SettingsManager settingsManager = new SettingsManager();
            boolean koyuTema = settingsManager.getEffectiveThemeMode() == SettingsManager.ThemeMode.DARK;
            darkModeUygula(koyuTema);
            preferences.putBoolean(PREF_DARK_MODE, koyuTema);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ayarlar");
            alert.setHeaderText("Ayarlar penceresi açılamadı");
            alert.setContentText("Lütfen tekrar deneyin.");
            alert.showAndWait();
        }
    }

    public void setAyarlarNavigasyonHandler(Runnable ayarlarNavigasyonHandler) {
        this.ayarlarNavigasyonHandler = ayarlarNavigasyonHandler;
    }

    public void disTemaTercihiniUygula(boolean koyuTema) {
        darkModeUygula(koyuTema);
        preferences.putBoolean(PREF_DARK_MODE, koyuTema);
    }

    @FXML
    public void emailGonderMetodu() {
        try {
            String url = "mailto:tehadro@gmail.com?subject=Program%20Hakkinda";
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("E-posta Destegi");
                alert.setHeaderText("Sistem e-posta acamiyor");
                alert.setContentText("Lutfen manuel olarak tehadro@gmail.com adresine ulasin.");
                alert.showAndWait();
                return;
            }
            Desktop.getDesktop().mail(new URI(url));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // Klavyemizden DELETE tuşuna bastığımızda, ListView'de hangi yapılacak seçili ise onu siliyor.
    @FXML
    public void tusaBasildiginda(javafx.scene.input.KeyEvent keyEvent) {
        Yapilacak secilenYapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (secilenYapilacak != null)
            if (keyEvent.getCode().equals(KeyCode.DELETE))
                yapilacakSil(secilenYapilacak);
    }

    // Yapılacak silinirken bir Alert sahnesi gösteriyor. OK butonu ile parametresi ile birlikte yapilacakSil metodu çağrılıyor.
    // ListView'den seçili olan yapılacak siliniyor. Dikkat edilmesi gerekn kısımlardan bir tanesi ise
    // Yapılacaklar kaydedilirken newLine'da eklendiği için yapılacak silinirken boşluk kalmıyor.
    public void yapilacakSil(Yapilacak yapilacak) {
        if (yapilacak == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Yapilacagi Sil");
        alert.setHeaderText("Yapılacak: " + yapilacak.getAciklama());
        alert.setContentText("Silmek için OK, iptal etmek için Cancel");
        Optional<ButtonType> sonuc = alert.showAndWait();

        if (sonuc.isPresent() && (sonuc.get() == ButtonType.OK)) {
            taskService.gorevSil(yapilacak);
        }
    }

    // Bu metod ListView'deki herhangi bir yapılacağın Acik
    @FXML
    public void bugunYapilacakGoster() {
        filtreleriUygula();
    }

    // Alert sahnesi ile yapılan Hakkımda metodu.
    public void hakkindaMetodu() {
        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.setTitle("Hakkında");
        al.setHeaderText("Hakkında");
        al.setContentText("Emeği Geçenler:\n\nBerkan Sözer\n");
        al.showAndWait();
    }

    // Programdan çıkış yaparken Alert veriyor. showAndWait ile Alert'i bekletmek zorundayız yoksa hemen kapanıyor.
    // CANCEL tuşuna bastığımızda hiçbir şey yapmıyor ancak OK tuşuna bastığımızda Platform.exit() metodunu çağırarak programdan çıkabiliyoruz.
    public void kapat() {
        Alert cikisAlert = new Alert(Alert.AlertType.CONFIRMATION);
        cikisAlert.setTitle("Çıkış Yap");
        cikisAlert.setHeaderText("Emin misiniz?");
        cikisAlert.setResizable(false);

        Optional<ButtonType> result = cikisAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    // detayFXML'de seçilen yeri kesme metodu
    @FXML
    public void kes() {
        String text = detayFXML.getSelectedText();

        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        sistemPanosu.setContent(content);

        // Buraya kadar olan kısımda seçilen texti sistem panosuna ekliyor. Geri kalan kısmında ise seçilen yeri(baslangic'tan son'a kadar) siliyor.

        IndexRange range = detayFXML.getSelection();
        String origText = detayFXML.getText();
        String baslangic = StringUtils.substring(origText, 0, range.getStart());
        String son = StringUtils.substring(origText, range.getEnd(), StringUtils.length(origText));

        detayFXML.setText(baslangic + son);
        detayFXML.positionCaret(range.getStart());

    }

    // Basit bir işlem. detayFXML'in içeriğini boş bir string ile değiştiriyor.
    @FXML
    public void sil(){
        detayFXML.replaceSelection("");
    }

    // detayFXML'de seçilen texti content nesnesine putString ile atıyoruz. systemClipboard ise işletim sisteminin clipboard'u.
    @FXML
    public void kopyala() {
        String text = detayFXML.getSelectedText();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        sistemPanosu.setContent(content);
    }

    // Sistem panosunda bir içerik varsa yapıştırıyoruz. stackoverflow'da bulduğum kodu kendi programıma uyarladım.
    // Metodun sonlarına doğru sistem panosunda olan içeriği, detayFXML'e yapıştırıyor
    @FXML
    public void yapistir() {
        if (!sistemPanosu.hasContent(DataFormat.PLAIN_TEXT)) {
            bosPanoIcinAyarlar();
            return;
        }

        String clipboardText = sistemPanosu.getString();
        IndexRange range = detayFXML.getSelection();
        String orjinalText = detayFXML.getText();

        int bitisPozisyonu;
        String guncellenmisText;
        String ilkKisim = StringUtils.substring(orjinalText, 0, range.getStart());
        String sonKisim = StringUtils.substring(orjinalText, range.getEnd(), StringUtils.length(orjinalText));

        guncellenmisText = ilkKisim + clipboardText + sonKisim;

        if (range.getStart() == range.getEnd()) {
            bitisPozisyonu = range.getEnd() + StringUtils.length(clipboardText);
        } else {
            bitisPozisyonu = range.getStart() + StringUtils.length(clipboardText);
        }

        detayFXML.setText(guncellenmisText);
        detayFXML.positionCaret(bitisPozisyonu);
    }

    // detayFXML'deki tüm text seç
    @FXML
    public void hepsiniSec(){
        detayFXML.selectAll();
    }

    // detayFXML'deki hiçbir texti seçme
    @FXML
    public void hicbiriniSecme(){
        detayFXML.deselect();
    }

    // Geriye kalan metodlar şuanda kullanılmıyor. Amaç kopyalanacakya da kesilecek bir şey yok ise "Kopyala" ya da "Kes" butonlarının setDisable(true) olması yani basılabilir olmaması
    @FXML
    public void duzenleMenuyuGoster() {
        if (sistemPanosu == null) {
            sistemPanosu = Clipboard.getSystemClipboard();
        }

        if (sistemPanosu.hasString()) {
            panoIceriginiAyarla();
        } else {
            bosPanoIcinAyarlar();
        }

        if (birSeySeciliMi()) {
            adjustForSelection();

        } else {
            adjustForDeselection();
        }
    }

    private boolean birSeySeciliMi() {
        return !detayFXML.getSelectedText().isEmpty();
    }

    private void bosPanoIcinAyarlar() {
        yapistirFXML.setDisable(true);  // Yapıştırılacak bir şey yok ise
    }

    private void panoIceriginiAyarla() {
        yapistirFXML.setDisable(false);  // Yapıştırılacak bir şey var ise
    }

    private void adjustForSelection() {
        kesFXML.setDisable(false);
        kopyalaFXML.setDisable(false);
    }

    private void adjustForDeselection() {
        kesFXML.setDisable(true);
        kopyalaFXML.setDisable(true);
    }

    private void filtreleriUygula() {
        Yapilacak seciliYapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        aramaPredicate = aramaFiltresiOlustur();
        oncelikPredicate = oncelikFiltresiOlustur();

        Predicate<Yapilacak> tarihFiltresi = bugunToggleButton.isSelected()
                ? bugunYapilacaklarPredicate
                : tumYapilacaklarPredicate;
        yapilacakFilteredList.setPredicate(tarihFiltresi.and(aramaPredicate).and(oncelikPredicate));

        if (yapilacakFilteredList.isEmpty()) {
            detayPaneliniDoldur(null);
            gorevSayaciniGuncelle();
            return;
        }

        if (seciliYapilacak != null && yapilacakFilteredList.contains(seciliYapilacak)) {
            yapilacakListeFXML.getSelectionModel().select(seciliYapilacak);
            detayPaneliniDoldur(seciliYapilacak);
            gorevSayaciniGuncelle();
            return;
        }
        yapilacakListeFXML.getSelectionModel().selectFirst();
        detayPaneliniDoldur(yapilacakListeFXML.getSelectionModel().getSelectedItem());
        gorevSayaciniGuncelle();
    }

    private Predicate<Yapilacak> aramaFiltresiOlustur() {
        String aramaMetni = aramaFXML.getText();
        if (aramaMetni == null || aramaMetni.isBlank()) {
            return taskService.tumGorevlerFiltresi();
        }
        String kriter = aramaMetni.toLowerCase().trim();
        return yapilacak -> {
            String aciklama = yapilacak.getAciklama() == null ? "" : yapilacak.getAciklama().toLowerCase();
            String detay = yapilacak.getDetay() == null ? "" : yapilacak.getDetay().toLowerCase();
            String tagler = String.join(" ", yapilacak.getTags()).toLowerCase();
            return aciklama.contains(kriter) || detay.contains(kriter) || tagler.contains(kriter);
        };
    }

    private Predicate<Yapilacak> oncelikFiltresiOlustur() {
        String secim = oncelikFiltreFXML.getValue();
        if (secim == null || secim.isBlank() || secim.equals("Tümü")) {
            return taskService.tumGorevlerFiltresi();
        }
        return switch (secim) {
            case "Yüksek" -> taskService.oncelikFiltresi(Oncelik.HIGH);
            case "Orta" -> taskService.oncelikFiltresi(Oncelik.MEDIUM);
            case "Düşük" -> taskService.oncelikFiltresi(Oncelik.LOW);
            default -> taskService.tumGorevlerFiltresi();
        };
    }

    private Color oncelikNoktasiRengi(Oncelik oncelik) {
        return switch (oncelik) {
            case HIGH -> Color.web("#FF3B30");
            case MEDIUM -> Color.web("#4A7CFF");
            case LOW -> Color.web("#34C759");
        };
    }

    private void otomatikGuncellemeKontrolunuBaslat() {
        SettingsManager settingsManager = new SettingsManager();
        if (!settingsManager.isAutoUpdateCheckEnabled()) {
            return;
        }
        UpdateChecker.latestRelease(UpdateChecker.DEFAULT_GITHUB_OWNER, UpdateChecker.DEFAULT_GITHUB_REPO)
                .whenComplete((result, throwable) -> {
                    if (throwable == null && result != null && result.success()) {
                        settingsManager.setLastUpdateCheckEpoch(System.currentTimeMillis());
                    }
                });
    }

}

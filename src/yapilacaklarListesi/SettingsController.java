package yapilacaklarListesi;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import yapilacaklarListesi.settings.SettingsManager;
import yapilacaklarListesi.settings.SettingsSection;
import yapilacaklarListesi.settings.sections.AppearanceSection;
import yapilacaklarListesi.settings.sections.DataSection;
import yapilacaklarListesi.settings.sections.NotificationsSection;
import yapilacaklarListesi.settings.sections.PomodoroSection;
import yapilacaklarListesi.settings.sections.UpdatesSection;

import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsController {

    @FXML private VBox settingsRoot;

    @FXML private Button gorunumKategoriButton;
    @FXML private Button pomodoroKategoriButton;
    @FXML private Button bildirimlerKategoriButton;
    @FXML private Button veriKategoriButton;
    @FXML private Button guncellemelerKategoriButton;

    @FXML private VBox gorunumSection;
    @FXML private VBox pomodoroSection;
    @FXML private VBox bildirimlerSection;
    @FXML private VBox veriSection;
    @FXML private VBox guncellemelerSection;

    @FXML private RadioButton temaAcikRadio;
    @FXML private RadioButton temaKoyuRadio;
    @FXML private RadioButton temaSistemRadio;
    @FXML private ComboBox<String> dilComboBox;

    @FXML private Spinner<Integer> pomodoroCalismaSpinner;
    @FXML private Spinner<Integer> pomodoroKisaMolaSpinner;
    @FXML private Spinner<Integer> pomodoroUzunMolaSpinner;
    @FXML private Spinner<Integer> pomodoroUzunMolaAraligiSpinner;
    @FXML private CheckBox pomodoroSesBildirimCheckBox;

    @FXML private CheckBox bildirimMasaustuCheckBox;
    @FXML private CheckBox gorevHatirlaticisiCheckBox;
    @FXML private CheckBox bildirimSesiCheckBox;
    @FXML private CheckBox sessizSaatlerCheckBox;
    @FXML private Spinner<Integer> sessizSaatBaslangicSaatSpinner;
    @FXML private Spinner<Integer> sessizSaatBaslangicDakikaSpinner;
    @FXML private Spinner<Integer> sessizSaatBitisSaatSpinner;
    @FXML private Spinner<Integer> sessizSaatBitisDakikaSpinner;

    @FXML private Button veriDisaAktarButton;
    @FXML private Button veriIceAktarButton;
    @FXML private Button veriSifirlaButton;
    @FXML private CheckBox otomatikYedeklemeCheckBox;
    @FXML private ComboBox<String> otomatikYedeklemeAralikComboBox;

    private static final String ACTIVE_CATEGORY_STYLE = "settings-category-active";

    private final SettingsManager settingsManager = new SettingsManager();
    private final ToggleGroup temaToggleGroup = new ToggleGroup();
    private final Map<String, Button> kategoriButonlari = new LinkedHashMap<>();
    private final Map<String, VBox> sectionPanelleri = new LinkedHashMap<>();
    private final Map<String, SettingsSection> sectionlar = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        sectionKayitlariniHazirla();
        sectionYukleriniAl();
        kategoriGoster(AppearanceSection.SECTION_ID);
    }

    @FXML
    public void gorunumuGoster() {
        kategoriGoster(AppearanceSection.SECTION_ID);
    }

    @FXML
    public void pomodoroyuGoster() {
        kategoriGoster(PomodoroSection.SECTION_ID);
    }

    @FXML
    public void bildirimleriGoster() {
        kategoriGoster(NotificationsSection.SECTION_ID);
    }

    @FXML
    public void veriyiGoster() {
        kategoriGoster(DataSection.SECTION_ID);
    }

    @FXML
    public void guncellemeleriGoster() {
        kategoriGoster(UpdatesSection.SECTION_ID);
    }

    @FXML
    public void kaydetVeKapat() {
        for (SettingsSection section : sectionlar.values()) {
            section.save(settingsManager);
        }
        pencereyiKapat();
    }

    @FXML
    public void iptalEt() {
        for (SettingsSection section : sectionlar.values()) {
            section.rollback();
        }
        pencereyiKapat();
    }

    private void sectionKayitlariniHazirla() {
        sectionKaydet(new AppearanceSection(
                temaToggleGroup,
                temaAcikRadio,
                temaKoyuRadio,
                temaSistemRadio,
                dilComboBox,
                this::dilYenidenBaslatUyarisiniGoster
        ), gorunumKategoriButton, gorunumSection);
        sectionKaydet(new PomodoroSection(
                pomodoroCalismaSpinner,
                pomodoroKisaMolaSpinner,
                pomodoroUzunMolaSpinner,
                pomodoroUzunMolaAraligiSpinner,
                pomodoroSesBildirimCheckBox
        ), pomodoroKategoriButton, pomodoroSection);
        sectionKaydet(new NotificationsSection(
                bildirimMasaustuCheckBox,
                gorevHatirlaticisiCheckBox,
                bildirimSesiCheckBox,
                sessizSaatlerCheckBox,
                sessizSaatBaslangicSaatSpinner,
                sessizSaatBaslangicDakikaSpinner,
                sessizSaatBitisSaatSpinner,
                sessizSaatBitisDakikaSpinner
        ), bildirimlerKategoriButton, bildirimlerSection);
        sectionKaydet(new DataSection(
                veriDisaAktarButton,
                veriIceAktarButton,
                veriSifirlaButton,
                otomatikYedeklemeCheckBox,
                otomatikYedeklemeAralikComboBox,
                settingsRoot
        ), veriKategoriButton, veriSection);
        sectionKaydet(new UpdatesSection(), guncellemelerKategoriButton, guncellemelerSection);
    }

    private void sectionKaydet(SettingsSection section, Button kategoriButton, VBox panel) {
        String sectionId = section.getSectionId();
        sectionlar.put(sectionId, section);
        kategoriButonlari.put(sectionId, kategoriButton);
        sectionPanelleri.put(sectionId, panel);
    }

    private void sectionYukleriniAl() {
        for (SettingsSection section : sectionlar.values()) {
            section.load(settingsManager);
        }
    }

    private void kategoriGoster(String sectionId) {
        if (!sectionlar.containsKey(sectionId)) {
            return;
        }

        for (Map.Entry<String, Button> entry : kategoriButonlari.entrySet()) {
            Button button = entry.getValue();
            button.getStyleClass().remove(ACTIVE_CATEGORY_STYLE);
            if (entry.getKey().equals(sectionId)) {
                button.getStyleClass().add(ACTIVE_CATEGORY_STYLE);
            }
        }

        for (Map.Entry<String, VBox> entry : sectionPanelleri.entrySet()) {
            boolean aktif = entry.getKey().equals(sectionId);
            VBox panel = entry.getValue();
            panel.setVisible(aktif);
            panel.setManaged(aktif);
        }
    }

    private void pencereyiKapat() {
        Stage stage = (Stage) settingsRoot.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void dilYenidenBaslatUyarisiniGoster() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dil Ayarı");
        alert.setHeaderText("Uygulama yeniden başlatılmalı");
        alert.setContentText("Dil değişikliği uygulamayı yeniden başlatınca geçerli olur.");
        if (settingsRoot.getScene() != null && settingsRoot.getScene().getWindow() != null) {
            alert.initOwner(settingsRoot.getScene().getWindow());
        }
        alert.showAndWait();
    }
}

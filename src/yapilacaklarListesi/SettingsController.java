package yapilacaklarListesi;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

    private static final String ACTIVE_CATEGORY_STYLE = "settings-category-active";

    private final SettingsManager settingsManager = new SettingsManager();
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
        sectionKaydet(new AppearanceSection(), gorunumKategoriButton, gorunumSection);
        sectionKaydet(new PomodoroSection(), pomodoroKategoriButton, pomodoroSection);
        sectionKaydet(new NotificationsSection(), bildirimlerKategoriButton, bildirimlerSection);
        sectionKaydet(new DataSection(), veriKategoriButton, veriSection);
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
}

package yapilacaklarListesi;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

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

    @FXML
    public void initialize() {
        kategoriGoster(gorunumKategoriButton, gorunumSection);
    }

    @FXML
    public void gorunumuGoster() {
        kategoriGoster(gorunumKategoriButton, gorunumSection);
    }

    @FXML
    public void pomodoroyuGoster() {
        kategoriGoster(pomodoroKategoriButton, pomodoroSection);
    }

    @FXML
    public void bildirimleriGoster() {
        kategoriGoster(bildirimlerKategoriButton, bildirimlerSection);
    }

    @FXML
    public void veriyiGoster() {
        kategoriGoster(veriKategoriButton, veriSection);
    }

    @FXML
    public void guncellemeleriGoster() {
        kategoriGoster(guncellemelerKategoriButton, guncellemelerSection);
    }

    @FXML
    public void kaydetVeKapat() {
        pencereyiKapat();
    }

    @FXML
    public void iptalEt() {
        pencereyiKapat();
    }

    private void kategoriGoster(Button seciliButton, VBox seciliSection) {
        for (Button button : tumKategoriButonlari()) {
            button.getStyleClass().remove(ACTIVE_CATEGORY_STYLE);
        }
        if (!seciliButton.getStyleClass().contains(ACTIVE_CATEGORY_STYLE)) {
            seciliButton.getStyleClass().add(ACTIVE_CATEGORY_STYLE);
        }

        for (VBox section : tumSectionlar()) {
            boolean aktif = section == seciliSection;
            section.setVisible(aktif);
            section.setManaged(aktif);
        }
    }

    private List<Button> tumKategoriButonlari() {
        return List.of(
                gorunumKategoriButton,
                pomodoroKategoriButton,
                bildirimlerKategoriButton,
                veriKategoriButton,
                guncellemelerKategoriButton
        );
    }

    private List<VBox> tumSectionlar() {
        return List.of(
                gorunumSection,
                pomodoroSection,
                bildirimlerSection,
                veriSection,
                guncellemelerSection
        );
    }

    private void pencereyiKapat() {
        Stage stage = (Stage) settingsRoot.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}

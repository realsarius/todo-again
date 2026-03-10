package yapilacaklarListesi.settings.sections;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import yapilacaklarListesi.settings.SettingsManager;
import yapilacaklarListesi.settings.UpdateChecker;
import yapilacaklarListesi.settings.VersionResolver;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

public class UpdatesSection extends BaseSettingsSection {

    public static final String SECTION_ID = "updates";

    private final Label mevcutSurumLabel;
    private final Label guncellemeDurumLabel;
    private final Hyperlink indirLinki;
    private final Button kontrolButton;
    private final CheckBox otomatikKontrolCheckBox;

    private boolean ilkOtomatikKontrol;
    private String mevcutSurum;
    private String sonIndirmeUrl;
    private boolean modelYukleniyor;

    public UpdatesSection(
            Label mevcutSurumLabel,
            Label guncellemeDurumLabel,
            Hyperlink indirLinki,
            Button kontrolButton,
            CheckBox otomatikKontrolCheckBox
    ) {
        super(SECTION_ID);
        this.mevcutSurumLabel = mevcutSurumLabel;
        this.guncellemeDurumLabel = guncellemeDurumLabel;
        this.indirLinki = indirLinki;
        this.kontrolButton = kontrolButton;
        this.otomatikKontrolCheckBox = otomatikKontrolCheckBox;

        this.indirLinki.setVisible(false);
        this.indirLinki.setManaged(false);
        this.indirLinki.setOnAction(event -> indirLinkiniAc());

        this.kontrolButton.setOnAction(event -> guncellemeleriKontrolEt());
        this.otomatikKontrolCheckBox.selectedProperty().addListener((obs, eski, yeni) -> {
            if (modelYukleniyor || (eski != null && eski.equals(yeni))) {
                return;
            }
            markDirty();
        });
    }

    @Override
    public void load(SettingsManager manager) {
        modelYukleniyor = true;
        try {
            mevcutSurum = VersionResolver.resolveCurrentVersion();
            mevcutSurumLabel.setText(mevcutSurum);
            ilkOtomatikKontrol = manager.isAutoUpdateCheckEnabled();
            otomatikKontrolCheckBox.setSelected(ilkOtomatikKontrol);
            guncellemeDurumunuYaz("Henüz kontrol edilmedi.", "settings-status-info");
            indirLinkiniGizle();
            clearDirty();
        } finally {
            modelYukleniyor = false;
        }
    }

    @Override
    public void save(SettingsManager manager) {
        manager.setAutoUpdateCheckEnabled(otomatikKontrolCheckBox.isSelected());
        ilkOtomatikKontrol = manager.isAutoUpdateCheckEnabled();
        clearDirty();
    }

    @Override
    public void rollback() {
        modelYukleniyor = true;
        try {
            otomatikKontrolCheckBox.setSelected(ilkOtomatikKontrol);
            clearDirty();
        } finally {
            modelYukleniyor = false;
        }
    }

    private void guncellemeleriKontrolEt() {
        kontrolButton.setDisable(true);
        guncellemeDurumunuYaz("Güncellemeler kontrol ediliyor...", "settings-status-info");
        indirLinkiniGizle();

        UpdateChecker.latestRelease(UpdateChecker.DEFAULT_GITHUB_OWNER, UpdateChecker.DEFAULT_GITHUB_REPO)
                .whenComplete((result, throwable) -> Platform.runLater(() -> {
                    kontrolButton.setDisable(false);
                    if (throwable != null) {
                        guncellemeDurumunuYaz("Kontrol sırasında bir hata oluştu.", "settings-status-error");
                        return;
                    }
                    if (result == null || !result.success()) {
                        String hata = result == null ? "Güncelleme bilgisi alınamadı." : result.errorMessage();
                        guncellemeDurumunuYaz(hata, "settings-status-error");
                        return;
                    }

                    SettingsManager manager = new SettingsManager();
                    manager.setLastUpdateCheckEpoch(System.currentTimeMillis());

                    UpdateChecker.ReleaseInfo releaseInfo = result.releaseInfo();
                    int karsilastirma = UpdateChecker.compareVersions(mevcutSurum, releaseInfo.tagName());
                    if (karsilastirma < 0) {
                        sonIndirmeUrl = releaseInfo.htmlUrl();
                        guncellemeDurumunuYaz(releaseInfo.tagName() + " mevcut — İndir", "settings-status-warning");
                        indirLinkiniGoster();
                        return;
                    }
                    sonIndirmeUrl = null;
                    guncellemeDurumunuYaz("En güncel sürümdesiniz ✓", "settings-status-ok");
                    indirLinkiniGizle();
                }));
    }

    private void indirLinkiniAc() {
        if (sonIndirmeUrl == null || sonIndirmeUrl.isBlank()) {
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            guncellemeDurumunuYaz("Tarayıcı açılamıyor. URL: " + sonIndirmeUrl, "settings-status-error");
            return;
        }
        try {
            Desktop.getDesktop().browse(URI.create(sonIndirmeUrl));
        } catch (IOException e) {
            guncellemeDurumunuYaz("İndirme sayfası açılamadı.", "settings-status-error");
        }
    }

    private void guncellemeDurumunuYaz(String mesaj, String styleClass) {
        guncellemeDurumLabel.setText(Objects.requireNonNullElse(mesaj, ""));
        guncellemeDurumLabel.getStyleClass().removeAll(
                "settings-status-info",
                "settings-status-ok",
                "settings-status-warning",
                "settings-status-error"
        );
        guncellemeDurumLabel.getStyleClass().add(styleClass);
    }

    private void indirLinkiniGoster() {
        indirLinki.setText("İndir");
        indirLinki.setVisible(true);
        indirLinki.setManaged(true);
    }

    private void indirLinkiniGizle() {
        indirLinki.setVisible(false);
        indirLinki.setManaged(false);
    }

}

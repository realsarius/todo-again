package yapilacaklarListesi.settings.sections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import yapilacaklarListesi.settings.SettingsManager;
import yapilacaklarListesi.veriler.Oncelik;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class DataSection extends BaseSettingsSection {

    public static final String SECTION_ID = "data";
    private static final String ARALIK_GUNLUK = "Her gün";
    private static final String ARALIK_HAFTALIK = "Her hafta";

    private final Button disaAktarButton;
    private final Button iceAktarButton;
    private final Button sifirlaButton;
    private final CheckBox otomatikYedeklemeCheckBox;
    private final ComboBox<String> otomatikYedeklemeAralikComboBox;
    private final VBox settingsRoot;
    private final Gson gson;

    private boolean ilkOtomatikYedekleme;
    private SettingsManager.BackupInterval ilkAralik;
    private boolean modelYukleniyor;

    public DataSection(
            Button disaAktarButton,
            Button iceAktarButton,
            Button sifirlaButton,
            CheckBox otomatikYedeklemeCheckBox,
            ComboBox<String> otomatikYedeklemeAralikComboBox,
            VBox settingsRoot
    ) {
        super(SECTION_ID);
        this.disaAktarButton = disaAktarButton;
        this.iceAktarButton = iceAktarButton;
        this.sifirlaButton = sifirlaButton;
        this.otomatikYedeklemeCheckBox = otomatikYedeklemeCheckBox;
        this.otomatikYedeklemeAralikComboBox = otomatikYedeklemeAralikComboBox;
        this.settingsRoot = settingsRoot;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        this.otomatikYedeklemeAralikComboBox.getItems().setAll(ARALIK_GUNLUK, ARALIK_HAFTALIK);

        this.otomatikYedeklemeCheckBox.selectedProperty().addListener((obs, eski, yeni) -> {
            if (modelYukleniyor || (eski != null && eski.equals(yeni))) {
                return;
            }
            aralikKontrolunuGuncelle();
            markDirty();
        });
        this.otomatikYedeklemeAralikComboBox.valueProperty().addListener((obs, eski, yeni) -> {
            if (modelYukleniyor || yeni == null || yeni.equals(eski)) {
                return;
            }
            markDirty();
        });

        this.disaAktarButton.setOnAction(event -> disaAktar());
        this.iceAktarButton.setOnAction(event -> iceAktar());
        this.sifirlaButton.setOnAction(event -> tumVeriyiSifirla());
    }

    @Override
    public void load(SettingsManager manager) {
        modelYukleniyor = true;
        try {
            ilkOtomatikYedekleme = manager.isAutoBackupEnabled();
            ilkAralik = manager.getAutoBackupInterval();

            otomatikYedeklemeCheckBox.setSelected(ilkOtomatikYedekleme);
            otomatikYedeklemeAralikComboBox.setValue(aralikLabeli(ilkAralik));
            aralikKontrolunuGuncelle();
            clearDirty();
        } finally {
            modelYukleniyor = false;
        }
    }

    @Override
    public void save(SettingsManager manager) {
        manager.setAutoBackupEnabled(otomatikYedeklemeCheckBox.isSelected());
        manager.setAutoBackupInterval(seciliAralik());

        ilkOtomatikYedekleme = manager.isAutoBackupEnabled();
        ilkAralik = manager.getAutoBackupInterval();
        clearDirty();
    }

    @Override
    public void rollback() {
        modelYukleniyor = true;
        try {
            otomatikYedeklemeCheckBox.setSelected(ilkOtomatikYedekleme);
            otomatikYedeklemeAralikComboBox.setValue(aralikLabeli(ilkAralik));
            aralikKontrolunuGuncelle();
            clearDirty();
        } finally {
            modelYukleniyor = false;
        }
    }

    private void disaAktar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Veriyi JSON Olarak Dışa Aktar");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Dosyası", "*.json"));
        chooser.setInitialFileName("Yapilacaklar-export.json");

        Window owner = pencereSahibi();
        java.io.File secilenDosya = chooser.showSaveDialog(owner);
        if (secilenDosya == null) {
            return;
        }

        ExportPayload payload = exportPayloadOlustur(YapilacakVeri.getInstance().getYapilacaklar());
        try (Writer writer = Files.newBufferedWriter(secilenDosya.toPath(), StandardCharsets.UTF_8)) {
            gson.toJson(payload, writer);
            bilgiMesaji("Dışa aktarma tamamlandı", "Görevler JSON dosyasına kaydedildi.");
        } catch (IOException e) {
            hataMesaji("Dışa aktarma başarısız", "Dosya yazılırken bir hata oluştu.");
        }
    }

    private void iceAktar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("JSON Dosyasından İçe Aktar");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Dosyası", "*.json"));

        Window owner = pencereSahibi();
        java.io.File secilenDosya = chooser.showOpenDialog(owner);
        if (secilenDosya == null) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(secilenDosya.toPath(), StandardCharsets.UTF_8)) {
            ExportPayload payload = gson.fromJson(reader, ExportPayload.class);
            MergeResult sonuc = importVeBirlestir(payload);
            if (sonuc.eklenen > 0 || sonuc.guncellenen > 0) {
                YapilacakVeri.getInstance().yapilacaklariKaydet();
            }
            bilgiMesaji(
                    "İçe aktarma tamamlandı",
                    String.format(Locale.ROOT, "%d eklendi, %d güncellendi, %d atlandı.",
                            sonuc.eklenen, sonuc.guncellenen, sonuc.atlanan)
            );
        } catch (JsonParseException | IOException e) {
            hataMesaji("İçe aktarma başarısız", "Dosya okunurken veya işlenirken bir hata oluştu.");
        }
    }

    private MergeResult importVeBirlestir(ExportPayload payload) {
        MergeResult result = new MergeResult();
        if (payload == null || payload.tasks == null || payload.tasks.isEmpty()) {
            return result;
        }

        ObservableList<Yapilacak> mevcutGorevler = YapilacakVeri.getInstance().getYapilacaklar();
        for (TaskRecord kayit : payload.tasks) {
            Yapilacak aday = kayittanYapilacak(kayit);
            if (aday == null) {
                result.atlanan++;
                continue;
            }

            int idIndex = idIleIndexBul(mevcutGorevler, aday.getId());
            if (idIndex >= 0) {
                Yapilacak mevcut = mevcutGorevler.get(idIndex);
                if (dahaYeniMi(aday, mevcut)) {
                    mevcutGorevler.set(idIndex, aday);
                    result.guncellenen++;
                } else {
                    result.atlanan++;
                }
                continue;
            }

            int keyIndex = metinTarihAnahtariIleIndexBul(mevcutGorevler, aday);
            if (keyIndex >= 0) {
                Yapilacak mevcut = mevcutGorevler.get(keyIndex);
                if (dahaYeniMi(aday, mevcut)) {
                    mevcutGorevler.set(keyIndex, aday);
                    result.guncellenen++;
                } else {
                    result.atlanan++;
                }
                continue;
            }

            mevcutGorevler.add(aday);
            result.eklenen++;
        }
        return result;
    }

    private boolean dahaYeniMi(Yapilacak aday, Yapilacak mevcut) {
        return aday.getUpdatedAt().isAfter(mevcut.getUpdatedAt());
    }

    private int idIleIndexBul(List<Yapilacak> gorevler, String id) {
        if (id == null || id.isBlank()) {
            return -1;
        }
        for (int i = 0; i < gorevler.size(); i++) {
            if (id.equals(gorevler.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private int metinTarihAnahtariIleIndexBul(List<Yapilacak> gorevler, Yapilacak aday) {
        String adayAnahtar = metinTarihAnahtari(aday.getAciklama(), aday.getTarih());
        for (int i = 0; i < gorevler.size(); i++) {
            Yapilacak gorev = gorevler.get(i);
            if (adayAnahtar.equals(metinTarihAnahtari(gorev.getAciklama(), gorev.getTarih()))) {
                return i;
            }
        }
        return -1;
    }

    private String metinTarihAnahtari(String aciklama, LocalDate tarih) {
        String temizAciklama = aciklama == null ? "" : aciklama.trim().toLowerCase(Locale.ROOT);
        String tarihMetni = tarih == null ? "" : tarih.toString();
        return temizAciklama + "|" + tarihMetni;
    }

    private Yapilacak kayittanYapilacak(TaskRecord kayit) {
        if (kayit == null || kayit.aciklama == null || kayit.aciklama.isBlank() || kayit.tarih == null) {
            return null;
        }
        LocalDate tarih;
        try {
            tarih = LocalDate.parse(kayit.tarih);
        } catch (DateTimeParseException e) {
            return null;
        }

        String id = (kayit.id == null || kayit.id.isBlank()) ? UUID.randomUUID().toString() : kayit.id;
        String detay = kayit.detay == null ? "" : kayit.detay;
        Instant createdAt = parseInstant(kayit.createdAt, Instant.now());
        Instant updatedAt = parseInstant(kayit.updatedAt, createdAt);
        Oncelik oncelik = parseOncelik(kayit.oncelik);
        List<String> tags = temizTagler(kayit.tags);

        return new Yapilacak(id, kayit.aciklama.trim(), detay, tarih, createdAt, updatedAt, oncelik, tags);
    }

    private Instant parseInstant(String value, Instant fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }

    private Oncelik parseOncelik(String value) {
        if (value == null || value.isBlank()) {
            return Oncelik.MEDIUM;
        }
        try {
            return Oncelik.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Oncelik.MEDIUM;
        }
    }

    private List<String> temizTagler(List<String> tags) {
        List<String> temiz = new ArrayList<>();
        if (tags == null) {
            return temiz;
        }
        for (String tag : tags) {
            if (tag == null) {
                continue;
            }
            String deger = tag.trim();
            if (!deger.isEmpty()) {
                temiz.add(deger);
            }
        }
        return temiz;
    }

    private ExportPayload exportPayloadOlustur(List<Yapilacak> gorevler) {
        ExportPayload payload = new ExportPayload();
        payload.version = 1;
        payload.tasks = new ArrayList<>();
        for (Yapilacak gorev : gorevler) {
            TaskRecord kayit = new TaskRecord();
            kayit.id = gorev.getId();
            kayit.aciklama = gorev.getAciklama();
            kayit.detay = gorev.getDetay();
            kayit.tarih = gorev.getTarih() == null ? null : gorev.getTarih().toString();
            kayit.createdAt = gorev.getCreatedAt() == null ? null : gorev.getCreatedAt().toString();
            kayit.updatedAt = gorev.getUpdatedAt() == null ? null : gorev.getUpdatedAt().toString();
            kayit.oncelik = gorev.getOncelik() == null ? null : gorev.getOncelik().name();
            kayit.tags = gorev.getTags();
            payload.tasks.add(kayit);
        }
        return payload;
    }

    private void tumVeriyiSifirla() {
        Alert onay = new Alert(Alert.AlertType.CONFIRMATION);
        onay.setTitle("Tüm Verileri Sıfırla");
        onay.setHeaderText("Bu işlem geri alınamaz");
        onay.setContentText("Tüm görevleri silmek istediğinizden emin misiniz?");
        Window owner = pencereSahibi();
        if (owner != null) {
            onay.initOwner(owner);
        }
        Optional<ButtonType> cevap = onay.showAndWait();
        if (cevap.isEmpty() || cevap.get() != ButtonType.OK) {
            return;
        }

        try {
            YapilacakVeri.getInstance().getYapilacaklar().clear();
            YapilacakVeri.getInstance().yapilacaklariKaydet();
            bilgiMesaji("Sıfırlama tamamlandı", "Tüm görevler temizlendi.");
        } catch (IOException e) {
            hataMesaji("Sıfırlama başarısız", "Veriler temizlenirken hata oluştu.");
        }
    }

    private void aralikKontrolunuGuncelle() {
        boolean etkin = otomatikYedeklemeCheckBox.isSelected();
        otomatikYedeklemeAralikComboBox.setDisable(!etkin);
    }

    private SettingsManager.BackupInterval seciliAralik() {
        String secim = otomatikYedeklemeAralikComboBox.getValue();
        if (ARALIK_HAFTALIK.equals(secim)) {
            return SettingsManager.BackupInterval.WEEKLY;
        }
        return SettingsManager.BackupInterval.DAILY;
    }

    private String aralikLabeli(SettingsManager.BackupInterval interval) {
        if (interval == SettingsManager.BackupInterval.WEEKLY) {
            return ARALIK_HAFTALIK;
        }
        return ARALIK_GUNLUK;
    }

    private Window pencereSahibi() {
        if (settingsRoot.getScene() == null) {
            return null;
        }
        return settingsRoot.getScene().getWindow();
    }

    private void bilgiMesaji(String baslik, String icerik) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Veri");
        alert.setHeaderText(baslik);
        alert.setContentText(icerik);
        Window owner = pencereSahibi();
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    private void hataMesaji(String baslik, String icerik) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Veri");
        alert.setHeaderText(baslik);
        alert.setContentText(icerik);
        Window owner = pencereSahibi();
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    private static class ExportPayload {
        int version;
        List<TaskRecord> tasks;
    }

    private static class TaskRecord {
        String id;
        String aciklama;
        String detay;
        String tarih;
        String createdAt;
        String updatedAt;
        String oncelik;
        List<String> tags;
    }

    private static class MergeResult {
        int eklenen;
        int guncellenen;
        int atlanan;
    }
}

package yapilacaklarListesi;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import yapilacaklarListesi.veriler.Oncelik;
import yapilacaklarListesi.veriler.Yapilacak;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DialogController {

    @FXML private TextField aciklamaFXML;
    @FXML private TextArea detayFXML;
    @FXML private DatePicker tarihFXML;
    @FXML private ChoiceBox<Oncelik> oncelikFXML;
    @FXML private TextField taglerFXML;
    @FXML private CheckBox tumGunFXML;
    @FXML private ComboBox<String> baslangicSaatFXML;
    @FXML private ComboBox<String> bitisSaatFXML;

    private static final DateTimeFormatter SAAT_FORMATI = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        oncelikFXML.getItems().setAll(Oncelik.values());
        oncelikFXML.setValue(Oncelik.MEDIUM);
        saatSecenekleriniHazirla();
        tumGunFXML.setSelected(true);
        saatAlanlariniGuncelle(true);
        tumGunFXML.selectedProperty().addListener((obs, eski, yeni) -> saatAlanlariniGuncelle(yeni));
        baslangicSaatFXML.valueProperty().addListener((obs, eski, yeni) -> baslangicSaatiniOtomatikTamamla(yeni));
    }

    public void varsayilanTarihAyarla(LocalDate varsayilanTarih) {
        if (varsayilanTarih == null) {
            return;
        }
        tarihFXML.setValue(varsayilanTarih);
    }

    public void varsayilanTarihVeSaatAyarla(LocalDate varsayilanTarih, LocalTime varsayilanSaat) {
        varsayilanTarihAyarla(varsayilanTarih);
        if (varsayilanSaat == null) {
            tumGunFXML.setSelected(true);
            baslangicSaatFXML.setValue(null);
            bitisSaatFXML.setValue(null);
            return;
        }
        tumGunFXML.setSelected(false);
        baslangicSaatFXML.setValue(varsayilanSaat.format(SAAT_FORMATI));
        bitisSaatFXML.setValue(saatMetniniFormatla(varsayilanBitisSaati(varsayilanSaat)));
    }

    // FXML'deki aciklamaFXML, detayFXML, tarihFXML'in içeriklerini alıp yeni bir Yapilacak sınıfı oluşturarak return ediyoruz ki yeni Yapilacagi text dökümantasyonuna yazdırabilelim.
    public Yapilacak ciktiyiGoster(){
        String aciklama = this.aciklamaFXML.getText().trim();
        String detay = this.detayFXML.getText().trim();
        LocalDate tarih = tarihFXML.getValue();
        if (aciklama.isEmpty() || tarih == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Eksik Alan");
            alert.setHeaderText("Yapilacak olusturulamadi");
            alert.setContentText("Aciklama ve tarih alanlari zorunludur.");
            alert.showAndWait();
            return null;
        }

        Yapilacak yeni = new Yapilacak(aciklama, detay, tarih);
        yeni.setOncelik(oncelikFXML.getValue() == null ? Oncelik.MEDIUM : oncelikFXML.getValue());
        yeni.setTags(tagleriAyrisitir(taglerFXML.getText()));
        boolean tumGun = tumGunFXML.isSelected();
        LocalTime baslangic = saatMetniniParseEt(baslangicSaatFXML.getValue());
        LocalTime bitis = saatMetniniParseEt(bitisSaatFXML.getValue());

        if (!tumGun && baslangic == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Eksik Saat");
            alert.setHeaderText("Başlangıç saati gerekli");
            alert.setContentText("Tüm gün kapalıyken başlangıç saati seçmelisiniz.");
            alert.showAndWait();
            return null;
        }
        if (!tumGun && bitis != null && bitis.isBefore(baslangic)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Geçersiz Saat Aralığı");
            alert.setHeaderText("Bitiş saati başlangıçtan önce olamaz");
            alert.setContentText("Lütfen geçerli bir saat aralığı seçin.");
            alert.showAndWait();
            return null;
        }

        yeni.setTimeRange(tumGun, tumGun ? null : baslangic, tumGun ? null : bitis);
        return yeni;
    }

    private void saatSecenekleriniHazirla() {
        List<String> saatSecenekleri = new ArrayList<>();
        for (int saat = 0; saat < 24; saat++) {
            for (int dakika = 0; dakika < 60; dakika += 15) {
                saatSecenekleri.add(String.format("%02d:%02d", saat, dakika));
            }
        }
        baslangicSaatFXML.getItems().setAll(saatSecenekleri);
        bitisSaatFXML.getItems().setAll(saatSecenekleri);
    }

    private void saatAlanlariniGuncelle(boolean tumGunSecili) {
        baslangicSaatFXML.setDisable(tumGunSecili);
        bitisSaatFXML.setDisable(tumGunSecili);
    }

    private void baslangicSaatiniOtomatikTamamla(String yeniBaslangicMetni) {
        if (tumGunFXML.isSelected()) {
            return;
        }
        LocalTime baslangic = saatMetniniParseEt(yeniBaslangicMetni);
        if (baslangic == null) {
            return;
        }

        LocalTime bitis = saatMetniniParseEt(bitisSaatFXML.getValue());
        if (bitis == null || !bitis.isAfter(baslangic)) {
            bitisSaatFXML.setValue(saatMetniniFormatla(varsayilanBitisSaati(baslangic)));
        }
    }

    private LocalTime varsayilanBitisSaati(LocalTime baslangic) {
        if (baslangic == null) {
            return null;
        }
        LocalTime birSaatSonra = baslangic.plusHours(1);
        if (!birSaatSonra.isAfter(baslangic)) {
            return baslangic;
        }
        return birSaatSonra;
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

    private List<String> tagleriAyrisitir(String hamTagler) {
        if (hamTagler == null || hamTagler.isBlank()) {
            return List.of();
        }
        return Arrays.stream(hamTagler.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}

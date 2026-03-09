package yapilacaklarListesi;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import yapilacaklarListesi.veriler.Oncelik;
import yapilacaklarListesi.veriler.Yapilacak;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DialogController {

    @FXML private TextField aciklamaFXML;
    @FXML private TextArea detayFXML;
    @FXML private DatePicker tarihFXML;
    @FXML private ChoiceBox<Oncelik> oncelikFXML;
    @FXML private TextField taglerFXML;

    @FXML
    public void initialize() {
        oncelikFXML.getItems().setAll(Oncelik.values());
        oncelikFXML.setValue(Oncelik.MEDIUM);
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
        return yeni;
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

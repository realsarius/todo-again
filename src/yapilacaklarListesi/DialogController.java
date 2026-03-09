package yapilacaklarListesi;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import yapilacaklarListesi.veriler.Yapilacak;
import java.time.LocalDate;

public class DialogController {

    @FXML private TextField aciklamaFXML;
    @FXML private TextArea detayFXML;
    @FXML private DatePicker tarihFXML;

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

        return new Yapilacak(aciklama, detay, tarih);
    }
}

package yapilacaklarListesi;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;
import java.time.LocalDate;

public class DialogController {

    @FXML private TextField aciklamaFXML;
    @FXML private TextArea detayFXML;
    @FXML private DatePicker tarihFXML;

    public Yapilacak ciktiyiGoster(){
        String aciklama = this.aciklamaFXML.getText().trim();
        String detay = this.detayFXML.getText().trim();
        LocalDate tarih = tarihFXML.getValue();

        Yapilacak yeniYapilacak = new Yapilacak(aciklama, detay, tarih);
        YapilacakVeri.getInstance().yapilacakEkle(yeniYapilacak);

        return yeniYapilacak;
    }
}

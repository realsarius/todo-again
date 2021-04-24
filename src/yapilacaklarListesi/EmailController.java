package yapilacaklarListesi;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import yapilacaklarListesi.email.SendEmail;

public class EmailController {

    @FXML private TextField emailSifreFXML;
    @FXML private TextField emailAdresiFXML;
    @FXML private TextField emailBasligiFXML;
    @FXML private TextArea emailIcerikFXML;

    public SendEmail emailGonder(){
        String emailBasligi = this.emailBasligiFXML.getText().trim();
        String emailIcerik = this.emailAdresiFXML.getText().trim();
        String email = emailIcerikFXML.getText().trim();
        String emailSifre = emailSifreFXML.getText().trim();

        return new SendEmail(emailBasligi, emailIcerik, email, emailSifre);
    }

}

package yapilacaklarListesi.muzik;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class OKMuzik implements Muzik{

    private String okButonMuzik;

    public OKMuzik(){
        this.okButonMuzik = "dialogOkButon.mp3";
    }

    @Override
    public void oynat() {
        Media sound = new Media(new File(this.okButonMuzik).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }
}

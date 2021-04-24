package yapilacaklarListesi.muzik;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class CANCELMuzik implements Muzik{

    private String cancelMuzik;

    public CANCELMuzik(){
        this.cancelMuzik = "cancelButon.mp3";
    }

    @Override
    public void oynat() {
        Media sound = new Media(new File(this.cancelMuzik).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }
}

package yapilacaklarListesi.muzik;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class CANCELMuzik implements Muzik{

    private final String cancelMuzik;

    public CANCELMuzik(){
        this.cancelMuzik = "cancelButon.mp3";
    }

    @Override
    public void oynat() {
        var resource = getClass().getResource("/" + this.cancelMuzik);
        if (resource == null) {
            return;
        }
        Media sound = new Media(resource.toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }
}

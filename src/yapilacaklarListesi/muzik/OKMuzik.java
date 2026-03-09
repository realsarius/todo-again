package yapilacaklarListesi.muzik;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class OKMuzik implements Muzik{

    private final String okButonMuzik;

    public OKMuzik(){
        this.okButonMuzik = "dialogOkButon.mp3";
    }

    @Override
    public void oynat() {
        var resource = getClass().getResource("/" + this.okButonMuzik);
        if (resource == null) {
            return;
        }
        Media sound = new Media(resource.toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }
}

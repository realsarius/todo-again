package yapilacaklarListesi.muzik;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class DialogMuzik implements Muzik {

    private final String dialogMuzikYol;

    public DialogMuzik(){
        this.dialogMuzikYol = "dialog.mp3";
    }

    @Override
    public void oynat() {
        var resource = getClass().getResource("/" + this.dialogMuzikYol);
        if (resource == null) {
            return;
        }
        Media sound = new Media(resource.toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }

}

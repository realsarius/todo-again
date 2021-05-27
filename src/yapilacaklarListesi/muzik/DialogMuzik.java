package yapilacaklarListesi.muzik;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class DialogMuzik implements Muzik {

    private String dialogMuzikYol;

    public DialogMuzik(){
        this.dialogMuzikYol = "dialog.mp3";
    }

    @Override
    public void oynat() {
        Media sound = new Media(new File(this.dialogMuzikYol).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }

}

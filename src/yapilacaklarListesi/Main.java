package yapilacaklarListesi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import yapilacaklarListesi.veriler.YapilacakVeri;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void stop() {
        try {
            YapilacakVeri.getInstance().yapilacaklariKaydet();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        try {
            YapilacakVeri.getInstance().yapilacaklariCagir();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("test.fxml")));
        primaryStage.setTitle("Yapılacaklar Listesi");
        primaryStage.setScene(new Scene(root, 960, 550));
        primaryStage.getIcons().add(new Image("images/files.png"));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }


}

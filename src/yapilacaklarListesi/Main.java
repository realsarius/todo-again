package yapilacaklarListesi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import yapilacaklarListesi.veriler.YapilacakVeri;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class Main extends Application {

    private TrayIcon trayIcon;
    private boolean trayReady;
    private boolean trayInfoShown;

    @Override
    public void stop() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
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
        Scene scene = new Scene(root, 960, 550);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("app.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("dark-mode.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(580);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/files.png"))));

        trayReady = trayKur(primaryStage);
        if (trayReady) {
            Platform.setImplicitExit(false);
            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                pencereyiTepsiyeKucult(primaryStage);
            });
        }

        primaryStage.show();

    }

    private boolean trayKur(Stage primaryStage) {
        if (!SystemTray.isSupported()) {
            return false;
        }

        URL iconUrl = getClass().getResource("/images/files.png");
        if (iconUrl == null) {
            return false;
        }

        PopupMenu popup = new PopupMenu();
        MenuItem ac = new MenuItem("Uygulamayı Aç");
        ac.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.setIconified(false);
            primaryStage.toFront();
            primaryStage.requestFocus();
        }));

        MenuItem cikis = new MenuItem("Çıkış");
        cikis.addActionListener(e -> Platform.runLater(() -> {
            Platform.setImplicitExit(true);
            primaryStage.close();
            Platform.exit();
        }));

        popup.add(ac);
        popup.addSeparator();
        popup.add(cikis);

        java.awt.Image awtIcon = Toolkit.getDefaultToolkit().getImage(iconUrl);
        trayIcon = new TrayIcon(awtIcon, "Yapılacaklar Listesi", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.setIconified(false);
            primaryStage.toFront();
            primaryStage.requestFocus();
        }));

        try {
            SystemTray.getSystemTray().add(trayIcon);
            return true;
        } catch (AWTException e) {
            trayIcon = null;
            return false;
        }
    }

    private void pencereyiTepsiyeKucult(Stage primaryStage) {
        primaryStage.hide();
        if (!trayInfoShown && trayIcon != null) {
            trayIcon.displayMessage(
                    "Yapılacaklar Listesi",
                    "Uygulama tepsiye küçültüldü. Çıkış için tepsi menüsünü kullanabilirsiniz.",
                    TrayIcon.MessageType.INFO
            );
            trayInfoShown = true;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


}

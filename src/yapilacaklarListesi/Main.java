package yapilacaklarListesi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
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

    private static final String DARK_MODE_CLASS = "dark-mode";
    private static final String DARK_POPUP_CLASS = "dark-popup";
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
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main.fxml")));
        primaryStage.setTitle("Yapılacaklar Listesi");
        Scene scene = new Scene(root, 960, 550);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("app.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("dark-mode.css")).toExternalForm());
        primaryStage.setScene(scene);
        popupStilKoprusuKur(scene, root);
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

    private void popupStilKoprusuKur(Scene anaScene, Parent anaRoot) {
        ListChangeListener<Window> popupListener = degisim -> {
            while (degisim.next()) {
                for (Window pencere : degisim.getAddedSubList()) {
                    popupStiliniUygula(pencere, anaScene, anaRoot);
                }
            }
        };
        Window.getWindows().addListener(popupListener);

        anaRoot.getStyleClass().addListener((ListChangeListener<String>) degisim ->
                Window.getWindows().forEach(pencere -> popupStiliniUygula(pencere, anaScene, anaRoot))
        );

        Window.getWindows().forEach(pencere -> popupStiliniUygula(pencere, anaScene, anaRoot));
    }

    private void popupStiliniUygula(Window pencere, Scene anaScene, Parent anaRoot) {
        if (!(pencere instanceof PopupWindow)) {
            return;
        }
        Scene popupScene = pencere.getScene();
        if (popupScene == null || popupScene == anaScene) {
            return;
        }

        for (String stilDosyasi : anaScene.getStylesheets()) {
            if (!popupScene.getStylesheets().contains(stilDosyasi)) {
                popupScene.getStylesheets().add(stilDosyasi);
            }
        }

        Parent popupRoot = popupScene.getRoot();
        if (popupRoot == null) {
            return;
        }

        boolean koyuTema = anaRoot.getStyleClass().contains(DARK_MODE_CLASS);
        if (koyuTema) {
            if (!popupRoot.getStyleClass().contains(DARK_POPUP_CLASS)) {
                popupRoot.getStyleClass().add(DARK_POPUP_CLASS);
            }
            return;
        }
        popupRoot.getStyleClass().remove(DARK_POPUP_CLASS);
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

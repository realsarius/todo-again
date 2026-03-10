package yapilacaklarListesi;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class MainController {

    private enum AppView {
        TODO,
        CALENDAR,
        STATS,
        SETTINGS
    }

    @FXML private StackPane contentHost;

    @FXML private Button navTodoButton;
    @FXML private Button navCalendarButton;
    @FXML private Button navStatsButton;
    @FXML private Button navSettingsButton;

    private final Map<AppView, Parent> viewCache = new EnumMap<>(AppView.class);
    private final Map<AppView, Button> navButtons = new EnumMap<>(AppView.class);
    private AppView currentView;

    @FXML
    public void initialize() {
        navButtons.put(AppView.TODO, navTodoButton);
        navButtons.put(AppView.CALENDAR, navCalendarButton);
        navButtons.put(AppView.STATS, navStatsButton);
        navButtons.put(AppView.SETTINGS, navSettingsButton);

        navTodoButton.setTooltip(new Tooltip("Yapılacaklar"));
        navCalendarButton.setTooltip(new Tooltip("Takvim"));
        navStatsButton.setTooltip(new Tooltip("İstatistikler"));
        navSettingsButton.setTooltip(new Tooltip("Ayarlar"));

        switchView(AppView.TODO);
    }

    @FXML
    public void showTodo() {
        switchView(AppView.TODO);
    }

    @FXML
    public void showCalendar() {
        switchView(AppView.CALENDAR);
    }

    @FXML
    public void showStats() {
        switchView(AppView.STATS);
    }

    @FXML
    public void showSettings() {
        switchView(AppView.SETTINGS);
    }

    private void switchView(AppView targetView) {
        if (targetView == currentView) {
            return;
        }

        Parent nextView;
        try {
            nextView = loadView(targetView);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        navSeciminiGuncelle(targetView);
        Parent currentNode = contentHost.getChildren().isEmpty() ? null : (Parent) contentHost.getChildren().get(0);

        if (currentNode == null) {
            nextView.setOpacity(0);
            contentHost.getChildren().setAll(nextView);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), nextView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            currentView = targetView;
            return;
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentNode);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            nextView.setOpacity(0);
            contentHost.getChildren().setAll(nextView);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), nextView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();

        currentView = targetView;
    }

    private void navSeciminiGuncelle(AppView aktifView) {
        navButtons.values().forEach(button -> button.getStyleClass().remove("nav-item-active"));
        Button aktifButton = navButtons.get(aktifView);
        if (aktifButton != null && !aktifButton.getStyleClass().contains("nav-item-active")) {
            aktifButton.getStyleClass().add("nav-item-active");
        }
    }

    private Parent loadView(AppView view) throws IOException {
        Parent cached = viewCache.get(view);
        if (cached != null) {
            return cached;
        }

        String fxml = switch (view) {
            case TODO -> "test.fxml";
            case CALENDAR -> "calendar-view.fxml";
            case STATS -> "stats-view.fxml";
            case SETTINGS -> "settings-view.fxml";
        };

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent loaded = loader.load();
        viewCache.put(view, loaded);
        return loaded;
    }
}

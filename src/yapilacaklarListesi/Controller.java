package yapilacaklarListesi;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXToggleButton;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import yapilacaklarListesi.pomodoro.model.Attempt;
import yapilacaklarListesi.pomodoro.model.AttemptKind;
import yapilacaklarListesi.muzik.MuzikOynatici;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    @FXML private MenuItem kaydetFXML;
    @FXML private MenuItem silFXML;
    @FXML private MenuItem kesFXML;
    @FXML private MenuItem kopyalaFXML;
    @FXML private MenuItem yapistirFXML;
    @FXML private JFXToggleButton pomodoroToggleButtonFXML;
    @FXML private VBox vbox;
    @FXML private JFXToggleButton bugunToggleButton;
    @FXML private JFXListView<Yapilacak> yapilacakListeFXML;
    @FXML private Label tarihLabel;
    @FXML private TextArea detayFXML;

    private FilteredList<Yapilacak> yapilacakFilteredList;
    private Predicate<Yapilacak> tumYapilacaklarPredicate;
    private Predicate<Yapilacak> bugunYapilacaklarPredicate;
    Clipboard systemClipboard = Clipboard.getSystemClipboard();


    private Attempt mCurrentAttempt;
    private final StringProperty mTimerText;
    private Timeline mTimeline;

    public Controller(){
        mTimerText = new SimpleStringProperty();
        setTimerText(0);
    }

    public void initialize() {

        // Yapılacak seçildiğinde detayın da gelmesi için kullanılan metod
        yapilacakListeFXML.getSelectionModel().selectedItemProperty().addListener((observable, eskiDeger, yeniDeger) -> {
            if (yeniDeger != null) {
                Yapilacak yapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
                detayFXML.setText(yapilacak.getDetay());
                DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMMM, yyyy");
                tarihLabel.setText(df.format(yapilacak.getTarih()));

            }
        });


        detayFXML.setOnKeyPressed(keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.S)) {
                try {
                    Yapilacak yapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
                    yapilacak.setDetay(detayFXML.getText());
                    YapilacakVeri.getInstance().yapilacaklariKaydet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        silFXML.setOnAction((ActionEvent e) -> {
            Yapilacak yapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
            yapilacakSil(yapilacak);
        });

        kaydetFXML.setOnAction((ActionEvent e) -> {
            Yapilacak yapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
            yapilacak.setDetay(detayFXML.getText());
            yapilacakKaydet();
        });

        tumYapilacaklarPredicate = yapilacak -> true;
        bugunYapilacaklarPredicate = yapilacak -> (yapilacak.getTarih().equals(LocalDate.now()));

        yapilacakFilteredList = new FilteredList<>(YapilacakVeri.getInstance().getYapilacaklar(), tumYapilacaklarPredicate);

        SortedList<Yapilacak> sortedList = new SortedList<>(yapilacakFilteredList, Comparator.comparing(Yapilacak::getTarih));

        pomodoroToggleButtonFXML.setOnAction(observable -> {
            if (pomodoroToggleButtonFXML.isSelected()){
                baslat();
            } else {
                durdur();
            }
        });

        yapilacakListeFXML.setItems(sortedList);
        yapilacakListeFXML.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        yapilacakListeFXML.getSelectionModel().selectFirst();

    }

    @FXML
    public void yapilacakAlertsizKaydet() {
        try {
            YapilacakVeri.getInstance().yapilacaklariKaydet();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void yapilacakKaydet() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Yapilacakları Kaydet");
        alert.setContentText("Yapılacakları kaydetmek istediğinizden emin misiniz?");
        Optional<ButtonType> sonuc = alert.showAndWait();
        if (sonuc.isPresent() && (sonuc.get() == ButtonType.OK)) {
            try {
                YapilacakVeri.getInstance().yapilacaklariKaydet();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void prepareAttempt(AttemptKind kind) {
        reset();

        mCurrentAttempt = new Attempt(kind);
        mTimeline = new Timeline();
        mTimeline.setCycleCount(kind.getTotalSeconds());
        mTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            mCurrentAttempt.tick();
            pomodoroToggleButtonFXML.setText(mCurrentAttempt.toString());
        }));
        mTimeline.setOnFinished(e -> {
            saveCurrentAttempt();
            prepareAttempt((mCurrentAttempt.getKind() == AttemptKind.FOCUS) ? AttemptKind.BREAK : AttemptKind.FOCUS);
        });
    }

    private void saveCurrentAttempt() {
        mCurrentAttempt.save();
    }

    public void setTimerText(String timerText) {
        this.mTimerText.set(timerText);
    }

    public void setTimerText(int remainingSeconds) {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        setTimerText(String.format("%02d:%02d", minutes, seconds));
    }

    private void reset() {
        if (mTimeline != null && mTimeline.getStatus() == Animation.Status.RUNNING) {
            mTimeline.stop();
        }
    }

    public void playTimer() {
        mTimeline.play();
    }

    public void pauseTimer() {
        mTimeline.pause();
    }



    public void handleRestart() {
        prepareAttempt(AttemptKind.FOCUS);
        playTimer();
    }

    public void baslat() {
        if (mCurrentAttempt == null) {
            handleRestart();
        } else {
            playTimer();
        }
    }

    public void durdur() {
        pauseTimer();
    }

    @FXML
    public void yeniYapilacakDialog() {
        MuzikOynatici.dialogMuzikOynat();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Yeni Yapılacak");
        dialog.setHeaderText("Yeni yapılacak aktiviteyi buradan oluşturabilirsiniz.");
        dialog.initOwner(vbox.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("yapilacakDialogEkrani.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Dialog'u yükleyemedik.");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> cikti = dialog.showAndWait();
        if (cikti.isPresent() && cikti.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();
            Yapilacak yeniYapilacak = controller.ciktiyiGoster();
            yapilacakListeFXML.getSelectionModel().select(yeniYapilacak);
            MuzikOynatici.okMuzigiOynat();
        } else {
            MuzikOynatici.cancelMuzigiOynat();
        }
    }

    @FXML
    public void emailGonderMetodu() {
        try {
            Runtime rt = Runtime.getRuntime();
            String url = "mailto:tehadro@gmail.com?subject=Program%20Hakkinda";
            String[] browsers = { "epiphany", "firefox", "mozilla", "konqueror",
                    "netscape", "opera", "links", "lynx" };

            StringBuilder cmd = new StringBuilder();
            for (int i = 0; i < browsers.length; i++)
                if(i == 0)
                    cmd.append(String.format(    "%s \"%s\"", browsers[i], url));
                else
                    cmd.append(String.format(" || %s \"%s\"", browsers[i], url));

            rt.exec(new String[] { "sh", "-c", cmd.toString() });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void tusaBasildiginda(javafx.scene.input.KeyEvent keyEvent) {
        Yapilacak secilenYapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (secilenYapilacak != null)
            if (keyEvent.getCode().equals(KeyCode.DELETE))
                yapilacakSil(secilenYapilacak);
    }

    public void yapilacakSil(Yapilacak yapilacak) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Yapilacagi Sil");
        alert.setHeaderText("Yapılacak: " + yapilacak.getAciklama());
        alert.setContentText("Silmek için OK, iptal etmek için Cancel");
        Optional<ButtonType> sonuc = alert.showAndWait();

        if (sonuc.isPresent() && (sonuc.get() == ButtonType.OK)) {
            YapilacakVeri.getInstance().yapilacakSil(yapilacak);
        }
    }

    @FXML
    public void bugunYapilacakGoster() {
        Yapilacak secilmisYapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (bugunToggleButton.isSelected()) {
            yapilacakFilteredList.setPredicate(bugunYapilacaklarPredicate);
            if (yapilacakFilteredList.isEmpty()) {
                detayFXML.clear();
                tarihLabel.setText("");
            } else if (yapilacakFilteredList.contains(secilmisYapilacak)) {
                yapilacakListeFXML.getSelectionModel().select(secilmisYapilacak);
            } else {
                yapilacakListeFXML.getSelectionModel().selectFirst();
            }
        } else {
            yapilacakFilteredList.setPredicate(tumYapilacaklarPredicate);
            yapilacakListeFXML.getSelectionModel().select(secilmisYapilacak);
        }

    }

    @FXML
    public void programdanCik() {
        Platform.exit();
    }

    public void hakkindaMetodu() {
        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.setTitle("Hakkında");
        al.setHeaderText("Hakkında");
        al.setContentText("Emeği Geçenler:\n\nBerkan Sözer\n");
        al.showAndWait();
    }

    public void kapat() {
        Alert cikisAlert = new Alert(Alert.AlertType.CONFIRMATION);
        cikisAlert.setTitle("Çıkış Yap");
        cikisAlert.setHeaderText("Emin misiniz?");
        cikisAlert.setResizable(false);

        Optional<ButtonType> result = cikisAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    @FXML
    public void kes() {
        String text = detayFXML.getSelectedText();

        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        systemClipboard.setContent(content);

        IndexRange range = detayFXML.getSelection();
        String origText = detayFXML.getText();
        String firstPart = StringUtils.substring(origText, 0, range.getStart());
        String lastPart = StringUtils.substring(origText, range.getEnd(), StringUtils.length(origText));
        detayFXML.setText(firstPart + lastPart);

        detayFXML.positionCaret(range.getStart());

    }

    @FXML
    public void sil(){
        detayFXML.replaceSelection("");
    }

    @FXML
    public void kopyala() {
        String text = detayFXML.getSelectedText();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        systemClipboard.setContent(content);
    }

    @FXML
    public void yapistir() {
        if (!systemClipboard.hasContent(DataFormat.PLAIN_TEXT)) {
            adjustForEmptyClipboard();
            return;
        }

        String clipboardText = systemClipboard.getString();

//        TextField focusedTF = getFocusedTextField();
        IndexRange range = detayFXML.getSelection();

        String orjinalText = detayFXML.getText();

        int bitisPozisyonu;
        String guncellenmisText;
        String ilkKisim = StringUtils.substring(orjinalText, 0, range.getStart());
        String sonKisim = StringUtils.substring(orjinalText, range.getEnd(), StringUtils.length(orjinalText));

        guncellenmisText = ilkKisim + clipboardText + sonKisim;

        if (range.getStart() == range.getEnd()) {
            bitisPozisyonu = range.getEnd() + StringUtils.length(clipboardText);
        } else {
            bitisPozisyonu = range.getStart() + StringUtils.length(clipboardText);
        }

        detayFXML.setText(guncellenmisText);
        detayFXML.positionCaret(bitisPozisyonu);
    }

    @FXML
    public void hepsiniSec(){
        detayFXML.selectAll();
    }
    @FXML
    public void hicbiriniSecme(){
        detayFXML.deselect();
    }

    @FXML
    public void showingEditMenu() {
        if (systemClipboard == null) {
            systemClipboard = Clipboard.getSystemClipboard();
        }

        if (systemClipboard.hasString()) {
            adjustForClipboardContents();
        } else {
            adjustForEmptyClipboard();
        }

        if (anythingSelected()) {
            adjustForSelection();

        } else {
            adjustForDeselection();
        }
    }

    private boolean anythingSelected() {
        TextField textField = new TextField();
        return textField.getSelectedText().isEmpty();
    }

    private void adjustForEmptyClipboard() {
        kopyalaFXML.setDisable(true);  // nothing to paste
    }

    private void adjustForClipboardContents() {
        yapistirFXML.setDisable(false);  // something to paste
    }

    private void adjustForSelection() {
        kesFXML.setDisable(false);
        kopyalaFXML.setDisable(false);
    }

    private void adjustForDeselection() {
        kesFXML.setDisable(true);
        kopyalaFXML.setDisable(true);
    }

}

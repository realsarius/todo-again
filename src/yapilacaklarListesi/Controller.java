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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import yapilacaklarListesi.muzik.Muzik;
import yapilacaklarListesi.muzik.MuzikOynatici;
import yapilacaklarListesi.pomodoro.model.Pomodoro;
import yapilacaklarListesi.pomodoro.model.PomodoroEnum;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

// Controller kısmının içerisinde nesneye yönelik kısmı ile ilgili bir şey yok ancak açıklamaları yapacağım.
public class Controller {

    @FXML private MenuItem farkliKaydetFXML;
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
    Clipboard sistemPanosu = Clipboard.getSystemClipboard();

    private Pomodoro suankiPomodoro;
    private final StringProperty zamanlayiciText;
    private Timeline timeLine;

    public Controller(){
        zamanlayiciText = new SimpleStringProperty();
        setTimerText(0);
    }

    // Program başlarken yapılacaklar
    public void initialize() {

        farkliKaydetFXML.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Text");
            File file = fileChooser.showSaveDialog(stage);
            final String content = detayFXML.getText();
            try (final BufferedWriter writer = Files.newBufferedWriter(file.getAbsoluteFile().toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                writer.write(content);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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
    public void prepareAttempt(PomodoroEnum pomodoroHatirlatici) {
        reset();

        suankiPomodoro = new Pomodoro(pomodoroHatirlatici);
        timeLine = new Timeline();
        timeLine.setCycleCount(pomodoroHatirlatici.getToplamSaniye());
        timeLine.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            suankiPomodoro.tik();
            pomodoroToggleButtonFXML.setText(suankiPomodoro.toString());
        }));
        timeLine.setOnFinished(e -> {
            zamanlayiciyiKaydet();
            prepareAttempt((suankiPomodoro.getPomodoroEnum() == PomodoroEnum.FOCUS) ? PomodoroEnum.BREAK : PomodoroEnum.FOCUS);
        });
    }

    private void zamanlayiciyiKaydet() {
        suankiPomodoro.kaydet();
    }

    public void setZamanlayiciText(String zamanlayiciText) {
        this.zamanlayiciText.set(zamanlayiciText);
    }

    public void setTimerText(int geriyeKalanSaniye) {
        int dakika = geriyeKalanSaniye / 60;
        int saniye = geriyeKalanSaniye % 60;
        setZamanlayiciText(String.format("%02d:%02d", dakika, saniye));
    }

    private void reset() {
        if (timeLine != null && timeLine.getStatus() == Animation.Status.RUNNING) {
            timeLine.stop();
        }
    }

    public void zamanlayiciyiBaslat() {
        timeLine.play();
    }

    public void zamanlayiciyiDuraklat() {
        timeLine.pause();
    }

    public void zamanlayiciRestart() {
        prepareAttempt(PomodoroEnum.FOCUS);
        zamanlayiciyiBaslat();
    }

    public void baslat() {
        if (suankiPomodoro == null) {
            zamanlayiciRestart();
        } else {
            zamanlayiciyiBaslat();
        }
    }

    public void durdur() {
        zamanlayiciyiDuraklat();
    }

    // Yeni yapılacak eklemek için yapılmış yeni bir dialog
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
                    "netscape", "opera", "links", "lynx", "chrome", "operagx" };

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

    // Klavyemizden DELETE tuşuna bastığımızda, ListView'de hangi yapılacak seçili ise onu siliyor.
    @FXML
    public void tusaBasildiginda(javafx.scene.input.KeyEvent keyEvent) {
        Yapilacak secilenYapilacak = yapilacakListeFXML.getSelectionModel().getSelectedItem();
        if (secilenYapilacak != null)
            if (keyEvent.getCode().equals(KeyCode.DELETE))
                yapilacakSil(secilenYapilacak);
    }

    // Yapılacak silinirken bir Alert sahnesi gösteriyor. OK butonu ile parametresi ile birlikte yapilacakSil metodu çağrılıyor.
    // ListView'den seçili olan yapılacak siliniyor. Dikkat edilmesi gerekn kısımlardan bir tanesi ise
    // Yapılacaklar kaydedilirken newLine'da eklendiği için yapılacak silinirken boşluk kalmıyor.
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

    // Bu metod ListView'deki herhangi bir yapılacağın Acik
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

    // Alert sahnesi ile yapılan Hakkımda metodu.
    public void hakkindaMetodu() {
        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.setTitle("Hakkında");
        al.setHeaderText("Hakkında");
        al.setContentText("Emeği Geçenler:\n\nBerkan Sözer\n");
        al.showAndWait();
    }

    // Programdan çıkış yaparken Alert veriyor. showAndWait ile Alert'i bekletmek zorundayız yoksa hemen kapanıyor.
    // CANCEL tuşuna bastığımızda hiçbir şey yapmıyor ancak OK tuşuna bastığımızda Platform.exit() metodunu çağırarak programdan çıkabiliyoruz.
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

    // detayFXML'de seçilen yeri kesme metodu
    @FXML
    public void kes() {
        String text = detayFXML.getSelectedText();

        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        sistemPanosu.setContent(content);

        // Buraya kadar olan kısımda seçilen texti sistem panosuna ekliyor. Geri kalan kısmında ise seçilen yeri(baslangic'tan son'a kadar) siliyor.

        IndexRange range = detayFXML.getSelection();
        String origText = detayFXML.getText();
        String baslangic = StringUtils.substring(origText, 0, range.getStart());
        String son = StringUtils.substring(origText, range.getEnd(), StringUtils.length(origText));

        detayFXML.setText(baslangic + son);
        detayFXML.positionCaret(range.getStart());

    }

    // Basit bir işlem. detayFXML'in içeriğini boş bir string ile değiştiriyor.
    @FXML
    public void sil(){
        detayFXML.replaceSelection("");
    }

    // detayFXML'de seçilen texti content nesnesine putString ile atıyoruz. systemClipboard ise işletim sisteminin clipboard'u.
    @FXML
    public void kopyala() {
        String text = detayFXML.getSelectedText();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        sistemPanosu.setContent(content);
    }

    // Sistem panosunda bir içerik varsa yapıştırıyoruz. stackoverflow'da bulduğum kodu kendi programıma uyarladım.
    // Metodun sonlarına doğru sistem panosunda olan içeriği, detayFXML'e yapıştırıyor
    @FXML
    public void yapistir() {
        if (!sistemPanosu.hasContent(DataFormat.PLAIN_TEXT)) {
            bosPanoIcinAyarlar();
            return;
        }

        String clipboardText = sistemPanosu.getString();
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

    // detayFXML'deki tüm text seç
    @FXML
    public void hepsiniSec(){
        detayFXML.selectAll();
    }

    // detayFXML'deki hiçbir texti seçme
    @FXML
    public void hicbiriniSecme(){
        detayFXML.deselect();
    }

    // Geriye kalan metodlar şuanda kullanılmıyor. Amaç kopyalanacakya da kesilecek bir şey yok ise "Kopyala" ya da "Kes" butonlarının setDisable(true) olması yani basılabilir olmaması
    @FXML
    public void duzenleMenuyuGoster() {
        if (sistemPanosu == null) {
            sistemPanosu = Clipboard.getSystemClipboard();
        }

        if (sistemPanosu.hasString()) {
            panoIceriginiAyarla();
        } else {
            bosPanoIcinAyarlar();
        }

        if (birSeySeciliMi()) {
            adjustForSelection();

        } else {
            adjustForDeselection();
        }
    }

    private boolean birSeySeciliMi() {
        TextField textField = new TextField();
        return textField.getSelectedText().isEmpty();
    }

    private void bosPanoIcinAyarlar() {
        kopyalaFXML.setDisable(true);  // Yapıştırılacak bir şey yok ise
    }

    private void panoIceriginiAyarla() {
        yapistirFXML.setDisable(false);  // Yapıştırılacak bir şey var ise
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

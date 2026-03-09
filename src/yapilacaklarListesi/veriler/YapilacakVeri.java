package yapilacaklarListesi.veriler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

// YapilacakVeri bir Singleton sınıfı.
public class YapilacakVeri {

    // Singleton'un kurallarından bir tanesi kendi nesnesini oluşturup initalize etmek ki aşağıdaki getInstance() sınıfında return edebilelim.
    private static final YapilacakVeri ornek = new YapilacakVeri();
    private static final String dosyaAdi = "Yapilacaklar.txt";
    private static final int BEKLENEN_SUTUN_SAYISI = 3;
    private ObservableList<Yapilacak> yapilacaklar;
    private final DateTimeFormatter dateTimeFormatter;
    private Path dosyaYolu;

    // Singleton
    public static YapilacakVeri getInstance() {
        return ornek;
    }

    // Singleton'un kurallarından bir tanesi Constructor'un "private" olması
    // Gelen zamanın formatını burada istediğimiz gibi değiştirebiliriz. Bildiğimiz takvim olan gün-ay-yıl'a dönüştürdüm.
    private YapilacakVeri() {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        this.dosyaYolu = Paths.get(dosyaAdi);
        this.yapilacaklar = FXCollections.observableArrayList();
    }

    // ObservableList'in ArrayList'ten farkı listede meydana gelen değişiklikleri izleyebiliyor olması. ObservableList bir JavaFX sınıfı -> javafx.collections.ObservableList;
    public ObservableList<Yapilacak> getYapilacaklar() {
        return yapilacaklar;
    }

    // Parametre olarak aldığı yapılacak sınıfını yapilacaklar ObservableList'ine ekliyor.
    public void yapilacakEkle(Yapilacak yapilacak) {
        if (yapilacaklar == null) {
            yapilacaklar = FXCollections.observableArrayList();
        }
        yapilacaklar.add(yapilacak);
    }


    public void yapilacaklariCagir() throws IOException {
        yapilacaklar = FXCollections.observableArrayList();
        if (Files.notExists(dosyaYolu)) {
            dosyaYolunuHazirla();
            Files.createFile(dosyaYolu);
            return;
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(dosyaYolu)) {
            String input;
            int satirNo = 0;
            while ((input = bufferedReader.readLine()) != null) {
                satirNo++;
                if (input.isBlank()) {
                    continue;
                }
                String[] yapilacakParcalar = input.split("\t", BEKLENEN_SUTUN_SAYISI);
                if (yapilacakParcalar.length < BEKLENEN_SUTUN_SAYISI) {
                    System.err.printf("Uyari: satir %d atlandi (eksik alan).%n", satirNo);
                    continue;
                }
                try {
                    String aciklama = yapilacakParcalar[0];
                    String detay = yapilacakParcalar[1];
                    String zamanString = yapilacakParcalar[2];
                    LocalDate zaman = LocalDate.parse(zamanString, dateTimeFormatter);
                    Yapilacak yapilacak = new Yapilacak(aciklama, detay, zaman);
                    yapilacaklar.add(yapilacak);
                } catch (DateTimeParseException e) {
                    System.err.printf("Uyari: satir %d atlandi (tarih parse edilemedi).%n", satirNo);
                }
            }
        }
    }

    // BufferedWriter ile yapılacaklar dosyaAdi(Yapilacaklar.txt) dosyasına kaydediliyor. Metodun herhangi bir parametrisi yok. Peki nasıl kaydediliyor?
    // FXML kısmında girilen her yapılacak ObservableList olan yapilacaklar'a atılıyor. Program kapanırken yapilacaklariKaydet() metodu çağrılıyor.
    // Örnek olarak 3 yapılacak girildi ve çıkış yaptık. Listemizin içinde 3 tane yapılacak var. forEach döngüsü ile teker teker Yapilacaklar.txt dosyasına
    // bu metod ile yazdırıyoruz. Yapılacak entity sınıfının 3 özelliği var. Sırasıyla aciklama, detay, tarih. Aralarında bir tab(\t) boşluğu olacak şekilde ayırarak
    // verileri yazdırıp getirmesi kolaylaşıyor
    public void yapilacaklariKaydet() throws IOException {
        if (Files.notExists(dosyaYolu)) {
            dosyaYolunuHazirla();
            Files.createFile(dosyaYolu);
        }
        if (yapilacaklar == null) {
            yapilacaklar = FXCollections.observableArrayList();
        }

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(dosyaYolu)) {
            for (Yapilacak yapilacak : yapilacaklar) {
                bufferedWriter.write(String.format("%s\t%s\t%s",
                        temizleAlan(yapilacak.getAciklama()),
                        temizleAlan(yapilacak.getDetay()),
                        yapilacak.getTarih().format(dateTimeFormatter)));
                bufferedWriter.newLine();
            }
        }
    }

    public void yapilacakSil(Yapilacak yapilacak) {
        this.yapilacaklar.remove(yapilacak);
    }

    // Testler ve migration kontrolleri icin dosya yolunu degistirilebilir tutuyoruz.
    void setDosyaYolu(Path dosyaYolu) {
        this.dosyaYolu = dosyaYolu;
    }

    void varsayilanDosyaYolunaDon() {
        this.dosyaYolu = Paths.get(dosyaAdi);
    }

    private String temizleAlan(String alan) {
        if (alan == null) {
            return "";
        }
        return alan.replace("\t", " ").replace("\n", " ").replace("\r", " ");
    }

    private void dosyaYolunuHazirla() throws IOException {
        Path parent = dosyaYolu.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
    }
}

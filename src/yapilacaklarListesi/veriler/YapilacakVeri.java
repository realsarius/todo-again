package yapilacaklarListesi.veriler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class YapilacakVeri {

    private static final YapilacakVeri ornek = new YapilacakVeri();
    private static final String legacyDosyaAdi = "Yapilacaklar.txt";
    private static final String jsonDosyaAdi = "Yapilacaklar.json";
    private static final int JSON_VERSIYON = 1;
    private static final int BEKLENEN_SUTUN_SAYISI = 3;

    private ObservableList<Yapilacak> yapilacaklar;
    private final DateTimeFormatter legacyDateTimeFormatter;
    private final Gson gson;
    private Path legacyDosyaYolu;
    private Path jsonDosyaYolu;
    private boolean manuelDosyaYolu;

    public static YapilacakVeri getInstance() {
        return ornek;
    }

    private YapilacakVeri() {
        this.legacyDateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.legacyDosyaYolu = Paths.get(legacyDosyaAdi);
        this.jsonDosyaYolu = Paths.get(jsonDosyaAdi);
        this.yapilacaklar = FXCollections.observableArrayList();
        this.manuelDosyaYolu = false;
    }

    public ObservableList<Yapilacak> getYapilacaklar() {
        return yapilacaklar;
    }

    public void yapilacakEkle(Yapilacak yapilacak) {
        if (yapilacaklar == null) {
            yapilacaklar = FXCollections.observableArrayList();
        }
        yapilacaklar.add(yapilacak);
    }

    public void yapilacaklariCagir() throws IOException {
        yapilacaklar = FXCollections.observableArrayList();

        if (manuelDosyaYolu) {
            if (Files.exists(legacyDosyaYolu)) {
                txtDosyasindanYukle();
                if (Files.notExists(jsonDosyaYolu)) {
                    jsonaKaydet();
                }
                return;
            }
            if (Files.exists(jsonDosyaYolu) && jsondanYukle(jsonDosyaYolu, true)) {
                return;
            }
            dosyaYolunuHazirla(legacyDosyaYolu);
            Files.createFile(legacyDosyaYolu);
            return;
        }

        if (Files.exists(jsonDosyaYolu) && jsondanYukle(jsonDosyaYolu, true)) {
            return;
        }
        if (Files.exists(legacyDosyaYolu)) {
            txtDosyasindanYukle();
            jsonaKaydet();
            return;
        }

        dosyaYolunuHazirla(legacyDosyaYolu);
        Files.createFile(legacyDosyaYolu);
    }

    public void yapilacaklariKaydet() throws IOException {
        if (yapilacaklar == null) {
            yapilacaklar = FXCollections.observableArrayList();
        }
        jsonaKaydet();
        txtDosyasinaKaydet();
    }

    public void yapilacakSil(Yapilacak yapilacak) {
        this.yapilacaklar.remove(yapilacak);
    }

    void setDosyaYolu(Path dosyaYolu) {
        this.legacyDosyaYolu = dosyaYolu;
        this.jsonDosyaYolu = jsonYolunuUret(dosyaYolu);
        this.manuelDosyaYolu = true;
    }

    void varsayilanDosyaYolunaDon() {
        this.legacyDosyaYolu = Paths.get(legacyDosyaAdi);
        this.jsonDosyaYolu = Paths.get(jsonDosyaAdi);
        this.manuelDosyaYolu = false;
    }

    Path getJsonDosyaYolu() {
        return jsonDosyaYolu;
    }

    private void txtDosyasindanYukle() throws IOException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(legacyDosyaYolu)) {
            String input;
            int satirNo = 0;
            while ((input = bufferedReader.readLine()) != null) {
                satirNo++;
                if (input.isBlank()) {
                    continue;
                }
                String[] yapilacakParcalar = input.split("\\t", BEKLENEN_SUTUN_SAYISI);
                if (yapilacakParcalar.length < BEKLENEN_SUTUN_SAYISI) {
                    System.err.printf("Uyari: satir %d atlandi (eksik alan).%n", satirNo);
                    continue;
                }
                try {
                    String aciklama = yapilacakParcalar[0];
                    String detay = yapilacakParcalar[1];
                    String zamanString = yapilacakParcalar[2];
                    LocalDate zaman = LocalDate.parse(zamanString, legacyDateTimeFormatter);
                    Yapilacak yapilacak = new Yapilacak(aciklama, detay, zaman);
                    yapilacaklar.add(yapilacak);
                } catch (DateTimeParseException e) {
                    System.err.printf("Uyari: satir %d atlandi (tarih parse edilemedi).%n", satirNo);
                }
            }
        }
    }

    private boolean jsondanYukle(Path kaynakDosya, boolean yedekKullan) throws IOException {
        try {
            if (Files.notExists(kaynakDosya)) {
                return false;
            }
            String icerik = Files.readString(kaynakDosya, StandardCharsets.UTF_8);
            if (icerik.isBlank()) {
                return true;
            }
            YapilacaklarJson jsonVeri = gson.fromJson(icerik, YapilacaklarJson.class);
            if (jsonVeri == null || jsonVeri.tasks == null) {
                return true;
            }
            for (int i = 0; i < jsonVeri.tasks.size(); i++) {
                YapilacakKaydi kayit = jsonVeri.tasks.get(i);
                if (kayit == null || kayit.aciklama == null || kayit.tarih == null) {
                    System.err.printf("Uyari: JSON kaydi %d atlandi (eksik alan).%n", i + 1);
                    continue;
                }
                LocalDate tarih = parseJsonTarihi(kayit.tarih, i + 1);
                if (tarih == null) {
                    continue;
                }
                String detay = kayit.detay == null ? "" : kayit.detay;
                yapilacaklar.add(new Yapilacak(kayit.aciklama, detay, tarih));
            }
            return true;
        } catch (JsonParseException e) {
            if (!yedekKullan) {
                System.err.println("Uyari: JSON yedegi de okunamadi.");
                return false;
            }
            Path yedek = yedekDosyaYolu(kaynakDosya);
            if (Files.exists(yedek) && jsondanYukle(yedek, false)) {
                jsonaKaydet();
                return true;
            }
            System.err.println("Uyari: JSON okunamadi, uygun yedek bulunamadi.");
            yapilacaklar.clear();
            return false;
        }
    }

    private LocalDate parseJsonTarihi(String tarihDegeri, int kayitNo) {
        try {
            return LocalDate.parse(tarihDegeri);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(tarihDegeri, legacyDateTimeFormatter);
            } catch (DateTimeParseException e) {
                System.err.printf("Uyari: JSON kaydi %d atlandi (tarih parse edilemedi).%n", kayitNo);
                return null;
            }
        }
    }

    private void txtDosyasinaKaydet() throws IOException {
        if (Files.notExists(legacyDosyaYolu)) {
            dosyaYolunuHazirla(legacyDosyaYolu);
            Files.createFile(legacyDosyaYolu);
        }
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(legacyDosyaYolu)) {
            for (Yapilacak yapilacak : yapilacaklar) {
                bufferedWriter.write(String.format("%s\t%s\t%s",
                        temizleAlan(yapilacak.getAciklama()),
                        temizleAlan(yapilacak.getDetay()),
                        yapilacak.getTarih().format(legacyDateTimeFormatter)));
                bufferedWriter.newLine();
            }
        }
    }

    private String temizleAlan(String alan) {
        if (alan == null) {
            return "";
        }
        return alan.replace("\t", " ").replace("\n", " ").replace("\r", " ");
    }

    private void jsonaKaydet() throws IOException {
        dosyaYolunuHazirla(jsonDosyaYolu);
        yedekOlustur(jsonDosyaYolu);

        YapilacaklarJson jsonVeri = new YapilacaklarJson();
        jsonVeri.version = JSON_VERSIYON;
        jsonVeri.tasks = new ArrayList<>();

        for (Yapilacak yapilacak : yapilacaklar) {
            YapilacakKaydi kayit = new YapilacakKaydi();
            kayit.aciklama = temizleAlan(yapilacak.getAciklama());
            kayit.detay = temizleAlan(yapilacak.getDetay());
            kayit.tarih = yapilacak.getTarih().toString();
            jsonVeri.tasks.add(kayit);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(jsonDosyaYolu, StandardCharsets.UTF_8)) {
            gson.toJson(jsonVeri, writer);
        }
    }

    private void yedekOlustur(Path kaynak) throws IOException {
        if (Files.exists(kaynak)) {
            Files.copy(kaynak, yedekDosyaYolu(kaynak), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path yedekDosyaYolu(Path kaynak) {
        return Paths.get(kaynak.toString() + ".bak");
    }

    private Path jsonYolunuUret(Path txtYolu) {
        String dosyaAdi = txtYolu.getFileName().toString();
        String jsonAdi;
        if (dosyaAdi.endsWith(".txt")) {
            jsonAdi = dosyaAdi.substring(0, dosyaAdi.length() - 4) + ".json";
        } else {
            jsonAdi = dosyaAdi + ".json";
        }
        Path parent = txtYolu.getParent();
        return parent == null ? Paths.get(jsonAdi) : parent.resolve(jsonAdi);
    }

    private void dosyaYolunuHazirla(Path dosyaYolu) throws IOException {
        Path parent = dosyaYolu.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private static class YapilacaklarJson {
        int version;
        List<YapilacakKaydi> tasks;
    }

    private static class YapilacakKaydi {
        String aciklama;
        String detay;
        String tarih;
    }
}

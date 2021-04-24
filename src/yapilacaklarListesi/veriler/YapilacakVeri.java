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
import java.util.Iterator;
import java.util.List;

public class YapilacakVeri {

    private static YapilacakVeri ornek = new YapilacakVeri();
    private static String dosyaAdi = "Yapilacaklar.txt";
    private ObservableList<Yapilacak> yapilacaklar;
    private DateTimeFormatter dateTimeFormatter;

    public static YapilacakVeri getInstance() {
        return ornek;
    }


    private YapilacakVeri() {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public ObservableList<Yapilacak> getYapilacaklar() {
        return yapilacaklar;
    }

    public void yapilacakEkle(Yapilacak yapilacak) {
        yapilacaklar.add(yapilacak);
    }

    public void yapilacaklariCagir() throws IOException {
        yapilacaklar = FXCollections.observableArrayList();
        Path dosyaYolu = Paths.get(dosyaAdi);
        BufferedReader bufferedReader = Files.newBufferedReader(dosyaYolu);
        String input;
        try {
            while ((input = bufferedReader.readLine()) != null) {
                String[] yapilacakParcalar = input.split("\t");

                String aciklama = yapilacakParcalar[0];
                String detay = yapilacakParcalar[1];
                String zamanString = yapilacakParcalar[2];

                LocalDate zaman = LocalDate.parse(zamanString, dateTimeFormatter);
                Yapilacak yapilacak = new Yapilacak(aciklama, detay, zaman);
                yapilacaklar.add(yapilacak);

            }

        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    public void yapilacaklariKaydet() throws IOException {
        Path dosyaYolu = Paths.get(dosyaAdi);
        BufferedWriter bufferedWriter = Files.newBufferedWriter(dosyaYolu);
        try {
            Iterator<Yapilacak> iterator = yapilacaklar.iterator();
            while (iterator.hasNext()) {
                Yapilacak yapilacak = iterator.next();
                bufferedWriter.write(String.format("%s\t%s\t%s",
                        yapilacak.getAciklama(),
                        yapilacak.getDetay(),
                        yapilacak.getTarih().format(dateTimeFormatter)));
                bufferedWriter.newLine();
            }
        } finally {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }


    public void yapilacakSil(Yapilacak yapilacak) {
        this.yapilacaklar.remove(yapilacak);
    }
}

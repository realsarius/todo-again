package yapilacaklarListesi.veriler;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class YapilacakVeriTest extends TestCase {


    public void testGetInstance() {
    }

    public void testGetYapilacaklar() {
    }

    public void testYapilacakEkle() {
    }

    public void testYapilacaklariCagir() {
    }

    public void testYapilacaklariKaydet() {
        String dosyaAdi = "Yapilacaklar.txt";
        Path dosyaYolu = Paths.get(dosyaAdi);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(dosyaYolu)) {
            bufferedWriter.write(String.format("%s\t%s\t%s",
                    "aciklama",
                    "detay",
                    LocalDate.parse("04-04-2021", dateTimeFormatter)));
            bufferedWriter.newLine();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public void testYapilacakSil() {
    }
}
package yapilacaklarListesi.veriler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class YapilacakVeriTest extends TestCase {

    private final DateTimeFormatter dateTimeFormatter;
    private ObservableList<Yapilacak> yapilacaklar;

    public YapilacakVeriTest() {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public void testYapilacakEkle() throws IOException {
        System.out.println("**********testYapilacakEkle**********");
        yapilacaklar = FXCollections.observableArrayList();
        Path path = Paths.get("YapilacaklarTest");
        if (!Files.exists(path)) {
            Files.createDirectory(Paths.get("YapilacaklarTest"));
            File sourceLocation = new File("Yapilacaklar.txt");
            File targetLocation = new File("YapilacaklarTest");
            FileUtils.copyFileToDirectory(sourceLocation, targetLocation);
            String aciklama = "aciklama";
            String detay = "detay";
            LocalDate tarih = LocalDate.parse("01-01-2020", dateTimeFormatter);
            Yapilacak yeniYapilacak = new Yapilacak(aciklama, detay, tarih);

            Writer bufferedWriter = new BufferedWriter(new FileWriter(String.valueOf(Paths.get("YapilacaklarTest/Yapilacaklar.txt")), true));
            bufferedWriter.append(String.format("%s\t%s\t%s",
                    yeniYapilacak.getAciklama(),
                    yeniYapilacak.getDetay(),
                    yeniYapilacak.getTarih().format(dateTimeFormatter)));
            bufferedWriter.close();
        } else {
            String aciklama = "aciklama";
            String detay = "detay";
            LocalDate tarih = LocalDate.parse("01-01-2020", dateTimeFormatter);
            Yapilacak yeniYapilacak = new Yapilacak(aciklama, detay, tarih);

            Writer bufferedWriter = new BufferedWriter(new FileWriter(String.valueOf(Paths.get("YapilacaklarTest/Yapilacaklar.txt")), true));
            bufferedWriter.append("\n");
            bufferedWriter.append(String.format("%s\t%s\t%s",
                    yeniYapilacak.getAciklama(),
                    yeniYapilacak.getDetay(),
                    yeniYapilacak.getTarih().format(dateTimeFormatter)));
            System.out.println(yeniYapilacak.getAciklama() + "\t" + yeniYapilacak.getDetay() + "\t" + yeniYapilacak.getTarih().format(dateTimeFormatter));
            System.out.println("Başarıyla eklendi.");
            bufferedWriter.close();
        }
        ;
        System.out.println("**********testYapilacakEkle**********\n\n");
    }

    public void testYapilacaklariCagir() {
        System.out.println("**********testYapilacaklariCagir**********");
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get("YapilacaklarTest/Yapilacaklar.txt"))) {
            String input;
            while ((input = bufferedReader.readLine()) != null) {
                String[] yapilacakParcalar = input.split("\t");

                String aciklama = yapilacakParcalar[0];
                String detay = yapilacakParcalar[1];
                String zamanString = yapilacakParcalar[2];

                LocalDate zaman = LocalDate.parse(zamanString, dateTimeFormatter);
                System.out.println(aciklama + "\t" + detay + "\t" + zaman);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("**********testYapilacaklariCagir**********\n\n");
    }

    public void testYapilacaklariKaydet() throws FileNotFoundException {
        System.out.println("**********testYapilacaklariKaydet**********");
        if (!new File("Yapilacaklar.txt").exists()) {
            throw new FileNotFoundException("Dosya bulunamadı");
        } else {
            System.out.println("Dosya bulundu. Test Başarılı.");
        }
        System.out.println("**********testYapilacaklariKaydet**********\n\n");
    }

    public void testYapilacakSil() throws IOException {
        System.out.println("**********testYapilacaklariKaydet**********");
        RandomAccessFile f = new RandomAccessFile("YapilacaklarTest/Yapilacaklar.txt", "rw");
        byte b;
        long length = f.length() - 1;
        do {
            length -= 1;
            f.seek(length);
            b = f.readByte();
        } while (b != 10 && length>0);
        f.setLength(length + 1);
        f.close();
        System.out.println("Son satır başarıyla silindi.");
        System.out.println("**********testYapilacaklariKaydet**********\n\n");
    }
}
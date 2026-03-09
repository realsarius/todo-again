package yapilacaklarListesi.veriler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YapilacakVeriMigrationTest {

    private final YapilacakVeri veri = YapilacakVeri.getInstance();

    @AfterEach
    void temizle() {
        veri.varsayilanDosyaYolunaDon();
    }

    @Test
    void mevcutYapilacaklarDosyasiParseEdilir() throws IOException {
        Path legacyDosya = Paths.get("Yapilacaklar.txt");
        assertTrue(Files.exists(legacyDosya));

        veri.setDosyaYolu(legacyDosya);
        veri.yapilacaklariCagir();

        assertFalse(veri.getYapilacaklar().isEmpty());
        assertEquals("Ödevi tamamla", veri.getYapilacaklar().get(0).getAciklama());
    }

    @Test
    void bozukSatirlarAtlanirVeGecerliSatirlarYuklenir() throws IOException {
        Path tempDosya = Files.createTempFile("yapilacaklar-bozuk", ".txt");
        Files.writeString(tempDosya,
                "Valid Gorev\tDetay\t01-01-2026\n" +
                "Eksik Alan\n" +
                "Tarih Hatali\tDetay\t99-99-2026\n");

        veri.setDosyaYolu(tempDosya);
        veri.yapilacaklariCagir();

        assertEquals(1, veri.getYapilacaklar().size());
        assertEquals("Valid Gorev", veri.getYapilacaklar().get(0).getAciklama());
    }

    @Test
    void dosyaYoksaOlusturulurVeBosListeDoner() throws IOException {
        Path tempKlasor = Files.createTempDirectory("yapilacaklar");
        Path olmayanDosya = tempKlasor.resolve("Yapilacaklar.txt");
        assertFalse(Files.exists(olmayanDosya));

        veri.setDosyaYolu(olmayanDosya);
        veri.yapilacaklariCagir();

        assertTrue(Files.exists(olmayanDosya));
        assertTrue(veri.getYapilacaklar().isEmpty());
    }
}

package yapilacaklarListesi.veriler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YapilacakVeriMigrationTest {

    private final YapilacakVeri veri = YapilacakVeri.getInstance();

    @AfterEach
    void temizle() {
        veri.varsayilanDosyaYolunaDon();
        veri.getYapilacaklar().clear();
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

    @Test
    void kaydetVeYenidenYukleAyniVeriyiVerir() throws IOException {
        Path tempDosya = Files.createTempFile("yapilacaklar-kaydet", ".txt");
        veri.setDosyaYolu(tempDosya);
        veri.yapilacaklariCagir();

        veri.yapilacakEkle(new Yapilacak("Yenileme", "Plan dokumani", LocalDate.of(2026, 3, 9)));
        veri.yapilacaklariKaydet();

        veri.yapilacaklariCagir();

        assertEquals(1, veri.getYapilacaklar().size());
        assertEquals("Yenileme", veri.getYapilacaklar().get(0).getAciklama());
        assertEquals("Plan dokumani", veri.getYapilacaklar().get(0).getDetay());
        assertEquals(LocalDate.of(2026, 3, 9), veri.getYapilacaklar().get(0).getTarih());
        assertNotNull(veri.getYapilacaklar().get(0).getId());
        assertEquals(Oncelik.MEDIUM, veri.getYapilacaklar().get(0).getOncelik());
        assertNull(veri.getYapilacaklar().get(0).getDueTime());
        assertNull(veri.getYapilacaklar().get(0).getCompletedAt());
        assertFalse(veri.getYapilacaklar().get(0).isCompleted());
    }

    @Test
    void txtDosyasiYukleninceJsonDosyasiOlusturulur() throws IOException {
        Path tempDosya = Files.createTempFile("yapilacaklar-json-gecis", ".txt");
        Files.writeString(tempDosya, "JSON Gecis\tDetay\t09-03-2026\n");

        veri.setDosyaYolu(tempDosya);
        veri.yapilacaklariCagir();

        Path jsonDosya = veri.getJsonDosyaYolu();
        assertTrue(Files.exists(jsonDosya));
        String jsonIcerik = Files.readString(jsonDosya);
        assertTrue(jsonIcerik.contains("\"version\": 3"));
        assertTrue(jsonIcerik.contains("JSON Gecis"));
    }

    @Test
    void bozukJsonDosyasiYedektenGeriYuklenir() throws IOException {
        Path tempKlasor = Files.createTempDirectory("yapilacaklar-json-yedek");
        Path legacyDosya = tempKlasor.resolve("Yapilacaklar.txt");

        veri.setDosyaYolu(legacyDosya);
        Path jsonDosya = veri.getJsonDosyaYolu();
        Path yedekJson = Paths.get(jsonDosya.toString() + ".bak");

        Files.writeString(jsonDosya, "{bozuk-json");
        Files.writeString(yedekJson,
                "{\n" +
                        "  \"version\": 1,\n" +
                        "  \"tasks\": [\n" +
                        "    {\"aciklama\": \"Yedekten\", \"detay\": \"Kurtarma\", \"tarih\": \"2026-03-09\"}\n" +
                        "  ]\n" +
                        "}\n");

        veri.yapilacaklariCagir();

        assertEquals(1, veri.getYapilacaklar().size());
        assertEquals("Yedekten", veri.getYapilacaklar().get(0).getAciklama());
        assertTrue(Files.readString(jsonDosya).contains("Yedekten"));
    }

    @Test
    void legacyYoksaJsonDosyasindanYuklenir() throws IOException {
        Path tempKlasor = Files.createTempDirectory("yapilacaklar-json-oncelik");
        Path legacyDosya = tempKlasor.resolve("Yapilacaklar.txt");

        veri.setDosyaYolu(legacyDosya);
        Path jsonDosya = veri.getJsonDosyaYolu();
        Files.writeString(jsonDosya,
                "{\n" +
                        "  \"version\": 1,\n" +
                        "  \"tasks\": [\n" +
                        "    {\"aciklama\": \"JSON Kayit\", \"detay\": null, \"tarih\": \"09-03-2026\"}\n" +
                        "  ]\n" +
                        "}\n");

        veri.yapilacaklariCagir();

        assertEquals(1, veri.getYapilacaklar().size());
        assertEquals("JSON Kayit", veri.getYapilacaklar().get(0).getAciklama());
        assertEquals("", veri.getYapilacaklar().get(0).getDetay());
        assertEquals(LocalDate.of(2026, 3, 9), veri.getYapilacaklar().get(0).getTarih());
    }

    @Test
    void bosJsonIcerigiHataVermedenBosListeDoner() throws IOException {
        Path tempKlasor = Files.createTempDirectory("yapilacaklar-json-bos");
        Path legacyDosya = tempKlasor.resolve("Yapilacaklar.txt");

        veri.setDosyaYolu(legacyDosya);
        Path jsonDosya = veri.getJsonDosyaYolu();
        Files.writeString(jsonDosya, "");

        veri.yapilacaklariCagir();

        assertTrue(veri.getYapilacaklar().isEmpty());
    }

    @Test
    void jsonEksikAlanlariAtlarGecerliKaydiYukler() throws IOException {
        Path tempKlasor = Files.createTempDirectory("yapilacaklar-json-eksik");
        Path legacyDosya = tempKlasor.resolve("Yapilacaklar.txt");

        veri.setDosyaYolu(legacyDosya);
        Path jsonDosya = veri.getJsonDosyaYolu();
        Files.writeString(jsonDosya,
                "{\n" +
                        "  \"version\": 1,\n" +
                        "  \"tasks\": [\n" +
                        "    {\"aciklama\": \"Eksik Tarih\", \"detay\": \"Detay\"},\n" +
                        "    {\"aciklama\": \"Gecerli\", \"detay\": \"Detay\", \"tarih\": \"2026-03-09\"}\n" +
                        "  ]\n" +
                        "}\n");

        veri.yapilacaklariCagir();

        assertEquals(1, veri.getYapilacaklar().size());
        assertEquals("Gecerli", veri.getYapilacaklar().get(0).getAciklama());
    }

    @Test
    void jsonYeniAlanlarIleYuklenir() throws IOException {
        Path tempKlasor = Files.createTempDirectory("yapilacaklar-json-genis");
        Path legacyDosya = tempKlasor.resolve("Yapilacaklar.txt");

        veri.setDosyaYolu(legacyDosya);
        Path jsonDosya = veri.getJsonDosyaYolu();
        Files.writeString(jsonDosya,
                "{\n" +
                        "  \"version\": 1,\n" +
                        "  \"tasks\": [\n" +
                        "    {\n" +
                        "      \"id\": \"task-123\",\n" +
                        "      \"aciklama\": \"Yeni Alanlar\",\n" +
                        "      \"detay\": \"Detay\",\n" +
                        "      \"tarih\": \"2026-03-09\",\n" +
                        "      \"createdAt\": \"2026-03-09T10:00:00Z\",\n" +
                        "      \"updatedAt\": \"2026-03-09T12:00:00Z\",\n" +
                        "      \"oncelik\": \"HIGH\",\n" +
                        "      \"tags\": [\"is\", \"kisisel\"],\n" +
                        "      \"due_time\": \"14:30\",\n" +
                        "      \"completed_at\": \"2026-03-09T12:30:00\",\n" +
                        "      \"is_completed\": true\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n");

        veri.yapilacaklariCagir();

        Yapilacak kayit = veri.getYapilacaklar().get(0);
        assertEquals("task-123", kayit.getId());
        assertEquals(Oncelik.HIGH, kayit.getOncelik());
        assertEquals(Instant.parse("2026-03-09T10:00:00Z"), kayit.getCreatedAt());
        assertEquals(Instant.parse("2026-03-09T12:00:00Z"), kayit.getUpdatedAt());
        assertEquals(List.of("is", "kisisel"), kayit.getTags());
        assertFalse(kayit.isAllDay());
        assertEquals(LocalTime.of(14, 30), kayit.getStartTime());
        assertNull(kayit.getEndTime());
        assertEquals(LocalTime.of(14, 30), kayit.getDueTime());
        assertEquals(LocalDateTime.of(2026, 3, 9, 12, 30), kayit.getCompletedAt());
        assertTrue(kayit.isCompleted());
    }
}

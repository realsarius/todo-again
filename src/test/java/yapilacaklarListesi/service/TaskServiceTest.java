package yapilacaklarListesi.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yapilacaklarListesi.veriler.Oncelik;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskServiceTest {

    private final YapilacakVeri veri = YapilacakVeri.getInstance();
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        veri.getYapilacaklar().clear();
        taskService = new TaskService(veri);
    }

    @AfterEach
    void tearDown() throws Exception {
        varsayilanYolaDon();
        veri.getYapilacaklar().clear();
    }

    @Test
    void gorevEkleVeSilCalisir() {
        Yapilacak gorev = new Yapilacak("Refactor", "TaskService ayrimi", LocalDate.now());

        taskService.gorevEkle(gorev);
        assertEquals(1, taskService.tumGorevler().size());

        taskService.gorevSil(gorev);
        assertTrue(taskService.tumGorevler().isEmpty());
    }

    @Test
    void gorevDetayiGuncellemeNullDetayiBosYapar() {
        Yapilacak gorev = new Yapilacak("Kaydet", "eski", LocalDate.now());
        taskService.gorevEkle(gorev);

        taskService.gorevDetayiGuncelle(gorev, null);

        assertEquals("", gorev.getDetay());
    }

    @Test
    void bugunFiltresiSadeceBugunkuKaydiGosterir() {
        Yapilacak bugun = new Yapilacak("Bugun", "detay", LocalDate.now());
        Yapilacak yarin = new Yapilacak("Yarin", "detay", LocalDate.now().plusDays(1));

        taskService.gorevEkle(bugun);
        taskService.gorevEkle(yarin);

        long bugunAdedi = taskService.tumGorevler()
                .stream()
                .filter(taskService.bugunGorevleriFiltresi())
                .count();

        assertEquals(1, bugunAdedi);
        assertFalse(taskService.tumGorevler().isEmpty());
    }

    @Test
    void nullGorevEkleVeSilCagrisindaListeDegismez() {
        taskService.gorevEkle(null);
        taskService.gorevSil(null);
        assertTrue(taskService.tumGorevler().isEmpty());
    }

    @Test
    void nullGorevDetayiGuncellemeHataFirlatmaz() {
        taskService.gorevDetayiGuncelle(null, "detay");
        assertTrue(taskService.tumGorevler().isEmpty());
    }

    @Test
    void tumGorevlerFiltresiHerKayitIcinTrueDoner() {
        Yapilacak gorev = new Yapilacak("Filtre", "detay", LocalDate.now());
        assertTrue(taskService.tumGorevlerFiltresi().test(gorev));
    }

    @Test
    void yapiciyaNullVerilirseHataFirlatir() {
        assertThrows(NullPointerException.class, () -> new TaskService(null));
    }

    @Test
    void guncelleVeKaydetCalisir() throws Exception {
        Path tempDosya = Files.createTempFile("task-service", ".txt");
        testYolunuAyarla(tempDosya);

        Yapilacak gorev = new Yapilacak("Kayit", "ilk", LocalDate.of(2026, 3, 9));
        taskService.gorevEkle(gorev);

        taskService.gorevDetayiGuncelleVeKaydet(gorev, "guncel");

        veri.getYapilacaklar().clear();
        veri.yapilacaklariCagir();

        assertEquals(1, veri.getYapilacaklar().size());
        assertEquals("guncel", veri.getYapilacaklar().get(0).getDetay());
    }

    @Test
    void oncelikFiltresiSecilenOnceligiDoner() {
        Yapilacak high = new Yapilacak("A", "detay", LocalDate.now());
        high.setOncelik(Oncelik.HIGH);
        Yapilacak low = new Yapilacak("B", "detay", LocalDate.now());
        low.setOncelik(Oncelik.LOW);

        taskService.gorevEkle(high);
        taskService.gorevEkle(low);

        long highSayisi = taskService.tumGorevler().stream()
                .filter(taskService.oncelikFiltresi(Oncelik.HIGH))
                .count();
        assertEquals(1, highSayisi);
    }

    @Test
    void oncelikFiltresiNullVerilirseTumunuDoner() {
        Yapilacak gorev = new Yapilacak("Tum", "detay", LocalDate.now());
        taskService.gorevEkle(gorev);

        long sayi = taskService.tumGorevler().stream()
                .filter(taskService.oncelikFiltresi(null))
                .count();
        assertEquals(1, sayi);
    }

    @Test
    void gorevZamaniAllDayOldugundaSaatlerTemizlenir() {
        Yapilacak gorev = new Yapilacak("Saat", "detay", LocalDate.now());
        taskService.gorevEkle(gorev);

        taskService.gorevZamaniniGuncelle(gorev, false, LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertFalse(gorev.isAllDay());
        assertEquals(LocalTime.of(9, 0), gorev.getStartTime());
        assertEquals(LocalTime.of(10, 0), gorev.getEndTime());

        taskService.gorevZamaniniGuncelle(gorev, true, null, null);
        assertTrue(gorev.isAllDay());
        assertNull(gorev.getStartTime());
        assertNull(gorev.getEndTime());
    }

    @Test
    void gorevZamaniGecersizAraliktaHataVerir() {
        Yapilacak gorev = new Yapilacak("Saat", "detay", LocalDate.now());
        taskService.gorevEkle(gorev);

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.gorevZamaniniGuncelle(gorev, false, LocalTime.of(16, 0), LocalTime.of(15, 0))
        );
    }

    private void testYolunuAyarla(Path yol) throws Exception {
        Method m = YapilacakVeri.class.getDeclaredMethod("setDosyaYolu", Path.class);
        m.setAccessible(true);
        m.invoke(veri, yol);
    }

    private void varsayilanYolaDon() throws Exception {
        Method m = YapilacakVeri.class.getDeclaredMethod("varsayilanDosyaYolunaDon");
        m.setAccessible(true);
        m.invoke(veri);
    }
}

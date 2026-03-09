package yapilacaklarListesi.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

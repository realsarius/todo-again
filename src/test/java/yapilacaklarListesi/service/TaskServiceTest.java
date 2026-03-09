package yapilacaklarListesi.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void tearDown() {
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
}

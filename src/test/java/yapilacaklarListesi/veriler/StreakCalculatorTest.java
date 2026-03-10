package yapilacaklarListesi.veriler;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StreakCalculatorTest {

    @Test
    void guncelSeriArdisikGunleriHesaplar() {
        LocalDate bugun = LocalDate.of(2026, 3, 10);

        Yapilacak g1 = yeniTamamlanan("A", bugun.atTime(9, 0), Oncelik.HIGH);
        Yapilacak g2 = yeniTamamlanan("B", bugun.minusDays(1).atTime(10, 0), Oncelik.MEDIUM);
        Yapilacak g3 = yeniTamamlanan("C", bugun.minusDays(2).atTime(11, 0), Oncelik.LOW);

        int seri = StreakCalculator.guncelSeri(List.of(g1, g2, g3), bugun);

        assertEquals(3, seri);
    }

    @Test
    void enUretkenGunVeBaskinOncelikDoner() {
        LocalDate bugun = LocalDate.of(2026, 3, 10);

        Yapilacak pazartesi1 = yeniTamamlanan("P1", LocalDateTime.of(2026, 3, 9, 9, 0), Oncelik.HIGH);
        Yapilacak pazartesi2 = yeniTamamlanan("P2", LocalDateTime.of(2026, 3, 9, 12, 0), Oncelik.HIGH);
        Yapilacak sali = yeniTamamlanan("S1", LocalDateTime.of(2026, 3, 10, 8, 30), Oncelik.MEDIUM);

        String gun = StreakCalculator.enUretkenGun(List.of(pazartesi1, pazartesi2, sali));
        int adet = StreakCalculator.enUretkenGunTamamlamaAdedi(List.of(pazartesi1, pazartesi2, sali));
        Oncelik baskin = StreakCalculator.haftaninBaskinOnceligi(List.of(pazartesi1, pazartesi2, sali), bugun);

        assertEquals("Pazartesi", gun);
        assertEquals(2, adet);
        assertEquals(Oncelik.HIGH, baskin);
    }

    @Test
    void veriYoksaNullVeSifirDoner() {
        LocalDate bugun = LocalDate.of(2026, 3, 10);

        assertEquals(0, StreakCalculator.guncelSeri(List.of(), bugun));
        assertEquals(0, StreakCalculator.enUretkenGunTamamlamaAdedi(List.of()));
        assertNull(StreakCalculator.haftaninBaskinOnceligi(List.of(), bugun));
    }

    private Yapilacak yeniTamamlanan(String aciklama, LocalDateTime completedAt, Oncelik oncelik) {
        Yapilacak gorev = new Yapilacak(aciklama, "", completedAt.toLocalDate());
        gorev.setOncelik(oncelik);
        gorev.setCompletedAt(completedAt);
        return gorev;
    }
}

package yapilacaklarListesi.veriler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class StreakCalculator {

    private static final Locale TR_LOCALE = Locale.forLanguageTag("tr-TR");

    private StreakCalculator() {
    }

    public static int guncelSeri(List<Yapilacak> gorevler, LocalDate referansGun) {
        if (gorevler == null || gorevler.isEmpty() || referansGun == null) {
            return 0;
        }

        Set<LocalDate> tamamlanmaGunleri = gorevler.stream()
                .map(StreakCalculator::tamamlanmaTarihi)
                .filter(tarih -> tarih != null)
                .collect(Collectors.toSet());

        int seri = 0;
        LocalDate gun = referansGun;
        while (tamamlanmaGunleri.contains(gun)) {
            seri++;
            gun = gun.minusDays(1);
        }
        return seri;
    }

    public static String enUretkenGun(List<Yapilacak> gorevler) {
        if (gorevler == null || gorevler.isEmpty()) {
            return "Veri yok";
        }

        Map<DayOfWeek, Long> sayac = gorevler.stream()
                .map(StreakCalculator::tamamlanmaTarihi)
                .filter(tarih -> tarih != null)
                .collect(Collectors.groupingBy(LocalDate::getDayOfWeek, Collectors.counting()));

        return sayac.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> gunAdi(entry.getKey()))
                .orElse("Veri yok");
    }

    public static int enUretkenGunTamamlamaAdedi(List<Yapilacak> gorevler) {
        if (gorevler == null || gorevler.isEmpty()) {
            return 0;
        }
        Map<DayOfWeek, Long> sayac = gorevler.stream()
                .map(StreakCalculator::tamamlanmaTarihi)
                .filter(tarih -> tarih != null)
                .collect(Collectors.groupingBy(LocalDate::getDayOfWeek, Collectors.counting()));

        return sayac.values().stream()
                .max(Comparator.naturalOrder())
                .orElse(0L)
                .intValue();
    }

    public static Oncelik haftaninBaskinOnceligi(List<Yapilacak> gorevler, LocalDate referansGun) {
        if (gorevler == null || gorevler.isEmpty() || referansGun == null) {
            return null;
        }

        LocalDate haftaBaslangici = referansGun.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate haftaBitisi = haftaBaslangici.plusDays(6);

        Map<Oncelik, Long> sayac = new EnumMap<>(Oncelik.class);
        for (Yapilacak gorev : gorevler) {
            LocalDate tamamlanma = tamamlanmaTarihi(gorev);
            if (tamamlanma == null || tamamlanma.isBefore(haftaBaslangici) || tamamlanma.isAfter(haftaBitisi)) {
                continue;
            }
            Oncelik oncelik = gorev.getOncelik() == null ? Oncelik.MEDIUM : gorev.getOncelik();
            sayac.merge(oncelik, 1L, Long::sum);
        }

        return sayac.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static LocalDate tamamlanmaTarihi(Yapilacak gorev) {
        if (gorev == null) {
            return null;
        }
        LocalDateTime completedAt = gorev.getCompletedAt();
        if (completedAt != null) {
            return completedAt.toLocalDate();
        }
        if (gorev.isCompleted()) {
            return gorev.getTarih();
        }
        return null;
    }

    private static String gunAdi(DayOfWeek dayOfWeek) {
        String ad = dayOfWeek.getDisplayName(TextStyle.FULL, TR_LOCALE);
        return ad.substring(0, 1).toUpperCase(TR_LOCALE) + ad.substring(1);
    }
}

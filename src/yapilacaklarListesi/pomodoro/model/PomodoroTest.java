package yapilacaklarListesi.pomodoro.model;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;

public class PomodoroTest extends TestCase {

    private int kalanSaniye;
    private static int staticKalanSaniye;
    private final PomodoroEnum pomodoroEnum;

    public PomodoroTest(int kalanSaniye, PomodoroEnum pomodoroEnum) {
        this.kalanSaniye = kalanSaniye;
        this.pomodoroEnum = pomodoroEnum;
    }

    public void testGetPomodoroEnum() {
    }

    public void testTik() {
        kalanSaniye--;
    }

    public void testGetKalanSaniye() {

    }

    public void testTestToString() {
        // Pomodoro 25 dk yani Timer'ı 1500 saniye, ancak dakika:saniye olarak burada gösteriyoruz.
        long minute = TimeUnit.SECONDS.toMinutes(this.kalanSaniye) - (TimeUnit.SECONDS.toHours(this.kalanSaniye) * 60);
        long second = TimeUnit.SECONDS.toSeconds(this.kalanSaniye) - (TimeUnit.SECONDS.toMinutes(this.kalanSaniye) * 60);
        System.out.println(minute + ":" + second); // 25:00 | 24:59
    }

    public void testKaydet() {
    }
}
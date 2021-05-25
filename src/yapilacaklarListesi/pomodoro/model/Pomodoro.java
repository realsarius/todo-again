package yapilacaklarListesi.pomodoro.model;

import java.util.concurrent.TimeUnit;

public class Pomodoro {
    private int kalanSaniye;
    private static int staticKalanSaniye;
    private final PomodoroEnum pomodoroEnum; // final = sadece bir kez atanabilir. 25dk pomodoro. 5dk dinlen.

    public Pomodoro(PomodoroEnum pomodoroEnum) {
        this.pomodoroEnum = pomodoroEnum;
        kalanSaniye = pomodoroEnum.getToplamSaniye();
        staticKalanSaniye = pomodoroEnum.getToplamSaniye();
    }

    public PomodoroEnum getPomodoroEnum() {
        return pomodoroEnum;
    }

    public void tik() { // kalanSaniye = kalanSaniye - 1;
        kalanSaniye--;
    }

    public int getKalanSaniye() {
        return kalanSaniye;
    }

    @Override
    public String toString() {
        // Pomodoro 25 dk yani Timer'ı 1500 saniye, ancak dakika:saniye olarak burada gösteriyoruz.
        long minute = TimeUnit.SECONDS.toMinutes(this.kalanSaniye) - (TimeUnit.SECONDS.toHours(this.kalanSaniye) * 60);
        long second = TimeUnit.SECONDS.toSeconds(this.kalanSaniye) - (TimeUnit.SECONDS.toMinutes(this.kalanSaniye) * 60);
        System.out.println(minute + ":" + second); // 25:00 | 24:59
        return minute + ":" + second;
    }

    public void kaydet() {
        System.out.printf("Kaydediliyor: %s %n", this);
    }
}

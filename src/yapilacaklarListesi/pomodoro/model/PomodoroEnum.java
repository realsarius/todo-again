package yapilacaklarListesi.pomodoro.model;

// Bu enum sınıfında sabit sayılar tanıtıldı.
public enum PomodoroEnum {
    FOCUS(25 * 60), // 25 * 60 = 1500 saniye = 25 dk.
    BREAK(5 * 60); // 5 * 60 = 300 saniye = 5 dk.

    private final int toplamSaniye;

    PomodoroEnum(int toplamSaniye) {
        this.toplamSaniye = toplamSaniye;
    }

    public int getToplamSaniye() {
        return toplamSaniye;
    }
}

package yapilacaklarListesi.pomodoro.model;

public enum AttemptKind {
    FOCUS(25 * 60),
    BREAK(5 * 60);

    private final int mTotalSeconds;

    AttemptKind(int totalSeconds) {
        mTotalSeconds = totalSeconds;
    }

    public int getTotalSeconds() {
        return mTotalSeconds;
    }
}

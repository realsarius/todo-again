package yapilacaklarListesi.pomodoro.model;

import java.util.concurrent.TimeUnit;

public class Attempt {
    private int mRemainingSeconds;
    private final AttemptKind mKind;

    public Attempt(AttemptKind kind) {
        mKind = kind;
        mRemainingSeconds = kind.getTotalSeconds();
    }

    public AttemptKind getKind() {
        return mKind;
    }

    public void tick() {
        mRemainingSeconds--;
    }

    @Override
    public String toString() {
        long minute = TimeUnit.SECONDS.toMinutes(this.mRemainingSeconds) - (TimeUnit.SECONDS.toHours(this.mRemainingSeconds)* 60);
        long second = TimeUnit.SECONDS.toSeconds(this.mRemainingSeconds) - (TimeUnit.SECONDS.toMinutes(this.mRemainingSeconds) *60);
        System.out.println(minute + ":" + second);
        return minute + ":" + second;
    }

    public void save() {
        System.out.printf("Saving: %s %n", this);
    }
}

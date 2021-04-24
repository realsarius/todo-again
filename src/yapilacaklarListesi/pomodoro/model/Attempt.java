package yapilacaklarListesi.pomodoro.model;

import java.util.concurrent.TimeUnit;

public class Attempt {
    private String mMessage;
    private int mRemainingSeconds;
    private AttemptKind mKind;

    public Attempt(AttemptKind kind, String message) {
        mKind = kind;
        mMessage = message;
        mRemainingSeconds = kind.getTotalSeconds();
    }

    public AttemptKind getKind() {
        return mKind;
    }

    public String getMessage() {
        return mMessage;
    }

    public int getRemainingSeconds() {
        return mRemainingSeconds;
    }

    public void setMessage(String message) {
        mMessage = message;
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

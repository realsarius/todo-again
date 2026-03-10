package yapilacaklarListesi.settings;

import java.util.Locale;
import java.util.Objects;
import java.util.prefs.Preferences;

public class SettingsManager {

    public enum ThemeMode {
        LIGHT("light"),
        DARK("dark"),
        SYSTEM("system");

        private final String value;

        ThemeMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ThemeMode fromValue(String value) {
            if (value == null || value.isBlank()) {
                return SYSTEM;
            }
            for (ThemeMode mode : values()) {
                if (mode.value.equalsIgnoreCase(value)) {
                    return mode;
                }
            }
            return SYSTEM;
        }
    }

    public enum LanguageOption {
        TR("tr"),
        EN("en");

        private final String value;

        LanguageOption(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static LanguageOption fromValue(String value) {
            if (value == null || value.isBlank()) {
                return TR;
            }
            for (LanguageOption option : values()) {
                if (option.value.equalsIgnoreCase(value)) {
                    return option;
                }
            }
            return TR;
        }
    }

    public enum BackupInterval {
        DAILY("daily"),
        WEEKLY("weekly");

        private final String value;

        BackupInterval(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static BackupInterval fromValue(String value) {
            if (value == null || value.isBlank()) {
                return DAILY;
            }
            for (BackupInterval interval : values()) {
                if (interval.value.equalsIgnoreCase(value)) {
                    return interval;
                }
            }
            return DAILY;
        }
    }

    public static final String KEY_THEME_MODE = "settings.theme.mode";
    public static final String KEY_LANGUAGE = "settings.language";

    public static final String KEY_POMODORO_FOCUS = "pomodoro.focus.minutes";
    public static final String KEY_POMODORO_SHORT_BREAK = "pomodoro.shortbreak.minutes";
    public static final String KEY_POMODORO_LONG_BREAK = "pomodoro.longbreak.minutes";
    public static final String KEY_POMODORO_LONG_BREAK_INTERVAL = "pomodoro.longbreak.interval";
    public static final String KEY_POMODORO_SOUND = "pomodoro.sound.enabled";

    public static final String KEY_NOTIFY_DESKTOP = "notify.desktop.enabled";
    public static final String KEY_NOTIFY_TASK_REMINDER = "notify.task.reminder.enabled";
    public static final String KEY_NOTIFY_SOUND = "notify.sound.enabled";
    public static final String KEY_NOTIFY_QUIET_ENABLED = "notify.quiet.enabled";
    public static final String KEY_NOTIFY_QUIET_START = "notify.quiet.start";
    public static final String KEY_NOTIFY_QUIET_END = "notify.quiet.end";

    public static final String KEY_DATA_AUTOBACKUP_ENABLED = "data.autobackup.enabled";
    public static final String KEY_DATA_AUTOBACKUP_INTERVAL = "data.autobackup.interval";

    public static final String KEY_UPDATE_AUTO_CHECK = "update.autocheck.enabled";
    public static final String KEY_UPDATE_LAST_CHECK_EPOCH = "update.last.check.epoch";

    public static final int DEFAULT_POMODORO_FOCUS = 25;
    public static final int DEFAULT_POMODORO_SHORT_BREAK = 5;
    public static final int DEFAULT_POMODORO_LONG_BREAK = 15;
    public static final int DEFAULT_POMODORO_LONG_BREAK_INTERVAL = 4;

    public static final String DEFAULT_QUIET_START = "22:00";
    public static final String DEFAULT_QUIET_END = "08:00";

    private final Preferences preferences;

    public SettingsManager() {
        this.preferences = Preferences.userNodeForPackage(SettingsManager.class);
    }

    public ThemeMode getThemeMode() {
        return ThemeMode.fromValue(preferences.get(KEY_THEME_MODE, ThemeMode.SYSTEM.getValue()));
    }

    public void setThemeMode(ThemeMode mode) {
        ThemeMode safeMode = mode == null ? ThemeMode.SYSTEM : mode;
        preferences.put(KEY_THEME_MODE, safeMode.getValue());
    }

    public LanguageOption getLanguage() {
        String varsayilan = Locale.getDefault().getLanguage();
        if (!Objects.equals(varsayilan, "tr") && !Objects.equals(varsayilan, "en")) {
            varsayilan = "tr";
        }
        return LanguageOption.fromValue(preferences.get(KEY_LANGUAGE, varsayilan));
    }

    public void setLanguage(LanguageOption languageOption) {
        LanguageOption safeOption = languageOption == null ? LanguageOption.TR : languageOption;
        preferences.put(KEY_LANGUAGE, safeOption.getValue());
    }

    public int getPomodoroFocusMinutes() {
        return sinirla(preferences.getInt(KEY_POMODORO_FOCUS, DEFAULT_POMODORO_FOCUS), 1, 120);
    }

    public void setPomodoroFocusMinutes(int minutes) {
        preferences.putInt(KEY_POMODORO_FOCUS, sinirla(minutes, 1, 120));
    }

    public int getPomodoroShortBreakMinutes() {
        return sinirla(preferences.getInt(KEY_POMODORO_SHORT_BREAK, DEFAULT_POMODORO_SHORT_BREAK), 1, 30);
    }

    public void setPomodoroShortBreakMinutes(int minutes) {
        preferences.putInt(KEY_POMODORO_SHORT_BREAK, sinirla(minutes, 1, 30));
    }

    public int getPomodoroLongBreakMinutes() {
        return sinirla(preferences.getInt(KEY_POMODORO_LONG_BREAK, DEFAULT_POMODORO_LONG_BREAK), 1, 60);
    }

    public void setPomodoroLongBreakMinutes(int minutes) {
        preferences.putInt(KEY_POMODORO_LONG_BREAK, sinirla(minutes, 1, 60));
    }

    public int getPomodoroLongBreakInterval() {
        return sinirla(preferences.getInt(KEY_POMODORO_LONG_BREAK_INTERVAL, DEFAULT_POMODORO_LONG_BREAK_INTERVAL), 1, 12);
    }

    public void setPomodoroLongBreakInterval(int value) {
        preferences.putInt(KEY_POMODORO_LONG_BREAK_INTERVAL, sinirla(value, 1, 12));
    }

    public boolean isPomodoroSoundEnabled() {
        return preferences.getBoolean(KEY_POMODORO_SOUND, true);
    }

    public void setPomodoroSoundEnabled(boolean enabled) {
        preferences.putBoolean(KEY_POMODORO_SOUND, enabled);
    }

    public boolean isDesktopNotificationEnabled() {
        return preferences.getBoolean(KEY_NOTIFY_DESKTOP, true);
    }

    public void setDesktopNotificationEnabled(boolean enabled) {
        preferences.putBoolean(KEY_NOTIFY_DESKTOP, enabled);
    }

    public boolean isTaskReminderEnabled() {
        return preferences.getBoolean(KEY_NOTIFY_TASK_REMINDER, true);
    }

    public void setTaskReminderEnabled(boolean enabled) {
        preferences.putBoolean(KEY_NOTIFY_TASK_REMINDER, enabled);
    }

    public boolean isNotificationSoundEnabled() {
        return preferences.getBoolean(KEY_NOTIFY_SOUND, true);
    }

    public void setNotificationSoundEnabled(boolean enabled) {
        preferences.putBoolean(KEY_NOTIFY_SOUND, enabled);
    }

    public boolean isQuietHoursEnabled() {
        return preferences.getBoolean(KEY_NOTIFY_QUIET_ENABLED, false);
    }

    public void setQuietHoursEnabled(boolean enabled) {
        preferences.putBoolean(KEY_NOTIFY_QUIET_ENABLED, enabled);
    }

    public String getQuietStart() {
        return saatDegeriniDogrula(preferences.get(KEY_NOTIFY_QUIET_START, DEFAULT_QUIET_START), DEFAULT_QUIET_START);
    }

    public void setQuietStart(String time) {
        preferences.put(KEY_NOTIFY_QUIET_START, saatDegeriniDogrula(time, DEFAULT_QUIET_START));
    }

    public String getQuietEnd() {
        return saatDegeriniDogrula(preferences.get(KEY_NOTIFY_QUIET_END, DEFAULT_QUIET_END), DEFAULT_QUIET_END);
    }

    public void setQuietEnd(String time) {
        preferences.put(KEY_NOTIFY_QUIET_END, saatDegeriniDogrula(time, DEFAULT_QUIET_END));
    }

    public boolean isAutoBackupEnabled() {
        return preferences.getBoolean(KEY_DATA_AUTOBACKUP_ENABLED, false);
    }

    public void setAutoBackupEnabled(boolean enabled) {
        preferences.putBoolean(KEY_DATA_AUTOBACKUP_ENABLED, enabled);
    }

    public BackupInterval getAutoBackupInterval() {
        return BackupInterval.fromValue(preferences.get(KEY_DATA_AUTOBACKUP_INTERVAL, BackupInterval.DAILY.getValue()));
    }

    public void setAutoBackupInterval(BackupInterval interval) {
        BackupInterval safeInterval = interval == null ? BackupInterval.DAILY : interval;
        preferences.put(KEY_DATA_AUTOBACKUP_INTERVAL, safeInterval.getValue());
    }

    public boolean isAutoUpdateCheckEnabled() {
        return preferences.getBoolean(KEY_UPDATE_AUTO_CHECK, true);
    }

    public void setAutoUpdateCheckEnabled(boolean enabled) {
        preferences.putBoolean(KEY_UPDATE_AUTO_CHECK, enabled);
    }

    public long getLastUpdateCheckEpoch() {
        return preferences.getLong(KEY_UPDATE_LAST_CHECK_EPOCH, 0L);
    }

    public void setLastUpdateCheckEpoch(long epochMillis) {
        preferences.putLong(KEY_UPDATE_LAST_CHECK_EPOCH, Math.max(epochMillis, 0L));
    }

    private int sinirla(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String saatDegeriniDogrula(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        if (trimmed.matches("^(?:[01]\\d|2[0-3]):[0-5]\\d$")) {
            return trimmed;
        }
        return fallback;
    }
}

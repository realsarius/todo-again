package yapilacaklarListesi.settings;

public interface SettingsSection {

    String getSectionId();

    void load(SettingsManager manager);

    void save(SettingsManager manager);

    void rollback();

    boolean isDirty();
}

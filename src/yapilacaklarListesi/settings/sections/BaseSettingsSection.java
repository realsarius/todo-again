package yapilacaklarListesi.settings.sections;

import yapilacaklarListesi.settings.SettingsManager;
import yapilacaklarListesi.settings.SettingsSection;

public abstract class BaseSettingsSection implements SettingsSection {

    private final String sectionId;
    private boolean dirty;

    protected BaseSettingsSection(String sectionId) {
        this.sectionId = sectionId;
    }

    @Override
    public String getSectionId() {
        return sectionId;
    }

    @Override
    public void load(SettingsManager manager) {
        dirty = false;
    }

    @Override
    public void save(SettingsManager manager) {
        dirty = false;
    }

    @Override
    public void rollback() {
        dirty = false;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    protected void markDirty() {
        dirty = true;
    }

    protected void clearDirty() {
        dirty = false;
    }
}

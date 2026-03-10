package yapilacaklarListesi.settings.sections;

import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import yapilacaklarListesi.settings.SettingsManager;

public class AppearanceSection extends BaseSettingsSection {

    public static final String SECTION_ID = "appearance";
    private static final String DIL_TR = "Türkçe";
    private static final String DIL_EN = "English";

    private final ToggleGroup temaToggleGroup;
    private final RadioButton temaAcikRadio;
    private final RadioButton temaKoyuRadio;
    private final RadioButton temaSistemRadio;
    private final ComboBox<String> dilComboBox;
    private final Runnable dilDegisiklikUyarisi;

    private SettingsManager.ThemeMode ilkTema;
    private SettingsManager.LanguageOption ilkDil;
    private boolean modelYukleniyor;

    public AppearanceSection(
            ToggleGroup temaToggleGroup,
            RadioButton temaAcikRadio,
            RadioButton temaKoyuRadio,
            RadioButton temaSistemRadio,
            ComboBox<String> dilComboBox,
            Runnable dilDegisiklikUyarisi
    ) {
        super(SECTION_ID);
        this.temaToggleGroup = temaToggleGroup;
        this.temaAcikRadio = temaAcikRadio;
        this.temaKoyuRadio = temaKoyuRadio;
        this.temaSistemRadio = temaSistemRadio;
        this.dilComboBox = dilComboBox;
        this.dilDegisiklikUyarisi = dilDegisiklikUyarisi;

        this.temaAcikRadio.setUserData(SettingsManager.ThemeMode.LIGHT);
        this.temaKoyuRadio.setUserData(SettingsManager.ThemeMode.DARK);
        this.temaSistemRadio.setUserData(SettingsManager.ThemeMode.SYSTEM);

        this.temaAcikRadio.setToggleGroup(temaToggleGroup);
        this.temaKoyuRadio.setToggleGroup(temaToggleGroup);
        this.temaSistemRadio.setToggleGroup(temaToggleGroup);

        this.dilComboBox.getItems().setAll(DIL_TR, DIL_EN);

        temaToggleGroup.selectedToggleProperty().addListener((obs, eski, yeni) -> {
            if (modelYukleniyor || yeni == null) {
                return;
            }
            markDirty();
        });
        dilComboBox.valueProperty().addListener((obs, eski, yeni) -> {
            if (modelYukleniyor || yeni == null || yeni.equals(eski)) {
                return;
            }
            markDirty();
            dilDegisiklikUyarisi.run();
        });
    }

    @Override
    public void load(SettingsManager manager) {
        modelYukleniyor = true;
        try {
            ilkTema = manager.getThemeMode();
            ilkDil = manager.getLanguage();
            temayiSec(ilkTema);
            diliSec(ilkDil);
            clearDirty();
        } finally {
            modelYukleniyor = false;
        }
    }

    @Override
    public void save(SettingsManager manager) {
        manager.setThemeMode(seciliTema());
        manager.setLanguage(seciliDil());

        ilkTema = manager.getThemeMode();
        ilkDil = manager.getLanguage();
        clearDirty();
    }

    @Override
    public void rollback() {
        modelYukleniyor = true;
        try {
            temayiSec(ilkTema);
            diliSec(ilkDil);
            clearDirty();
        } finally {
            modelYukleniyor = false;
        }
    }

    private void temayiSec(SettingsManager.ThemeMode mode) {
        SettingsManager.ThemeMode secim = mode == null ? SettingsManager.ThemeMode.SYSTEM : mode;
        switch (secim) {
            case LIGHT -> temaToggleGroup.selectToggle(temaAcikRadio);
            case DARK -> temaToggleGroup.selectToggle(temaKoyuRadio);
            case SYSTEM -> temaToggleGroup.selectToggle(temaSistemRadio);
        }
    }

    private void diliSec(SettingsManager.LanguageOption option) {
        SettingsManager.LanguageOption secim = option == null ? SettingsManager.LanguageOption.TR : option;
        if (secim == SettingsManager.LanguageOption.EN) {
            dilComboBox.setValue(DIL_EN);
            return;
        }
        dilComboBox.setValue(DIL_TR);
    }

    private SettingsManager.ThemeMode seciliTema() {
        Toggle toggle = temaToggleGroup.getSelectedToggle();
        if (toggle == null || toggle.getUserData() == null) {
            return SettingsManager.ThemeMode.SYSTEM;
        }
        return (SettingsManager.ThemeMode) toggle.getUserData();
    }

    private SettingsManager.LanguageOption seciliDil() {
        String secim = dilComboBox.getValue();
        if (DIL_EN.equals(secim)) {
            return SettingsManager.LanguageOption.EN;
        }
        return SettingsManager.LanguageOption.TR;
    }
}

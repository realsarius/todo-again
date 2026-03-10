package yapilacaklarListesi.settings.sections;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import yapilacaklarListesi.settings.SettingsManager;

public class PomodoroSection extends BaseSettingsSection {

    public static final String SECTION_ID = "pomodoro";
    private final Spinner<Integer> calismaSpinner;
    private final Spinner<Integer> kisaMolaSpinner;
    private final Spinner<Integer> uzunMolaSpinner;
    private final Spinner<Integer> uzunMolaAraligiSpinner;
    private final CheckBox sesBildirimCheckBox;

    private int ilkCalisma;
    private int ilkKisaMola;
    private int ilkUzunMola;
    private int ilkUzunMolaAraligi;
    private boolean ilkSesBildirim;
    private boolean modelYukleniyor;

    public PomodoroSection(
            Spinner<Integer> calismaSpinner,
            Spinner<Integer> kisaMolaSpinner,
            Spinner<Integer> uzunMolaSpinner,
            Spinner<Integer> uzunMolaAraligiSpinner,
            CheckBox sesBildirimCheckBox
    ) {
        super(SECTION_ID);
        this.calismaSpinner = calismaSpinner;
        this.kisaMolaSpinner = kisaMolaSpinner;
        this.uzunMolaSpinner = uzunMolaSpinner;
        this.uzunMolaAraligiSpinner = uzunMolaAraligiSpinner;
        this.sesBildirimCheckBox = sesBildirimCheckBox;

        configureSpinner(calismaSpinner, 1, 120, SettingsManager.DEFAULT_POMODORO_FOCUS);
        configureSpinner(kisaMolaSpinner, 1, 30, SettingsManager.DEFAULT_POMODORO_SHORT_BREAK);
        configureSpinner(uzunMolaSpinner, 1, 60, SettingsManager.DEFAULT_POMODORO_LONG_BREAK);
        configureSpinner(uzunMolaAraligiSpinner, 1, 12, SettingsManager.DEFAULT_POMODORO_LONG_BREAK_INTERVAL);

        calismaSpinner.valueProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
        kisaMolaSpinner.valueProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
        uzunMolaSpinner.valueProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
        uzunMolaAraligiSpinner.valueProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
        sesBildirimCheckBox.selectedProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
    }

    @Override
    public void load(SettingsManager manager) {
        modelYukleniyor = true;
        try {
            ilkCalisma = manager.getPomodoroFocusMinutes();
            ilkKisaMola = manager.getPomodoroShortBreakMinutes();
            ilkUzunMola = manager.getPomodoroLongBreakMinutes();
            ilkUzunMolaAraligi = manager.getPomodoroLongBreakInterval();
            ilkSesBildirim = manager.isPomodoroSoundEnabled();

            calismaSpinner.getValueFactory().setValue(ilkCalisma);
            kisaMolaSpinner.getValueFactory().setValue(ilkKisaMola);
            uzunMolaSpinner.getValueFactory().setValue(ilkUzunMola);
            uzunMolaAraligiSpinner.getValueFactory().setValue(ilkUzunMolaAraligi);
            sesBildirimCheckBox.setSelected(ilkSesBildirim);
            clearDirty();
        } finally {
            modelYukleniyor = false;
        }
    }

    @Override
    public void save(SettingsManager manager) {
        manager.setPomodoroFocusMinutes(calismaSpinner.getValue());
        manager.setPomodoroShortBreakMinutes(kisaMolaSpinner.getValue());
        manager.setPomodoroLongBreakMinutes(uzunMolaSpinner.getValue());
        manager.setPomodoroLongBreakInterval(uzunMolaAraligiSpinner.getValue());
        manager.setPomodoroSoundEnabled(sesBildirimCheckBox.isSelected());

        ilkCalisma = manager.getPomodoroFocusMinutes();
        ilkKisaMola = manager.getPomodoroShortBreakMinutes();
        ilkUzunMola = manager.getPomodoroLongBreakMinutes();
        ilkUzunMolaAraligi = manager.getPomodoroLongBreakInterval();
        ilkSesBildirim = manager.isPomodoroSoundEnabled();
        clearDirty();
    }

    @Override
    public void rollback() {
        modelYukleniyor = true;
        try {
            calismaSpinner.getValueFactory().setValue(ilkCalisma);
            kisaMolaSpinner.getValueFactory().setValue(ilkKisaMola);
            uzunMolaSpinner.getValueFactory().setValue(ilkUzunMola);
            uzunMolaAraligiSpinner.getValueFactory().setValue(ilkUzunMolaAraligi);
            sesBildirimCheckBox.setSelected(ilkSesBildirim);
            clearDirty();
        } finally {
            modelYukleniyor = false;
        }
    }

    private void configureSpinner(Spinner<Integer> spinner, int min, int max, int initial) {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial);
        spinner.setValueFactory(valueFactory);
        spinner.setEditable(true);
    }

    private void modelDegisti(Object eski, Object yeni) {
        if (modelYukleniyor || (eski != null && eski.equals(yeni))) {
            return;
        }
        markDirty();
    }
}

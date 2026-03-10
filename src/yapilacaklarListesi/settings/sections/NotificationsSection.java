package yapilacaklarListesi.settings.sections;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import yapilacaklarListesi.settings.SettingsManager;

public class NotificationsSection extends BaseSettingsSection {

    public static final String SECTION_ID = "notifications";
    private final CheckBox masaustuBildirimCheckBox;
    private final CheckBox gorevHatirlaticiCheckBox;
    private final CheckBox bildirimSesiCheckBox;
    private final CheckBox sessizSaatlerCheckBox;
    private final Spinner<Integer> baslangicSaatSpinner;
    private final Spinner<Integer> baslangicDakikaSpinner;
    private final Spinner<Integer> bitisSaatSpinner;
    private final Spinner<Integer> bitisDakikaSpinner;

    private boolean ilkMasaustuBildirim;
    private boolean ilkGorevHatirlatici;
    private boolean ilkBildirimSesi;
    private boolean ilkSessizSaatler;
    private String ilkBaslangic;
    private String ilkBitis;
    private boolean modelYukleniyor;

    public NotificationsSection(
            CheckBox masaustuBildirimCheckBox,
            CheckBox gorevHatirlaticiCheckBox,
            CheckBox bildirimSesiCheckBox,
            CheckBox sessizSaatlerCheckBox,
            Spinner<Integer> baslangicSaatSpinner,
            Spinner<Integer> baslangicDakikaSpinner,
            Spinner<Integer> bitisSaatSpinner,
            Spinner<Integer> bitisDakikaSpinner
    ) {
        super(SECTION_ID);
        this.masaustuBildirimCheckBox = masaustuBildirimCheckBox;
        this.gorevHatirlaticiCheckBox = gorevHatirlaticiCheckBox;
        this.bildirimSesiCheckBox = bildirimSesiCheckBox;
        this.sessizSaatlerCheckBox = sessizSaatlerCheckBox;
        this.baslangicSaatSpinner = baslangicSaatSpinner;
        this.baslangicDakikaSpinner = baslangicDakikaSpinner;
        this.bitisSaatSpinner = bitisSaatSpinner;
        this.bitisDakikaSpinner = bitisDakikaSpinner;

        configureSaatSpinner(baslangicSaatSpinner, 0, 23);
        configureSaatSpinner(baslangicDakikaSpinner, 0, 59);
        configureSaatSpinner(bitisSaatSpinner, 0, 23);
        configureSaatSpinner(bitisDakikaSpinner, 0, 59);

        masaustuBildirimCheckBox.selectedProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
        gorevHatirlaticiCheckBox.selectedProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
        bildirimSesiCheckBox.selectedProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
        sessizSaatlerCheckBox.selectedProperty().addListener((obs, eski, yeni) -> {
            if (modelYukleniyor || (eski != null && eski.equals(yeni))) {
                return;
            }
            sessizSaatKontrolleriniGuncelle();
            markDirty();
        });
        baslangicSaatSpinner.valueProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
        baslangicDakikaSpinner.valueProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
        bitisSaatSpinner.valueProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
        bitisDakikaSpinner.valueProperty().addListener((obs, eski, yeni) -> modelDegisti(eski, yeni));
    }

    @Override
    public void load(SettingsManager manager) {
        modelYukleniyor = true;
        try {
            ilkMasaustuBildirim = manager.isDesktopNotificationEnabled();
            ilkGorevHatirlatici = manager.isTaskReminderEnabled();
            ilkBildirimSesi = manager.isNotificationSoundEnabled();
            ilkSessizSaatler = manager.isQuietHoursEnabled();
            ilkBaslangic = manager.getQuietStart();
            ilkBitis = manager.getQuietEnd();

            masaustuBildirimCheckBox.setSelected(ilkMasaustuBildirim);
            gorevHatirlaticiCheckBox.setSelected(ilkGorevHatirlatici);
            bildirimSesiCheckBox.setSelected(ilkBildirimSesi);
            sessizSaatlerCheckBox.setSelected(ilkSessizSaatler);
            spinneraSaatiUygula(ilkBaslangic, baslangicSaatSpinner, baslangicDakikaSpinner);
            spinneraSaatiUygula(ilkBitis, bitisSaatSpinner, bitisDakikaSpinner);
            sessizSaatKontrolleriniGuncelle();
            clearDirty();
        } finally {
            modelYukleniyor = false;
        }
    }

    @Override
    public void save(SettingsManager manager) {
        manager.setDesktopNotificationEnabled(masaustuBildirimCheckBox.isSelected());
        manager.setTaskReminderEnabled(gorevHatirlaticiCheckBox.isSelected());
        manager.setNotificationSoundEnabled(bildirimSesiCheckBox.isSelected());
        manager.setQuietHoursEnabled(sessizSaatlerCheckBox.isSelected());
        manager.setQuietStart(spinnerdanSaatOlustur(baslangicSaatSpinner, baslangicDakikaSpinner));
        manager.setQuietEnd(spinnerdanSaatOlustur(bitisSaatSpinner, bitisDakikaSpinner));

        ilkMasaustuBildirim = manager.isDesktopNotificationEnabled();
        ilkGorevHatirlatici = manager.isTaskReminderEnabled();
        ilkBildirimSesi = manager.isNotificationSoundEnabled();
        ilkSessizSaatler = manager.isQuietHoursEnabled();
        ilkBaslangic = manager.getQuietStart();
        ilkBitis = manager.getQuietEnd();
        clearDirty();
    }

    @Override
    public void rollback() {
        modelYukleniyor = true;
        try {
            masaustuBildirimCheckBox.setSelected(ilkMasaustuBildirim);
            gorevHatirlaticiCheckBox.setSelected(ilkGorevHatirlatici);
            bildirimSesiCheckBox.setSelected(ilkBildirimSesi);
            sessizSaatlerCheckBox.setSelected(ilkSessizSaatler);
            spinneraSaatiUygula(ilkBaslangic, baslangicSaatSpinner, baslangicDakikaSpinner);
            spinneraSaatiUygula(ilkBitis, bitisSaatSpinner, bitisDakikaSpinner);
            sessizSaatKontrolleriniGuncelle();
            clearDirty();
        } finally {
            modelYukleniyor = false;
        }
    }

    private void configureSaatSpinner(Spinner<Integer> spinner, int min, int max) {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, min);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
    }

    private void spinneraSaatiUygula(String saat, Spinner<Integer> saatSpinner, Spinner<Integer> dakikaSpinner) {
        String[] parcalar = (saat == null ? "00:00" : saat).split(":");
        int saatDegeri = parseSayisal(parcalar, 0, 0);
        int dakikaDegeri = parseSayisal(parcalar, 1, 0);
        saatSpinner.getValueFactory().setValue(Math.max(0, Math.min(23, saatDegeri)));
        dakikaSpinner.getValueFactory().setValue(Math.max(0, Math.min(59, dakikaDegeri)));
    }

    private int parseSayisal(String[] parcalar, int index, int varsayilan) {
        if (parcalar == null || index < 0 || index >= parcalar.length) {
            return varsayilan;
        }
        try {
            return Integer.parseInt(parcalar[index]);
        } catch (NumberFormatException e) {
            return varsayilan;
        }
    }

    private String spinnerdanSaatOlustur(Spinner<Integer> saatSpinner, Spinner<Integer> dakikaSpinner) {
        return String.format("%02d:%02d", saatSpinner.getValue(), dakikaSpinner.getValue());
    }

    private void sessizSaatKontrolleriniGuncelle() {
        boolean etkin = sessizSaatlerCheckBox.isSelected();
        baslangicSaatSpinner.setDisable(!etkin);
        baslangicDakikaSpinner.setDisable(!etkin);
        bitisSaatSpinner.setDisable(!etkin);
        bitisDakikaSpinner.setDisable(!etkin);
    }

    private void modelDegisti(Object eski, Object yeni) {
        if (modelYukleniyor || (eski != null && eski.equals(yeni))) {
            return;
        }
        markDirty();
    }
}

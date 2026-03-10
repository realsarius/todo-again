package yapilacaklarListesi;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import yapilacaklarListesi.service.TaskService;
import yapilacaklarListesi.veriler.Oncelik;
import yapilacaklarListesi.veriler.StreakCalculator;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

public class StatsController {

    private static final Locale TR_LOCALE = Locale.forLanguageTag("tr-TR");
    private static final DateTimeFormatter CHART_LABEL_FORMAT = DateTimeFormatter.ofPattern("d MMM", TR_LOCALE);

    @FXML private Label bugunDegerLabel;
    @FXML private Label bugunOranLabel;
    @FXML private ProgressBar bugunProgress;

    @FXML private Label haftaDegerLabel;
    @FXML private Label haftaOranLabel;
    @FXML private ProgressBar haftaProgress;

    @FXML private Label ayDegerLabel;
    @FXML private Label ayOranLabel;
    @FXML private ProgressBar ayProgress;

    @FXML private Label toplamDegerLabel;
    @FXML private Label toplamOranLabel;
    @FXML private ProgressBar toplamProgress;

    @FXML private ProgressBar highPriorityProgress;
    @FXML private ProgressBar mediumPriorityProgress;
    @FXML private ProgressBar lowPriorityProgress;
    @FXML private Label highPriorityCountLabel;
    @FXML private Label mediumPriorityCountLabel;
    @FXML private Label lowPriorityCountLabel;

    @FXML private StackedBarChart<String, Number> yediGunChart;
    @FXML private CategoryAxis yediGunXAxis;
    @FXML private NumberAxis yediGunYAxis;

    @FXML private Label streakDegerLabel;
    @FXML private Label enUretkenGunLabel;
    @FXML private Label enUretkenGunDetayLabel;
    @FXML private Label baskinOncelikLabel;
    @FXML private Label baskinOncelikDetayLabel;

    private final TaskService taskService = new TaskService(YapilacakVeri.getInstance());

    @FXML
    public void initialize() {
        yediGunChart.setCategoryGap(10);
        yediGunChart.setLegendVisible(true);
        yediGunXAxis.setTickLabelRotation(0);
        yediGunYAxis.setAutoRanging(true);

        taskService.tumGorevler().addListener((ListChangeListener<Yapilacak>) change -> istatistikleriYenile());

        istatistikleriYenile();
    }

    private void istatistikleriYenile() {
        List<Yapilacak> gorevler = taskService.tumGorevler();
        LocalDate bugun = LocalDate.now();

        guncelleKartlar(gorevler, bugun);
        guncelleOncelikDagilimi(gorevler);
        guncelleYediGunGrafigi(gorevler, bugun);
        guncelleStreakVeMotivasyon(gorevler, bugun);
    }

    private void guncelleKartlar(List<Yapilacak> gorevler, LocalDate bugun) {
        LocalDate haftaBaslangic = bugun.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate haftaBitis = haftaBaslangic.plusDays(6);

        List<Yapilacak> bugunGorevleri = gorevler.stream()
                .filter(gorev -> bugun.equals(gorev.getTarih()))
                .toList();
        List<Yapilacak> haftaGorevleri = gorevler.stream()
                .filter(gorev -> !gorev.getTarih().isBefore(haftaBaslangic) && !gorev.getTarih().isAfter(haftaBitis))
                .toList();
        List<Yapilacak> ayGorevleri = gorevler.stream()
                .filter(gorev -> gorev.getTarih().getYear() == bugun.getYear() && gorev.getTarih().getMonth() == bugun.getMonth())
                .toList();

        kartGuncelle(bugunDegerLabel, bugunOranLabel, bugunProgress, bugunGorevleri);
        kartGuncelle(haftaDegerLabel, haftaOranLabel, haftaProgress, haftaGorevleri);
        kartGuncelle(ayDegerLabel, ayOranLabel, ayProgress, ayGorevleri);

        long toplamTamamlanan = gorevler.stream().filter(this::tamamlandiMi).count();
        toplamDegerLabel.setText(gorevler.size() + " görev");
        toplamOranLabel.setText(toplamTamamlanan + " tamamlandı");
        toplamProgress.setProgress(progress(toplamTamamlanan, gorevler.size()));
        progressRengiUygula(toplamProgress, progress(toplamTamamlanan, gorevler.size()));
    }

    private void kartGuncelle(Label degerLabel, Label oranLabel, ProgressBar progressBar, List<Yapilacak> gorevler) {
        long tamamlanan = gorevler.stream().filter(this::tamamlandiMi).count();
        double oran = progress(tamamlanan, gorevler.size());

        degerLabel.setText(tamamlanan + "/" + gorevler.size());
        oranLabel.setText("%" + Math.round(oran * 100));
        progressBar.setProgress(oran);
        progressRengiUygula(progressBar, oran);
    }

    private void progressRengiUygula(ProgressBar bar, double oran) {
        bar.getStyleClass().removeAll("stat-progress-good", "stat-progress-mid", "stat-progress-low");
        if (oran > 0.80) {
            bar.getStyleClass().add("stat-progress-good");
        } else if (oran > 0.50) {
            bar.getStyleClass().add("stat-progress-mid");
        } else {
            bar.getStyleClass().add("stat-progress-low");
        }
    }

    private void guncelleOncelikDagilimi(List<Yapilacak> gorevler) {
        long high = gorevler.stream().filter(gorev -> gorev.getOncelik() == Oncelik.HIGH).count();
        long medium = gorevler.stream().filter(gorev -> gorev.getOncelik() == Oncelik.MEDIUM).count();
        long low = gorevler.stream().filter(gorev -> gorev.getOncelik() == Oncelik.LOW).count();

        int toplam = gorevler.size();
        highPriorityProgress.setProgress(progress(high, toplam));
        mediumPriorityProgress.setProgress(progress(medium, toplam));
        lowPriorityProgress.setProgress(progress(low, toplam));

        highPriorityCountLabel.setText(high + " görev");
        mediumPriorityCountLabel.setText(medium + " görev");
        lowPriorityCountLabel.setText(low + " görev");
    }

    private void guncelleYediGunGrafigi(List<Yapilacak> gorevler, LocalDate bugun) {
        XYChart.Series<String, Number> tamamlananSeries = new XYChart.Series<>();
        tamamlananSeries.setName("Tamamlanan");

        XYChart.Series<String, Number> kalanSeries = new XYChart.Series<>();
        kalanSeries.setName("Tamamlanmayan");

        for (int i = 6; i >= 0; i--) {
            LocalDate gun = bugun.minusDays(i);
            String etiket = gun.format(CHART_LABEL_FORMAT);

            long gunToplam = gorevler.stream().filter(gorev -> gun.equals(gorev.getTarih())).count();
            long gunTamamlanan = gorevler.stream()
                    .filter(gorev -> gun.equals(gorev.getTarih()) && tamamlandiMi(gorev))
                    .count();

            long gunKalan = Math.max(0, gunToplam - gunTamamlanan);

            tamamlananSeries.getData().add(new XYChart.Data<>(etiket, gunTamamlanan));
            kalanSeries.getData().add(new XYChart.Data<>(etiket, gunKalan));
        }

        yediGunChart.setData(FXCollections.observableArrayList(tamamlananSeries, kalanSeries));
    }

    private void guncelleStreakVeMotivasyon(List<Yapilacak> gorevler, LocalDate bugun) {
        int streak = StreakCalculator.guncelSeri(gorevler, bugun);
        streakDegerLabel.setText("🔥 " + streak + " günlük seri");

        String enUretkenGun = StreakCalculator.enUretkenGun(gorevler);
        int enUretkenGunAdet = StreakCalculator.enUretkenGunTamamlamaAdedi(gorevler);
        enUretkenGunLabel.setText(enUretkenGun);
        if (enUretkenGunAdet > 0) {
            enUretkenGunDetayLabel.setText("Ortalama tamamlanan görev: " + enUretkenGunAdet);
        } else {
            enUretkenGunDetayLabel.setText("Henüz tamamlanan görev yok");
        }

        Oncelik baskin = StreakCalculator.haftaninBaskinOnceligi(gorevler, bugun);
        if (baskin == null) {
            baskinOncelikLabel.setText("- ");
            baskinOncelikDetayLabel.setText("Bu hafta görev tamamlanmadı");
        } else {
            baskinOncelikLabel.setText(oncelikMetni(baskin));
            baskinOncelikDetayLabel.setText("Bu hafta en çok tamamlanan öncelik");
        }
    }

    private String oncelikMetni(Oncelik oncelik) {
        return switch (oncelik) {
            case HIGH -> "Yüksek";
            case MEDIUM -> "Orta";
            case LOW -> "Düşük";
        };
    }

    private boolean tamamlandiMi(Yapilacak gorev) {
        if (gorev == null) {
            return false;
        }
        if (gorev.isCompleted()) {
            return true;
        }
        LocalDateTime completedAt = gorev.getCompletedAt();
        return completedAt != null;
    }

    private double progress(long parca, long butun) {
        if (butun <= 0) {
            return 0;
        }
        return Math.min(1.0, (double) parca / butun);
    }
}

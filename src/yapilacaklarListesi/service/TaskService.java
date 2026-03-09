package yapilacaklarListesi.service;

import javafx.collections.ObservableList;
import yapilacaklarListesi.veriler.Oncelik;
import yapilacaklarListesi.veriler.Yapilacak;
import yapilacaklarListesi.veriler.YapilacakVeri;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

public class TaskService {

    private final YapilacakVeri yapilacakVeri;

    public TaskService(YapilacakVeri yapilacakVeri) {
        this.yapilacakVeri = Objects.requireNonNull(yapilacakVeri, "yapilacakVeri bos olamaz");
    }

    public ObservableList<Yapilacak> tumGorevler() {
        return yapilacakVeri.getYapilacaklar();
    }

    public Predicate<Yapilacak> tumGorevlerFiltresi() {
        return yapilacak -> true;
    }

    public Predicate<Yapilacak> bugunGorevleriFiltresi() {
        return yapilacak -> yapilacak.getTarih().equals(LocalDate.now());
    }

    public Predicate<Yapilacak> oncelikFiltresi(Oncelik oncelik) {
        if (oncelik == null) {
            return tumGorevlerFiltresi();
        }
        return yapilacak -> yapilacak.getOncelik() == oncelik;
    }

    public void gorevEkle(Yapilacak yapilacak) {
        if (yapilacak == null) {
            return;
        }
        yapilacakVeri.yapilacakEkle(yapilacak);
    }

    public void gorevSil(Yapilacak yapilacak) {
        if (yapilacak == null) {
            return;
        }
        yapilacakVeri.yapilacakSil(yapilacak);
    }

    public void gorevDetayiGuncelle(Yapilacak yapilacak, String detay) {
        if (yapilacak == null) {
            return;
        }
        yapilacak.setDetay(detay == null ? "" : detay);
    }

    public void kaydet() throws IOException {
        yapilacakVeri.yapilacaklariKaydet();
    }

    public void gorevDetayiGuncelleVeKaydet(Yapilacak yapilacak, String detay) throws IOException {
        gorevDetayiGuncelle(yapilacak, detay);
        kaydet();
    }
}

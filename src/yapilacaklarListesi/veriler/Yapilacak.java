package yapilacaklarListesi.veriler;

import java.time.LocalDate;

public class Yapilacak {

    private String aciklama;
    private String detay;
    private LocalDate tarih;

    public Yapilacak(String aciklama, String detay, LocalDate tarih) {
        this.aciklama = aciklama;
        this.detay = detay;
        this.tarih = tarih;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getDetay() {
        return detay;
    }

    public void setDetay(String detay) {
        this.detay = detay;
    }

    public LocalDate getTarih() {
        return tarih;
    }

    public void setTarih(LocalDate tarih) {
        this.tarih = tarih;
    }

    @Override
    public String toString() {
        return this.aciklama;
    }
}

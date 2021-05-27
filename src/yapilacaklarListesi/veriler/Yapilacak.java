package yapilacaklarListesi.veriler;

import java.time.LocalDate;

public class Yapilacak {

    // Yapılacak, basit bir entity sınıfı.
    private final String aciklama;
    private String detay;
    private final LocalDate tarih;

    public Yapilacak(String aciklama, String detay, LocalDate tarih) {
        this.aciklama = aciklama;
        this.detay = detay;
        this.tarih = tarih;
    }

    public String getAciklama() {
        return aciklama;
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

    @Override
    public String toString() {
        return this.aciklama;
    }
}

package yapilacaklarListesi.veriler;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Yapilacak {

    private final String id;
    private String aciklama;
    private String detay;
    private LocalDate tarih;
    private final Instant createdAt;
    private Instant updatedAt;
    private Oncelik oncelik;
    private List<String> tags;

    public Yapilacak(String aciklama, String detay, LocalDate tarih) {
        this(UUID.randomUUID().toString(), aciklama, detay, tarih, Instant.now(), Instant.now(), Oncelik.MEDIUM, List.of());
    }

    public Yapilacak(String id,
                     String aciklama,
                     String detay,
                     LocalDate tarih,
                     Instant createdAt,
                     Instant updatedAt,
                     Oncelik oncelik,
                     List<String> tags) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.aciklama = aciklama;
        this.detay = detay == null ? "" : detay;
        this.tarih = tarih;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        this.oncelik = oncelik == null ? Oncelik.MEDIUM : oncelik;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public String getId() {
        return id;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama == null ? "" : aciklama;
        this.updatedAt = Instant.now();
    }

    public String getDetay() {
        return detay;
    }

    public void setDetay(String detay) {
        this.detay = detay;
        this.updatedAt = Instant.now();
    }

    public LocalDate getTarih() {
        return tarih;
    }

    public void setTarih(LocalDate tarih) {
        if (tarih == null) {
            return;
        }
        this.tarih = tarih;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Oncelik getOncelik() {
        return oncelik;
    }

    public void setOncelik(Oncelik oncelik) {
        this.oncelik = oncelik;
        this.updatedAt = Instant.now();
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return this.aciklama;
    }
}

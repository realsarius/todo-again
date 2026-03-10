package yapilacaklarListesi.veriler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private LocalTime dueTime;
    private LocalDateTime completedAt;
    private boolean completed;

    public Yapilacak(String aciklama, String detay, LocalDate tarih) {
        this(
                UUID.randomUUID().toString(),
                aciklama,
                detay,
                tarih,
                Instant.now(),
                Instant.now(),
                Oncelik.MEDIUM,
                List.of(),
                null,
                null,
                false
        );
    }

    public Yapilacak(String id,
                     String aciklama,
                     String detay,
                     LocalDate tarih,
                     Instant createdAt,
                     Instant updatedAt,
                     Oncelik oncelik,
                     List<String> tags) {
        this(
                id,
                aciklama,
                detay,
                tarih,
                createdAt,
                updatedAt,
                oncelik,
                tags,
                null,
                null,
                false
        );
    }

    public Yapilacak(String id,
                     String aciklama,
                     String detay,
                     LocalDate tarih,
                     Instant createdAt,
                     Instant updatedAt,
                     Oncelik oncelik,
                     List<String> tags,
                     LocalTime dueTime,
                     LocalDateTime completedAt,
                     boolean completed) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.aciklama = aciklama == null ? "" : aciklama;
        this.detay = detay == null ? "" : detay;
        this.tarih = tarih;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        this.oncelik = oncelik == null ? Oncelik.MEDIUM : oncelik;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.dueTime = dueTime;

        boolean normalizedCompleted = completed || completedAt != null;
        LocalDateTime normalizedCompletedAt = completedAt;
        if (normalizedCompleted && normalizedCompletedAt == null) {
            normalizedCompletedAt = LocalDateTime.now();
        }
        if (!normalizedCompleted) {
            normalizedCompletedAt = null;
        }

        this.completed = normalizedCompleted;
        this.completedAt = normalizedCompletedAt;
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
        this.detay = detay == null ? "" : detay;
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
        if (updatedAt == null) {
            return;
        }
        this.updatedAt = updatedAt;
    }

    public Oncelik getOncelik() {
        return oncelik;
    }

    public void setOncelik(Oncelik oncelik) {
        this.oncelik = oncelik == null ? Oncelik.MEDIUM : oncelik;
        this.updatedAt = Instant.now();
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.updatedAt = Instant.now();
    }

    public LocalTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalTime dueTime) {
        this.dueTime = dueTime;
        this.updatedAt = Instant.now();
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
        this.completed = completedAt != null;
        this.updatedAt = Instant.now();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
        if (!completed) {
            this.completedAt = null;
        }
        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return this.aciklama;
    }
}

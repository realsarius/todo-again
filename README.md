# todo-again

2021 dönemine ait JavaFX masaüstü yapılacaklar uygulaması; modern Java araç zinciri ile yeniden hayata geçirildi.

## İçindekiler

- [0. Hızlı Kurulum](#0-hızlı-kurulum)
- [1. Proje Kapsamı](#1-proje-kapsamı)
- [2. Teknoloji Yığını](#2-teknoloji-yığını)
- [3. Mimari Tasarım](#3-mimari-tasarım)
- [4. Veri Formatı](#4-veri-formatı)
- [5. Loglama ve Hata Yönetimi](#5-loglama-ve-hata-yönetimi)
- [6. Test Stratejisi](#6-test-stratejisi)
- [7. Kurulum ve Çalıştırma](#7-kurulum-ve-çalıştırma)
- [8. Paketleme ve Dağıtım](#9-paketleme-ve-dağıtım)
- [9. Lisans](#10-lisans)

---

## 0. Hızlı Kurulum

### Yerel Kurulum (JDK + Maven)

```bash
# Uygulamayı çalıştır
mvn clean javafx:run

# Testleri ve kapsama kontrolünü çalıştır
mvn clean verify
```

### Docker ile (Java/Maven kurulumu gerekmez)

```bash
# Test + kapsama kontrolü
docker compose run --rm maven clean verify

# Sadece testler
docker compose run --rm maven clean test

# Paket üretimi (test atla)
docker compose run --rm maven clean package -DskipTests
```

---

## 1. Proje Kapsamı

**Görev Yönetimi**: Görev ekleme, düzenleme, silme ve dosyaya kaydetme akışları. Başlık, detay ve tarih alanlarıyla basit bir veri modeli kullanılır.

**Kalıcılık**: Görev listesi her kapanışta `Yapilacaklar.txt` dosyasına sekme-ayrımlı format ile yazılır. Uygulama başlarken bu dosya okunur; eksik veya bozuk satırlar atlanır, uygulama çökmez.

**Pomodoro Zamanlayıcı**: Odak süre takibi için entegre Pomodoro modeli. Süre dolduğunda ses efekti çalar.

**Ses Efektleri**: Düğme tıklamaları ve diyalog aksiyonları için özel ses dosyaları (`cancelButon.mp3`, `dialog.mp3`, `dialogOkButon.mp3`).

**Canlandırma Amacı**: Proje 2021 kodunu güncel Java araç zincirine (Java 21, Maven, JUnit 5, JaCoCo) taşımak ve sağlam bir CI altyapısı kurmak amacıyla canlandırıldı. JFoenix bağımlılığı kaldırıldı; standart JavaFX kontrolleri kullanılıyor.

---

## 2. Teknoloji Yığını

| Kategori | Teknoloji / Kütüphane | Kullanım Amacı |
|---|---|---|
| **Çalışma Ortamı** | Java 21 (LTS) | Uzun vadeli destek, modern dil özellikleri |
| **UI Framework** | JavaFX 21 (`controls`, `fxml`, `media`) | Masaüstü arayüz, FXML tabanlı layout |
| **Build** | Maven 3.9+ | Bağımlılık yönetimi, paketleme, plugin ekosistemi |
| **Serialization** | Gson 2.11 | JSON tabanlı veri yedekleme |
| **Yardımcı Kütüphane** | Apache Commons Lang 3 | String ve genel yardımcı metodlar |
| **Test** | JUnit 5 (Jupiter) | Birim testleri |
| **Kapsama** | JaCoCo | Branch coverage raporu ve eşik kontrolü |
| **DevOps** | Docker, Docker Compose | Ortam bağımsız build ve test |
| **CI** | GitHub Actions | Otomatik build + test pipeline |
| **Paketleme** | jpackage (JDK 21) | macOS DMG üretimi |

---

## 3. Mimari Tasarım

`todo-again`, dosya tabanlı kalıcılıkla çalışan tek katmanlı bir JavaFX masaüstü uygulamasıdır.

Detaylı mimari belgesi: [`docs/architecture.md`](docs/architecture.md)

### Başlatma Akışı

1. `Main` JavaFX'i başlatır ve `test.fxml` dosyasını yükler.
2. `Controller` UI olaylarını domain ve kalıcılık işlemlerine bağlar.
3. `YapilacakVeri` singleton, bellek içi listeyi ve disk G/Ç'yi yönetir.
4. Uygulama kapanırken güncel liste `Yapilacaklar.txt` dosyasına yazılır.

### Paket Yapısı

```
src/yapilacaklarListesi/
├── Main.java                      # JavaFX başlangıç noktası
├── Controller.java                # Ana ekran UI kontrolcüsü
├── DialogController.java          # Görev ekleme/düzenleme diyalogu
├── test.fxml                      # Ana ekran layout
├── yapilacakDialogEkrani.fxml     # Diyalog layout
├── veriler/
│   ├── Yapilacak.java             # Domain modeli (başlık, detay, tarih)
│   └── YapilacakVeri.java         # Kalıcılık servisi (okuma/yazma)
├── pomodoro/model/                # Pomodoro zamanlayıcı modeli
├── mediator/                      # Pomodoro string render adaptörü
└── muzik/                         # Ses oynatıcı soyutlamaları
```

### Kalite Kapıları

- **Branch Coverage**: `YapilacakVeri` sınıfı için JaCoCo eşiği >= %70
- **CI**: Her push ve PR'da `mvn -B verify` otomatik çalışır

---

## 4. Veri Formatı

Görevler `Yapilacaklar.txt` dosyasında satır bazlı, sekme-ayrımlı formatta saklanır.

### Kayıt Formatı

```
aciklama<TAB>detay<TAB>dd-MM-yyyy
```

### Örnek

```
Alışveriş yap	Süt, ekmek, yoğurt	24-03-2026
Raporu bitir	Q1 satış raporu taslağı	25-03-2026
```

### Savunmacı Yükleme Davranışı

- Dosya yoksa otomatik olarak oluşturulur.
- Sekme sayısı hatalı olan satırlar atlanır ve uyarı loglanır.
- Geçersiz tarih formatına sahip satırlar da sessizce geçilir; uygulama çökmez.

### JSON Yedekleme

Ek olarak `Yapilacaklar.json` dosyası Gson ile üretilen JSON formatında yedek olarak tutulabilir.

---

## 5. Loglama ve Hata Yönetimi

### Konsol Loglaması

`YapilacakVeri` sınıfında okuma/yazma hatalarında standart Java `System.err` / `System.out` tabanlı loglama kullanılır. Bozuk veri satırları atlanmadan önce uyarı olarak yazdırılır.

### Hata Diyalogları

UI katmanında kullanıcıya anlamlı `Alert` diyalogları gösterilir:

- Boş başlık veya geçersiz tarih ile görev ekleme girişimi
- Seçim yapılmadan silme veya düzenleme aksiyonu
- Dosya kayıt hatası

### NPE Koruması

Canlandırma sürecinde tespit edilen kritik akışlardaki boş referans riskleri kapatıldı:

- `FileChooser` sonucu `null` kontrolü
- Seçili görev yokken silme/düzenleme koruması
- Tarih alanının boş bırakılamaz hale getirilmesi

---

## 6. Test Stratejisi

### 6.1 Birim Testler

JUnit 5 (Jupiter) ile `YapilacakVeri` kalıcılık servisi üzerinde birim testleri yazıldı.

Kapsanan senaryolar:

- Görev kaydetme ve yeniden yükleme
- Bozuk veri satırı (skip davranışı)
- Boş dosya başlatma
- Legacy format (sekme-ayrımlı) uyumluluğu

### 6.2 Kapsama Hedefi

JaCoCo ile `YapilacakVeri` sınıfında **branch coverage >= %70** hedefi CI'da raporlanır.

### 6.3 Test Komutları

```bash
# Tüm testleri ve kapsama kontrolünü çalıştır
mvn clean verify

# Sadece testleri çalıştır
mvn clean test

# Docker üzerinden (Java/Maven kurulumu gerekmez)
docker compose run --rm maven clean verify
```

---

## 7. Kurulum ve Çalıştırma

### 7.1 Gereksinimler

**Yerel kurulum için:**

- JDK 21 (PATH'te erişilebilir olmalı)
- Maven 3.9+

```bash
java -version
mvn -version
```

**Docker ile kurulum için:**

- Docker Desktop veya Docker Engine + Compose eklentisi

### 7.2 Uygulamayı Çalıştır

```bash
# Yerel
mvn clean javafx:run

# Docker (GUI desteklemez, sadece build/test)
docker compose run --rm maven clean verify
```

> **Not:** Docker servisi `platform: linux/amd64` ile yapılandırılmıştır. Bu sayede ARM host makinelerde JavaFX artifact çözümleme tutarlı çalışır. Docker içinde masaüstü UI (`javafx:run`) çalıştırmak için ek display yönlendirme kurulumu gerekir.

### 7.3 Veri Dosyası

Uygulama varsayılan olarak çalışma dizinindeki `Yapilacaklar.txt` dosyasını kullanır.

- Dosya yoksa uygulama başlangıcında otomatik oluşturulur.
- Format: `aciklama<TAB>detay<TAB>dd-MM-yyyy`

### 7.4 DMG Paketi Üretimi

`.dmg` üretimi macOS runner gerektirir. En pratik yöntem GitHub Actions'tır.

**GitHub Actions üzerinden:**

1. `Actions` sekmesine gir.
2. `Build DMG` workflow'unu seç.
3. `Run workflow` ile çalıştır.
4. Job bitince `todo-again-dmg` artifact'ini indir.

**Yerel macOS'te:**

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

mvn -DskipTests clean package dependency:copy-dependencies -DincludeScope=runtime

JAR_PATH=$(ls target/todo-again-*.jar | head -n 1)
JAR_FILE=$(basename "$JAR_PATH")
mkdir -p target/jpackage-input
cp "$JAR_PATH" target/jpackage-input/
cp target/dependency/*.jar target/jpackage-input/

jpackage \
  --type dmg \
  --name "todo-again" \
  --dest target/dist \
  --input target/jpackage-input \
  --main-jar "$JAR_FILE" \
  --main-class yapilacaklarListesi.Main \
  --app-version "1.0.1"
```

Detaylı rehber: [`docs/local-test.md`](docs/local-test.md)

---

## 8. Ekran Görüntüleri

![screenshot-1](ss1.png)
![screenshot-2](ss2.png)
![screenshot-3](ss3.png)

---

## 9. Paketleme ve Dağıtım

### 9.1 CI Pipeline

`.github/workflows/ci.yml` — Her push ve PR'da tetiklenir:

- `mvn -B verify` (derleme + test + JaCoCo kapsama kontrolü)
- Java 21 üzerinde çalışır

### 9.2 DMG Build Pipeline

`.github/workflows/dmg.yml` — macOS runner'da DMG paketi üretir:

- **Manuel tetikleme**: `Actions` → `Build DMG` → `Run workflow` (tag girişi zorunlu)
- **Tag tetikleme**: `v1.0.1` gibi bir tag push'u
- **Çıktı artifact**: `todo-again-dmg`

### 9.3 Docker Notları

Maven bağımlılık cache'i `maven-cache` Docker volume'unda saklanır. Kaynak kod yerel çalışma dizininden container'a mount edilir.

### 9.4 Temel Doğrulama

Build ve test başarısını doğrulamak için:

```bash
# Yerel
mvn clean verify

# Docker
docker compose run --rm maven clean verify
```

---

## 10. Lisans

MIT — Detay için kök dizindeki [`LICENSE`](LICENSE) dosyasına bakınız.

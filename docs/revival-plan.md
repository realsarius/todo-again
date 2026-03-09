# todo-again Revival Plan (2026-03-09)

## 1) Hedef
Bu planin amaci, 2021 donemi JavaFX masaustu projesi olan `todo-again` uygulamasini:
- kisa vadede tekrar **calisir** hale getirmek,
- orta vadede **stabil** ve bakimi kolay bir yapiya tasimak,
- uzun vadede ise modern Java gelistirme pratiklerine yaklastirmaktir.

## 2) Mevcut Durum Ozeti
### Teknik durum (tespit)
- Proje IntelliJ `.iml` ve manuel `lib/` + `javafx-sdk-11.0.2` ile yonetiliyor.
- Maven/Gradle build yok.
- Kod tabani son anlamli degisikliklerini 2021'de almis; 2024'te daha cok README/ekran goruntusu guncellemesi var.
- Ortam bagimliligi yuksek (IDE uzerinden elde ayar gerektiriyor).

### Kritik riskler
- `NullPointerException` olasiligi yuksek akislarda mevcut (kaydet/sil, dialog iptali, tarih secimi).
- Veri saklama formati (`\t` ayracli satirlar) kirilgan.
- Testler assertion odakli degil; guvenli refactor destegi zayif.
- E-posta acma akisinda platforma bagimli shell komutlari var.

## 3) Basari Kriterleri
### MVP (minimum hedef)
- Tek komutla calisir: `./mvnw javafx:run`.
- Uygulama acilir, gorev ekleme/silme/kaydetme calisir.
- Basit kayit yukleme/kaydetme akisinda crash olmaz.
- En az 8-10 adet anlamli unit test gecsin.
- Java 21 + JavaFX 21 ile build ve run basarili olsun.

### Stabilizasyon hedefi
- CI'da otomatik build + test kosar.
- Veri formati bozulmadan migration senaryosu desteklenir.
- Kritik akislar icin hata mesaji ve loglama eklenir.

## 4) Kapsam
### Dahil
- Build sistemi standardizasyonu
- Java/JavaFX surum hizalamasi
- Kritik bug fix
- Test altyapisinin guclendirilmesi
- Basit dokumantasyon ve calistirma talimati

### Dahil degil (ilk faz)
- UI tasariminin komple yenilenmesi
- Yeni buyuk ozellikler (sync, cloud, mobil vb.)
- Veritabanina tam gecis

## 5) Fazli Yol Haritasi
## Faz 0 - Hazirlik ve Emniyet (0.5 gun)
1. `codex/revival-baseline` branch olustur.
2. Mevcut kaynak dosyalari icin yedek etiket (git tag) al.
3. Calisan ornek veri dosyasi ve hata veren senaryolari not et.

Cikti:
- Baslangic etiketi (`pre-revival-2026-03-09` gibi)
- Kisa risk notu

## Faz 1 - Calisir Hale Getirme (1-2 gun)
1. Build sistemini Maven'e tasi (`pom.xml`, `maven-wrapper`).
2. Java hedef surumunu sabitle: **Java 21 (LTS)**.
3. JavaFX bagimliliklarini sabitle: **JavaFX 21** (`controls`, `fxml`, `media`).
4. `module-info.java` kararini net uygula (Faz 1'de):
   - Moduler calisma aktif olacak.
   - Gerekli `requires`, `exports`, `opens ... to javafx.fxml` tanimlari eklenecek.
   - Maven `javafx-maven-plugin` ayarlari moduler calismaya gore sabitlenecek.
5. JFoenix'i kaldir ve standart JavaFX kontrollerine gec (opsiyonel degil):
   - `JFXListView` -> `ListView`
   - `JFXToggleButton` -> `ToggleButton`
   - FXML ve controller importlari temizlenecek.
6. Hemen patlayan buglari kapat:
   - FileChooser `null` kontrolu
   - Secili gorev `null` kontrolu
   - Tarih bos gecilemez kontrolu
   - Kaydet/sil akislari guvenli hale getir
7. E-posta acma akisinda platform bagimsiz API kullan (`Desktop.mail`) ve fallback ekle.

Cikti:
- Yerelde tek komutla acilan uygulama
- Crash'e neden olan kritik NPE'lerin kapanmasi

## Faz 2 - Stabilizasyon ve Veri Guvenligi (1-2 gun)
1. `YapilacakVeri` sinifinda okuma/yazma korumalari:
   - Dosya yoksa olustur
   - Bozuk satir atlama + uyari logu
   - Split/parse guvenligi
2. Veri formati iyilestirme:
   - Kisa vadede mevcut txt formatina geri uyumluluk
   - Orta vadede JSON'a gecis planli migration (opsiyonel)
3. Migration guvencesi icin testler ekle:
   - Projede bulunan mevcut `Yapilacaklar.txt` ornegi parse edilip dogrulanacak.
   - Legacy satir formatlari icin en az 1 negatif (bozuk satir) + 1 pozitif test yazilacak.
4. Kullaniciya anlamli hata diyaloglari ekle.

Cikti:
- Veri bozulmasinda uygulamanin tamamen cokmemesi
- Daha ongorulebilir hata davranisi

## Faz 3 - Test ve Kalite Kapisi (1-2 gun)
1. Testleri JUnit 5'e standartlastir.
2. `YapilacakVeri` icin unit testler yaz:
   - Kaydet/yukle
   - Bozuk veri satiri
   - Bos dosya
3. Controller mantigi icin testlenebilir parcalama (gerekirse service sinifi).
4. Basit CI pipeline ekle (GitHub Actions):
   - Build
   - Test
5. JaCoCo ile kapsama metrigi ekle:
   - `YapilacakVeri` ve cekirdek veri akislarinda minimum **%70 branch coverage** hedefi.
   - Bu esik CI'da raporlanacak (ilk etapta soft gate, sonra hard gate).

Cikti:
- Otomatik testlerle regressions yakalanir
- PR kalite kapisi olusur

## Faz 4 - Dokumantasyon ve Devredilebilirlik (0.5-1 gun)
1. README'yi guncelle:
   - Kurulum
   - Calistirma
   - Test
2. `docs/architecture.md` ile kisa mimari ozet yaz.
3. Katki rehberi (`CONTRIBUTING.md`) sade bir surum ekle.

Cikti:
- Baska bir gelistirici projeyi 10-15 dakikada ayaga kaldirabilir.

## 6) Teknik Gorev Listesi (Onceliklendirilmis)
### P0 (hemen)
- Build standardizasyonu
- Java 21 + JavaFX 21'e gecis
- JFoenix'in tamamen kaldirilmasi
- `module-info.java` ile moduler calisma
- NPE kaynakli kritik akislar
- Java runtime ve JavaFX uyumu

### P1 (kisa vade)
- Veri katmaninda guvenlik iyilestirmesi
- Legacy veri migration testleri
- Test kapsami
- CI

### P2 (orta vade)
- UI kod temizligi
- Paketleme (jlink/jpackage) iyilestirmesi
- JSON migration

## 7) Riskler ve Azaltma
1. JFoenix uyumsuzlugu
- Azaltma: JFoenix'i tamamen kaldirip standart JavaFX kontrollerine gecmek.

2. JavaFX moduler kurulum karmasasi
- Azaltma: `module-info.java` ve Maven plugin ayarlarini Faz 1'de netleyip sabitlemek.

3. Eski veri dosyasinda format bozulmasi
- Azaltma: defensive parse + bozuk satir raporlama.

4. Ortam farklari (macOS/Linux/Windows)
- Azaltma: shell komutlarini azaltip Java API tabanli yaklasim kullanmak.

5. Refactor sirasinda regresyon
- Azaltma: once test, sonra parca parca refactor.

## 8) Tahmini Takvim
- Toplam: 4-7 is gunu
- Hedef 1 (MVP): Gun 1-2 sonunda calisir surum
- Hedef 2 (stabil): Gun 3-5 sonunda test + CI + guvenli veri akis

## 9) Definition of Done
- Yerelde tek komutla calisir.
- Kritik akislarda app crash olmaz.
- Unit testler yesil.
- README guncel ve dogru.
- CI build+test basarili.

## 10) Onerilen Ilk Uygulama Sirasi (uygulamaya donuk)
1. Maven yapisini kur.
2. Java 21 + JavaFX 21 + `module-info.java` ile moduler ayagi sabitle.
3. JFoenix'i kaldir, standart JavaFX kontrollerine gec.
4. Uygulamayi ayaga kaldir.
5. NPE ve platform bagimli hatalari duzelt.
6. Veri katmani guvenligini artir.
7. Migration testleri + coverage hedefi ile testleri yaz ve CI ekle.
8. Dokumantasyonu finalize et.

---

## Ek: Hemen Acilacak Teknik Isler (Issue taslagi)
1. `[P0]` Build sistemi Maven'e gecsin ve tek komut run calissin.
2. `[P0]` Java 21 + JavaFX 21'e gecis.
3. `[P0]` JFoenix kaldirilsin, standart JavaFX kontrolleri kullanilsin.
4. `[P0]` `module-info.java` + moduler Maven calisma ayarlari.
5. `[P0]` `Controller` null-check duzeltmeleri (kaydet/sil/farkli kaydet).
6. `[P0]` `DialogController` tarih validasyonu.
7. `[P1]` `YapilacakVeri` defensive parsing + legacy migration testleri.
8. `[P1]` JUnit 5 tabanli gercek assertion testleri + JaCoCo `%70` branch coverage hedefi.
9. `[P1]` GitHub Actions build + test workflow.

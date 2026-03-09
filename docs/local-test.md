# Lokal Test Rehberi

## 1) Java/Maven kurmadan temel doğrulama (önerilen)
Bu akışta sadece Docker gerekir.

```bash
docker compose run --rm maven clean verify
```

Bu komut ile:
- derleme
- testler
- JaCoCo kapsama kontrolü
çalıştırılır.

## 2) DMG çıktısını test etme
`.dmg` üretimi macOS runner gerektirir, bu yüzden en pratik yöntem GitHub Actions'tır.

Adımlar:
1. GitHub'da `Actions` sekmesine gir.
2. `Build DMG` workflow'unu seç.
3. `Run workflow` ile çalıştır.
4. Job bitince `todo-again-dmg` artifact'ini indir.
5. İndirilen `.dmg` dosyasını açıp uygulamayı test et.

## 3) Lokal macOS'te DMG üretmek istersen
Bu adım için lokalde JDK 21 (jpackage dahil) gerekli.

```bash
mvn -DskipTests clean package dependency:copy-dependencies -DincludeScope=runtime
```

Sonra:

```bash
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
  --app-version "0.0.1"
```

package yapilacaklarListesi.settings;

import yapilacaklarListesi.Main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionResolver {

    private static final String UNKNOWN_VERSION = "Bilinmiyor";
    private static final Pattern POM_VERSION_PATTERN = Pattern.compile(
            "<artifactId>todo-again</artifactId>\\s*<version>([^<]+)</version>",
            Pattern.DOTALL
    );

    private VersionResolver() {
    }

    public static String resolveCurrentVersion() {
        String implementationVersion = Main.class.getPackage().getImplementationVersion();
        if (implementationVersion != null && !implementationVersion.isBlank()) {
            return implementationVersion.trim();
        }

        String mavenVersion = pomPropertiesVersion();
        if (mavenVersion != null) {
            return mavenVersion;
        }

        String pomVersion = pomXmlVersion();
        if (pomVersion != null) {
            return pomVersion;
        }

        return UNKNOWN_VERSION;
    }

    private static String pomPropertiesVersion() {
        try (var stream = VersionResolver.class.getResourceAsStream(
                "/META-INF/maven/io.realsarius/todo-again/pom.properties"
        )) {
            if (stream == null) {
                return null;
            }
            Properties properties = new Properties();
            properties.load(stream);
            String version = properties.getProperty("version");
            if (version == null || version.isBlank()) {
                return null;
            }
            return version.trim();
        } catch (IOException e) {
            return null;
        }
    }

    private static String pomXmlVersion() {
        Path pomPath = Path.of("pom.xml");
        if (Files.notExists(pomPath)) {
            return null;
        }
        try {
            String icerik = Files.readString(pomPath, StandardCharsets.UTF_8);
            Matcher matcher = POM_VERSION_PATTERN.matcher(icerik);
            if (!matcher.find()) {
                return null;
            }
            String version = matcher.group(1);
            if (version == null || version.isBlank()) {
                return null;
            }
            return Objects.requireNonNull(version).trim();
        } catch (IOException e) {
            return null;
        }
    }
}

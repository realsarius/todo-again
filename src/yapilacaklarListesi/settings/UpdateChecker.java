package yapilacaklarListesi.settings;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class UpdateChecker {

    public static final String DEFAULT_GITHUB_OWNER = "berkansozer";
    public static final String DEFAULT_GITHUB_REPO = "todo-again";

    private static final String RELEASES_URL_TEMPLATE =
            "https://api.github.com/repos/%s/%s/releases/latest";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Gson GSON = new Gson();

    private UpdateChecker() {
    }

    public static CompletableFuture<UpdateResult> latestRelease(String owner, String repo) {
        String safeOwner = (owner == null || owner.isBlank()) ? DEFAULT_GITHUB_OWNER : owner.trim();
        String safeRepo = (repo == null || repo.isBlank()) ? DEFAULT_GITHUB_REPO : repo.trim();

        URI uri = URI.create(String.format(Locale.ROOT, RELEASES_URL_TEMPLATE, safeOwner, safeRepo));
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "todo-again-settings")
                .GET()
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> parseResponse(response, safeOwner, safeRepo));
    }

    public static int compareVersions(String currentVersion, String latestTag) {
        int[] current = parseVersionNumbers(currentVersion);
        int[] latest = parseVersionNumbers(latestTag);
        int max = Math.max(current.length, latest.length);
        for (int i = 0; i < max; i++) {
            int c = i < current.length ? current[i] : 0;
            int l = i < latest.length ? latest[i] : 0;
            if (c < l) {
                return -1;
            }
            if (c > l) {
                return 1;
            }
        }
        return 0;
    }

    private static int[] parseVersionNumbers(String version) {
        if (version == null || version.isBlank()) {
            return new int[]{0};
        }
        String normalized = normalizeVersion(version);
        String[] parts = normalized.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = parseInt(parts[i]);
        }
        return result;
    }

    private static String normalizeVersion(String version) {
        String normalized = version.trim();
        if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
        }
        int dashIndex = normalized.indexOf('-');
        if (dashIndex > 0) {
            normalized = normalized.substring(0, dashIndex);
        }
        return normalized;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static UpdateResult parseResponse(HttpResponse<String> response, String owner, String repo) {
        int code = response.statusCode();
        String body = response.body();

        if (code == 200) {
            JsonObject object = GSON.fromJson(body, JsonObject.class);
            if (object == null) {
                return UpdateResult.error("GitHub yanıtı okunamadı.");
            }
            String tag = getString(object, "tag_name");
            String htmlUrl = getString(object, "html_url");
            if (tag == null || tag.isBlank()) {
                return UpdateResult.error("Sürüm etiketi bulunamadı.");
            }
            return UpdateResult.success(new ReleaseInfo(tag, htmlUrl));
        }

        if (code == 404) {
            return UpdateResult.error("Release kaynağı bulunamadı: " + owner + "/" + repo);
        }
        if (code == 403) {
            return UpdateResult.error("GitHub API erişim limiti aşıldı. Daha sonra tekrar deneyin.");
        }
        return UpdateResult.error("Güncelleme kontrolü başarısız oldu (HTTP " + code + ").");
    }

    private static String getString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return object.get(key).getAsString();
    }

    public record ReleaseInfo(String tagName, String htmlUrl) {
    }

    public record UpdateResult(boolean success, ReleaseInfo releaseInfo, String errorMessage) {

        public static UpdateResult success(ReleaseInfo releaseInfo) {
            return new UpdateResult(true, releaseInfo, null);
        }

        public static UpdateResult error(String errorMessage) {
            return new UpdateResult(false, null, errorMessage);
        }
    }
}

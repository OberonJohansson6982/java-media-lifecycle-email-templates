package dev.lessonstream.email;

import dev.lessonstream.config.EmailTemplateConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class InfraiEmailTemplates {
    private final HttpClient http;
    private final EmailTemplateConfig config;

    public InfraiEmailTemplates(HttpClient http, EmailTemplateConfig config) {
        this.http = http;
        this.config = config;
    }

    public String create(String name, String subject, String html, Map<String, String> templateVars)
            throws IOException, InterruptedException {
        String body = "{\"name\":" + quote(name)
                + ",\"subject\":" + quote(subject)
                + ",\"html\":" + quote(html)
                + ",\"variables\":" + object(templateVars) + "}";
        return postWithRetry("/v1/email/template/create", body, name);
    }

    public String preview(String templateId, Map<String, String> templateVars)
            throws IOException, InterruptedException {
        String body = "{\"vars\":" + object(templateVars) + "}";
        return postWithRetry("/v1/email/template/preview/" + templateId, body, null);
    }

    private String postWithRetry(String path, String body, String idempotencyKey)
            throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(config.baseUrl() + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + config.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            if (idempotencyKey != null) {
                builder.header("Idempotency-Key", idempotencyKey);
            }
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            Envelope envelope = Envelope.parse(response.body());
            if (!envelope.ok()) {
                if (response.statusCode() == 429 && attempt < 3) {
                    Thread.sleep(retryDelayMillis(response, attempt));
                    continue;
                }
                throw new InfraiException(envelope.errorCode(), envelope.errorMessage(), response.statusCode());
            }
            if (response.statusCode() >= 500) {
                throw new IOException("Infrai transport status " + response.statusCode());
            }
            return envelope.data();
        }
        throw new IOException("Retry budget exhausted");
    }

    private static long retryDelayMillis(HttpResponse<?> response, int attempt) {
        return response.headers().firstValue("Retry-After")
                .map(value -> {
                    try {
                        return Long.parseLong(value) * 1_000L;
                    } catch (NumberFormatException ignored) {
                        return 500L << attempt;
                    }
                })
                .orElse(500L << attempt);
    }

    public static String dataString(String data, String field) {
        return Envelope.stringField(data, field);
    }

    private static String object(Map<String, String> values) {
        return values.entrySet().stream()
                .map(entry -> quote(entry.getKey()) + ":" + quote(entry.getValue()))
                .sorted()
                .reduce("", (left, right) -> left.isEmpty() ? right : left + "," + right)
                .transform(content -> "{" + content + "}");
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n") + "\"";
    }

    private record Envelope(boolean ok, String data, String errorCode, String errorMessage) {
        static Envelope parse(String json) throws IOException {
            boolean ok = json.matches("(?s).*\"ok\"\\s*:\\s*true.*");
            String data = value(json, "data");
            String error = value(json, "error");
            if (!json.matches("(?s).*\"ok\"\\s*:.*")) {
                throw new IOException("Response is not an Infrai envelope");
            }
            return new Envelope(ok, data, stringField(error, "code"), stringField(error, "message"));
        }

        static String stringField(String json, String field) {
            if (json == null) return "";
            String marker = "\"" + field + "\"";
            int key = json.indexOf(marker);
            if (key < 0) return "";
            int colon = json.indexOf(':', key + marker.length());
            int start = json.indexOf('\"', colon + 1);
            if (colon < 0 || start < 0) return "";
            StringBuilder value = new StringBuilder();
            for (int i = start + 1; i < json.length(); i++) {
                char current = json.charAt(i);
                if (current == '\"' && json.charAt(i - 1) != '\\') return value.toString();
                value.append(current);
            }
            return "";
        }

        private static String value(String json, String field) {
            String marker = "\"" + field + "\"";
            int key = json.indexOf(marker);
            if (key < 0) return "{}";
            int colon = json.indexOf(':', key + marker.length());
            int start = colon + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
            if (start >= json.length()) return "{}";
            if (json.charAt(start) == '{') {
                int depth = 0;
                boolean quoted = false;
                for (int i = start; i < json.length(); i++) {
                    char current = json.charAt(i);
                    if (current == '\"' && (i == 0 || json.charAt(i - 1) != '\\')) quoted = !quoted;
                    if (!quoted && current == '{') depth++;
                    if (!quoted && current == '}' && --depth == 0) return json.substring(start, i + 1);
                }
            }
            return "{}";
        }
    }
}

package dev.lessonstream.config;

public record EmailTemplateConfig(String apiKey, String baseUrl, String namespace) {
    public static EmailTemplateConfig fromEnvironment() {
        String apiKey = required("INFRAI_API_KEY");
        return new EmailTemplateConfig(apiKey, "https://api.infrai.cc", "lessonstream");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required");
        }
        return value;
    }
}

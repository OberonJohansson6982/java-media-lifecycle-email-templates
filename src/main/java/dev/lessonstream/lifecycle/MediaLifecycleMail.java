package dev.lessonstream.lifecycle;

import java.util.Map;

public record MediaLifecycleMail(String templateName, String subject, String html, Map<String, String> templateVars) {
}

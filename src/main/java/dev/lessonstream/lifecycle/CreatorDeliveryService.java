package dev.lessonstream.lifecycle;

import dev.lessonstream.config.EmailTemplateConfig;
import dev.lessonstream.email.InfraiEmailTemplates;

import java.io.IOException;
import java.time.Instant;

public final class CreatorDeliveryService {
    private final MediaLifecyclePolicy policy;
    private final InfraiEmailTemplates templates;
    private final EmailTemplateConfig config;

    public CreatorDeliveryService(MediaLifecyclePolicy policy, InfraiEmailTemplates templates,
                                  EmailTemplateConfig config) {
        this.policy = policy;
        this.templates = templates;
        this.config = config;
    }

    public PreviewResult prepare(MediaLifecyclePolicy.AssetState state, String creatorName,
                                 String lessonTitle) throws IOException, InterruptedException {
        MediaLifecycleMail mail = policy.choose(state, creatorName, lessonTitle);
        String uniqueName = config.namespace() + "-" + mail.templateName() + "-" + Instant.now().toEpochMilli();
        String created = templates.create(uniqueName, mail.subject(), mail.html(), mail.templateVars());
        String templateId = InfraiEmailTemplates.dataString(created, "template_id");
        if (templateId.isBlank()) throw new IOException("Template response did not include id");
        String preview = templates.preview(templateId, mail.templateVars());
        return new PreviewResult(state, uniqueName, templateId, preview);
    }

    public record PreviewResult(MediaLifecyclePolicy.AssetState state, String templateName,
                                String templateId, String previewData) {
    }
}

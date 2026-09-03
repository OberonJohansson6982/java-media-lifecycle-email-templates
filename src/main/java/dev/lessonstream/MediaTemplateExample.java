package dev.lessonstream;

import dev.lessonstream.config.EmailTemplateConfig;
import dev.lessonstream.email.InfraiEmailTemplates;
import dev.lessonstream.lifecycle.CreatorDeliveryService;
import dev.lessonstream.lifecycle.MediaLifecyclePolicy;

import java.net.http.HttpClient;

public final class MediaTemplateExample {
    public static void main(String[] args) throws Exception {
        EmailTemplateConfig config = EmailTemplateConfig.fromEnvironment();
        InfraiEmailTemplates templates = new InfraiEmailTemplates(HttpClient.newHttpClient(), config);
        CreatorDeliveryService service = new CreatorDeliveryService(new MediaLifecyclePolicy(), templates, config);

        CreatorDeliveryService.PreviewResult result = service.prepare(
                MediaLifecyclePolicy.AssetState.READY, "Maya", "Color Theory for Beginners");
        System.out.println("Created and previewed " + result.templateName()
                + " for state " + result.state() + " (template " + result.templateId() + ")");
    }
}

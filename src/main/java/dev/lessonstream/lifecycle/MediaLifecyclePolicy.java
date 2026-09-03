package dev.lessonstream.lifecycle;

import java.util.Map;

public final class MediaLifecyclePolicy {
    public enum AssetState { INGESTED, PROCESSING, READY }

    public MediaLifecycleMail choose(AssetState state, String creatorName, String lessonTitle) {
        return switch (state) {
            case INGESTED -> mail("asset-ingested", "Your lesson upload is in",
                    "<h1>Upload received</h1><p>{{creator_name}}, {{lesson_title}} is ready for processing.</p>",
                    creatorName, lessonTitle);
            case PROCESSING -> mail("asset-processing", "Your lesson is being prepared",
                    "<h1>Processing started</h1><p>{{creator_name}}, we are preparing {{lesson_title}} for learners.</p>",
                    creatorName, lessonTitle);
            case READY -> mail("creator-delivery", "Your lesson is ready to teach",
                    "<h1>Lesson ready</h1><p>{{creator_name}}, {{lesson_title}} is ready for your course.</p>",
                    creatorName, lessonTitle);
        };
    }

    private static MediaLifecycleMail mail(String name, String subject, String html,
                                            String creatorName, String lessonTitle) {
        return new MediaLifecycleMail(name, subject, html,
                Map.of("creator_name", creatorName, "lesson_title", lessonTitle));
    }
}

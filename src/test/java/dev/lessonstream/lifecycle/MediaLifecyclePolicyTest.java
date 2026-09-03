package dev.lessonstream.lifecycle;

public final class MediaLifecyclePolicyTest {
    public static void main(String[] args) {
        MediaLifecycleMail mail = new MediaLifecyclePolicy().choose(
                MediaLifecyclePolicy.AssetState.READY, "Maya", "Color Theory for Beginners");

        require(mail.templateName().equals("creator-delivery"), "READY must select creator delivery");
        require(mail.subject().equals("Your lesson is ready to teach"), "READY subject must be concrete");
        require(mail.templateVars().get("creator_name").equals("Maya"), "creator variable must be preserved");
        require(mail.templateVars().get("lesson_title").equals("Color Theory for Beginners"),
                "lesson variable must be preserved");
        System.out.println("PASS: READY selects creator-delivery with both lesson variables");
    }

    private static void require(boolean decision, String message) {
        if (!decision) throw new AssertionError(message);
    }
}

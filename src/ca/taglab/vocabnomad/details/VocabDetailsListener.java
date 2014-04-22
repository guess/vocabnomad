package ca.taglab.vocabnomad.details;

public interface VocabDetailsListener {
    public static final int HEADER = 0;
    public static final int SENTENCE = 1;
    public static final int DEFINITION = 2;
    public static final int TAGS = 3;
    public static final int SHARED = 4;

    public void onEditPressed(int id, boolean isEditing);
    public void onTagPressed(String name);
    public void onProgressComplete();
    public void onProgressIncrement(int skill);
    public void onStartProgressIncrement(int skill);
    public void onStopProgressIncrement();
    public void onClosePrompt();
}

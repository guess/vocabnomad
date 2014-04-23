package ca.taglab.vocabnomad.olm;

public interface TagDetailsListener {
    public void onGoalLocked();
    public void noDefinitionExists();
    public void noSuggestedGoals();
}

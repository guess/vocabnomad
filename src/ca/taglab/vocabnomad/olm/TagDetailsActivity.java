package ca.taglab.vocabnomad.olm;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import ca.taglab.vocabnomad.R;


public class TagDetailsActivity extends FragmentActivity implements TagDetailsListener {
    public static final String TAG = "TagDetailsActivity";
    public static final String TAG_NAME = "tag_name";

    private String mTagName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.olm_tag_details);
        mTagName = getIntent().getStringExtra(TAG_NAME);
        Log.i(TAG, "Details: " + mTagName);

        if (findViewById(R.id.tag_details) != null) {
            if (savedInstanceState != null) {
                Log.i(TAG, "Returning");
                return;
            }

            Log.i(TAG, "Drawing the fragment");

            CategoryTitleFragment title = CategoryTitleFragment.newInstance(mTagName);
            GoalDefinitionFragment definition = GoalDefinitionFragment.newInstance(mTagName);
            RelatedTagsFragment related = RelatedTagsFragment.newInstance(mTagName);
            ManageGoalFragment manager = ManageGoalFragment.newInstance(mTagName, false);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tag_title, title)
                    .replace(R.id.definition, definition)
                    .replace(R.id.manager, manager)
                    .replace(R.id.related_goals, related)
                    .commit();

        }
    }

    @Override
    public void showSuggestions() {
        // The goal is locked, suggest smaller goals for the user to tackle
        SuggestedGoalsFragment suggested = SuggestedGoalsFragment.newInstance(mTagName);
        getSupportFragmentManager().beginTransaction().replace(R.id.suggested, suggested).commit();
    }

    @Override
    public void noDefinitionExists() {
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentById(R.id.definition))
                .commit();
        findViewById(R.id.definition).setVisibility(View.GONE);
    }

    @Override
    public void noSuggestedGoals() {
        ManageGoalFragment manager = ManageGoalFragment.newInstance(mTagName, true);
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentById(R.id.suggested))
                .replace(R.id.manager, manager).commit();
    }
}

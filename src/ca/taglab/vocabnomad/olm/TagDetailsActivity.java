package ca.taglab.vocabnomad.olm;



import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import javax.crypto.Mac;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.types.Goal;
import ca.taglab.vocabnomad.widgets.TypefacedTextView;


public class TagDetailsActivity extends FragmentActivity {
    public static final String TAG = "TagDetailsActivity";
    public static final String TAG_NAME = "tag_name";
    public static final int GOAL_LIMIT = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.olm_tag_details);
        String tag = getIntent().getStringExtra(TAG_NAME);
        Log.i(TAG, "Details: " + tag);

        new CreateGoalButton(tag).execute();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                Log.i(TAG, "Returning");
                return;
            }

            Log.i(TAG, "Drawing the fragment");

            CategoryTitleFragment title = CategoryTitleFragment.newInstance(tag);
            FavouriteWordsFragment words = FavouriteWordsFragment.newInstance(tag);
            RelatedTagsFragment tags = RelatedTagsFragment.newInstance(tag);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tag_title, title)
                    .add(R.id.fragment_container, words)
                    .add(R.id.fragment_container, tags)
                    .commit();

        }
    }


    /**
     * Create the goal button such that it:
     *      - says "Add Goal" if the tag is not a goal
     *      - says "Remove Goal" if the tag is a goal
     *      - is hidden if the user has reached their goal limit
     */
    class CreateGoalButton extends AsyncTask<Void, Void, Integer> {
        private static final int REACHED_LIMIT = 1;
        private static final int IS_GOAL = 2;
        private static final int NOT_GOAL = 3;

        private String mTagName;

        CreateGoalButton(String name) {
            this.mTagName = name;
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            // Check to see if this tag is a goal first
            if (Goal.isActiveGoal(TagDetailsActivity.this, mTagName)) {
                return IS_GOAL;
            }

            // It is not a goal. Has the user exceeded the goal limit?
            if (Goal.isAtGoalLimit(TagDetailsActivity.this, GOAL_LIMIT)) {
                return REACHED_LIMIT;
            }

            // This is not a goal and the user still has not reached the limit
            return NOT_GOAL;
        }

        @Override
        protected void onPostExecute(Integer retVal) {
            View layout = findViewById(R.id.add_goal);
            layout.findViewById(R.id.header).setBackgroundResource(R.drawable.white_clickable);

            switch (retVal) {

                case IS_GOAL:
                    ((TypefacedTextView) layout.findViewById(R.id.title)).setText("Remove goal");
                    ((ImageView) layout.findViewById(R.id.image)).setImageResource(R.drawable.remove_normal_black);
                    layout.setOnClickListener(new RemoveGoal(mTagName));
                    break;

                case NOT_GOAL:
                    ((TypefacedTextView) layout.findViewById(R.id.title)).setText("Add goal");
                    ((ImageView) layout.findViewById(R.id.image)).setImageResource(R.drawable.add_normal_black);
                    layout.setOnClickListener(new AddGoal(mTagName));
                    break;

                default:
                    return;
            }

            layout.setVisibility(View.VISIBLE);
        }

        /**
         * OnClickListener that will add the tag to the user's goals.
         */
        class AddGoal implements View.OnClickListener {
            private String name;

            AddGoal(String name) {
                this.name = name;
            }

            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // Get the tag from the database
                        Cursor cursor = getContentResolver().query(
                                Contract.Tag.getUri(),
                                Contract.Tag.PROJECTION,
                                Contract.Tag.NAME + "=?",
                                new String[] { name },
                                null, null
                        );

                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                // Find the tag's device ID and add the goal to the database
                                long id = cursor.getLong(cursor.getColumnIndex(Contract.Tag._ID));
                                Goal.addGoal(TagDetailsActivity.this, id, name);
                            }
                            cursor.close();
                        }
                        new CreateGoalButton(name).execute();
                    }
                }).start();
            }
        }

        /**
         * OnClickListener that will remove the tag from the user's goals.
         */
        class RemoveGoal implements View.OnClickListener {
            private String name;

            RemoveGoal(String name) {
                this.name = name;
            }

            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Goal.deleteGoal(TagDetailsActivity.this, name);
                        new CreateGoalButton(name).execute();
                    }
                }).start();
            }
        }
    }


}

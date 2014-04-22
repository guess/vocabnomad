package ca.taglab.vocabnomad.olm;



import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.types.Goal;
import ca.taglab.vocabnomad.types.Word;
import ca.taglab.vocabnomad.widgets.ImageOpt;
import ca.taglab.vocabnomad.widgets.TypefacedTextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link ActiveGoalFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ActiveGoalFragment extends Fragment {
    private static final String GOAL_NAME = "goal";
    private String mGoalName;

    /**
     * Create a new instance of the ActiveGoalFragment, for the tag 'name'.
     * @param name      The name of a tag
     * @return an instance of the ActiveGoalFragment for the goal with tag 'name'
     */
    public static ActiveGoalFragment newInstance(String name) {
        ActiveGoalFragment fragment = new ActiveGoalFragment();
        Bundle args = new Bundle();
        args.putString(GOAL_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGoalName = getArguments().getString(GOAL_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.olm_goal_item, container, false);
        if (layout == null) return null;
        if (mGoalName == null) return layout;

        CategoryTitleFragment title = CategoryTitleFragment.newInstance(mGoalName);
        getChildFragmentManager().beginTransaction().add(R.id.goal, title).commit();
        layout.findViewById(R.id.goal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TagDetailsActivity.class);
                intent.putExtra(TagDetailsActivity.TAG_NAME, mGoalName);
                startActivity(intent);
            }
        });

        new SetProgress((ProgressBar) layout.findViewById(R.id.progress)).execute(mGoalName);
        new SetRelatedTags((ViewGroup) layout.findViewById(R.id.tags)).execute(mGoalName);

        return layout;
    }

    class SetRelatedTags extends AsyncTask<String, Void, Cursor> {
        private static final int TAG_LIMIT = 4;
        private ViewGroup layout;

        SetRelatedTags(ViewGroup layout) {
            this.layout = layout;
        }

        @Override
        protected Cursor doInBackground(String... tags) {
            String tag = tags[0];
            return UserStats.getRelatedTags(getActivity(), tag);
        }

        @Override
        protected void onPostExecute(Cursor tags) {
            layout.removeAllViewsInLayout();

            if (tags != null) {
                tags.moveToFirst();
                for (int i = 0; i < TAG_LIMIT && !tags.isAfterLast(); i++) {
                    if (getActivity() == null) return;
                    final TextView tag = (TextView) LayoutInflater.from(getActivity()).inflate(
                            R.layout.tag, layout, false);

                    if (tag != null) {
                        tag.setText(tags.getString(tags.getColumnIndex(Contract.View.NAME)));
                        tag.setTextSize(18);
                        tag.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String tag = ((TextView) v).getText().toString();
                                Intent intent = new Intent(getActivity(), TagDetailsActivity.class);
                                intent.putExtra(TagDetailsActivity.TAG_NAME, tag);
                                startActivity(intent);
                            }
                        });
                        layout.addView(tag, 0);
                    }
                    tags.moveToNext();
                }
                tags.close();
            }
        }
    }

    public class SetProgress extends AsyncTask<String, Void, Void> {
        private ProgressBar bar;

        private int total;
        private int progress;

        SetProgress(ProgressBar bar) {
            this.bar = bar;
        }

        @Override
        protected Void doInBackground(String... tags) {
            String tag = tags[0];

            Goal.updateProgress(getActivity());

            Cursor cursor = Goal.getGoal(getActivity(), tag);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    this.total = cursor.getInt(cursor.getColumnIndex(Contract.Goals.TOTAL));
                    this.progress = cursor.getInt(cursor.getColumnIndex(Contract.Goals.PROGRESS));
                }
                cursor.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            bar.setMax(total);
            bar.setProgress(progress);
        }
    }

}

package ca.taglab.vocabnomad.olm;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.widgets.TypefacedTextView;


public class OverviewFragment extends Fragment {
    ViewGroup mReadDetails, mWriteDetails, mListenDetails, mSpeakDetails;
    HashMap<Integer, Boolean> expanded;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.olm_overview, container, false);

        expanded = new HashMap<Integer, Boolean>();

        if (layout != null) {
            mReadDetails = (ViewGroup) layout.findViewById(R.id.read_details);
            mReadDetails.setOnClickListener(new ExpandCollapse());
            mWriteDetails = (ViewGroup) layout.findViewById(R.id.write_details);
            mWriteDetails.setOnClickListener(new ExpandCollapse());
            mListenDetails = (ViewGroup) layout.findViewById(R.id.listen_details);
            mListenDetails.setOnClickListener(new ExpandCollapse());
            mSpeakDetails = (ViewGroup) layout.findViewById(R.id.speak_details);
            mSpeakDetails.setOnClickListener(new ExpandCollapse());
            drawDetails();
        }

        expand(UserStats.READING);
        expand(UserStats.WRITING);
        expand(UserStats.LISTENING);
        expand(UserStats.SPEAKING);

        return layout;
    }

    /**
     * Dynamically draw the detailed view of the OLM overview
     */
    private void drawDetails() {
        ((ImageView) mReadDetails.findViewById(R.id.skill_img)).setImageResource(R.drawable.read);
        ((TypefacedTextView) mReadDetails.findViewById(R.id.skill_name)).setText(R.string.reading);

        ((ImageView) mWriteDetails.findViewById(R.id.skill_img)).setImageResource(R.drawable.write);
        ((TypefacedTextView) mWriteDetails.findViewById(R.id.skill_name)).setText(R.string.writing);

        ((ImageView) mListenDetails.findViewById(R.id.skill_img)).setImageResource(R.drawable.listen);
        ((TypefacedTextView) mListenDetails.findViewById(R.id.skill_name)).setText(R.string.listening);

        ((ImageView) mSpeakDetails.findViewById(R.id.skill_img)).setImageResource(R.drawable.speak);
        ((TypefacedTextView) mSpeakDetails.findViewById(R.id.skill_name)).setText(R.string.speaking);
    }


    class ExpandCollapse implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int skill;
            switch (view.getId()) {
                case R.id.read_details:
                    skill = UserStats.READING;
                    break;
                case R.id.write_details:
                    skill = UserStats.WRITING;
                    break;
                case R.id.listen_details:
                    skill = UserStats.LISTENING;
                    break;
                case R.id.speak_details:
                    skill = UserStats.SPEAKING;
                    break;
                default:
                    return;
            }

            if (expanded.get(skill)) {
                collapse(skill);
            } else {
                expand(skill);
            }
        }
    }


    private void expand(int skill) {
        switch (skill) {
            case UserStats.READING:
                new FillDetails(UserStats.READING).execute();
                ((ImageView) mReadDetails.findViewById(R.id.expand_collapse))
                        .setImageResource(R.drawable.ic_action_collapse);
                break;
            case UserStats.WRITING:
                new FillDetails(UserStats.WRITING).execute();
                ((ImageView) mWriteDetails.findViewById(R.id.expand_collapse))
                        .setImageResource(R.drawable.ic_action_collapse);
                break;
            case UserStats.LISTENING:
                new FillDetails(UserStats.LISTENING).execute();
                ((ImageView) mListenDetails.findViewById(R.id.expand_collapse))
                        .setImageResource(R.drawable.ic_action_collapse);
                break;
            case UserStats.SPEAKING:
                new FillDetails(UserStats.SPEAKING).execute();
                ((ImageView) mSpeakDetails.findViewById(R.id.expand_collapse))
                        .setImageResource(R.drawable.ic_action_collapse);
                break;
        }

        expanded.put(skill, true);
    }


    private void collapse(int skill) {
        switch (skill) {
            case UserStats.READING:
                ((ViewGroup) mReadDetails.findViewById(R.id.container)).removeAllViews();
                ((ImageView) mReadDetails.findViewById(R.id.expand_collapse))
                        .setImageResource(R.drawable.ic_action_expand);
                break;
            case UserStats.WRITING:
                ((ViewGroup) mWriteDetails.findViewById(R.id.container)).removeAllViews();
                ((ImageView) mWriteDetails.findViewById(R.id.expand_collapse))
                        .setImageResource(R.drawable.ic_action_expand);
                break;
            case UserStats.LISTENING:
                ((ViewGroup) mListenDetails.findViewById(R.id.container)).removeAllViews();
                ((ImageView) mListenDetails.findViewById(R.id.expand_collapse))
                        .setImageResource(R.drawable.ic_action_expand);
                break;
            case UserStats.SPEAKING:
                ((ViewGroup) mSpeakDetails.findViewById(R.id.container)).removeAllViews();
                ((ImageView) mSpeakDetails.findViewById(R.id.expand_collapse))
                        .setImageResource(R.drawable.ic_action_expand);
                break;
        }

        expanded.put(skill, false);
    }


    class FillDetails extends AsyncTask<Void, Void, ArrayList<String>> {
        int skill;

        FillDetails(int skill) {
            this.skill = skill;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            Cursor cursor;
            ArrayList<String> tags = new ArrayList<String>();

            switch (skill) {
                case UserStats.READING:
                    cursor = UserStats.getTags(getActivity(), UserStats.READING_ACTIONS);
                    break;
                case UserStats.WRITING:
                    cursor = UserStats.getTags(getActivity(), UserStats.WRITING_ACTIONS);
                    break;
                case UserStats.LISTENING:
                    cursor = UserStats.getTags(getActivity(), UserStats.LISTENING_ACTIONS);
                    break;
                case UserStats.SPEAKING:
                    cursor = UserStats.getTags(getActivity(), UserStats.SPEAKING_ACTIONS);
                    break;
                default:
                    return null;
            }

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    for (int i = 0; i < 4 && !cursor.isAfterLast(); i++) {
                        String tag = cursor.getString(cursor.getColumnIndex(Contract.View.NAME));
                        if (!TextUtils.isEmpty(tag)) tags.add(tag);
                        cursor.moveToNext();
                    }
                }
                cursor.close();
            }

            return tags;
        }

        @Override
        protected void onPostExecute(ArrayList<String> tags) {
            ViewGroup group = null;
            if (tags == null) return;

            switch (skill) {
                case UserStats.READING:
                    group = (ViewGroup) mReadDetails.findViewById(R.id.container);
                    break;
                case UserStats.WRITING:
                    group = (ViewGroup) mWriteDetails.findViewById(R.id.container);
                    break;
                case UserStats.LISTENING:
                    group = (ViewGroup) mListenDetails.findViewById(R.id.container);
                    break;
                case UserStats.SPEAKING:
                    group = (ViewGroup) mSpeakDetails.findViewById(R.id.container);
                    break;
            }

            if (group != null) {
                for (int i = 0; i < tags.size(); i++) {
                    final TextView newView = (TextView) LayoutInflater.from(getActivity())
                            .inflate(android.R.layout.simple_list_item_1, group, false);
                    if (newView != null) {
                        ((TextView) newView.findViewById(android.R.id.text1)).setText(tags.get(i));
                        group.addView(newView, i);
                    }
                }
            }
        }
    }

}

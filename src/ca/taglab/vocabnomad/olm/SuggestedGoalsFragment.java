package ca.taglab.vocabnomad.olm;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.types.Goal;
import ca.taglab.vocabnomad.widgets.TypefacedTextView;

public class SuggestedGoalsFragment extends Fragment {
    public static final String TAG_NAME = "tag_name";
    public static final String TITLE = "title";
    private String mGoal;
    private String mTitle;

    private ViewGroup mList;

    private TagDetailsListener mListener;


    public static SuggestedGoalsFragment newInstance(String tag) {
        SuggestedGoalsFragment fragment = new SuggestedGoalsFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, "Suggested Goals");
        args.putString(TAG_NAME, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGoal = getArguments().getString(TAG_NAME);
            mTitle = getArguments().getString(TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.widget_card_list, container, false);

        if (layout != null) {
            ((TextView) layout.findViewById(R.id.title)).setText(mTitle);
            layout.findViewById(R.id.image).setVisibility(View.GONE);
            mList = (ViewGroup) layout.findViewById(R.id.list);
            new FillList().execute();
        }

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TagDetailsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    class OpenGoal implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), TagDetailsActivity.class);
            intent.putExtra(TagDetailsActivity.TAG_NAME, (String) view.getTag());
            startActivity(intent);
        }
    }

    /**
     * Fill the list values on a separate thread.
     */
    class FillList extends AsyncTask<Void, Void, ArrayList<ViewGroup>> {
        @Override
        protected ArrayList<ViewGroup> doInBackground(Void... voids) {
            ArrayList<ViewGroup> views = new ArrayList<ViewGroup>();
            ArrayList<String> goals = Goal.getManageableGoals(getActivity(), mGoal, 5);

            for (String goal : goals) {
                final ViewGroup newView = (ViewGroup) LayoutInflater.from(getActivity())
                        .inflate(R.layout.widget_card_list_item, mList, false);
                if (newView != null) {
                    newView.setBackgroundResource(R.drawable.white_clickable);
                    if (!TextUtils.isEmpty(goal)) {
                        ((TypefacedTextView) newView.findViewById(R.id.text)).setText(goal);
                        newView.setTag(goal);
                        newView.setOnClickListener(new OpenGoal());
                    }
                    views.add(newView);
                }
            }

            return views;
        }

        @Override
        protected void onPostExecute(ArrayList<ViewGroup> views) {

            if (views.isEmpty() && mListener != null) {
                mListener.noSuggestedGoals();
            }

            for (ViewGroup view : views) {
                mList.addView(view);
            }
        }
    }
}

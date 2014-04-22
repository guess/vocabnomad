package ca.taglab.vocabnomad.olm;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.details.VocabDetailsListener;


public class GoalCompletePrompt extends Fragment {
    private static final String GOAL = "goal";
    private static final String POINTS = "points";

    private String mGoal;
    private int mPoints;

    private VocabDetailsListener mListener;

    public static GoalCompletePrompt newInstance(String goal) {
        GoalCompletePrompt fragment = new GoalCompletePrompt();
        Bundle args = new Bundle();
        args.putString(GOAL, goal);
        args.putInt(POINTS, 500);
        fragment.setArguments(args);
        return fragment;
    }

    public GoalCompletePrompt() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGoal = getArguments().getString(GOAL);
            mPoints = getArguments().getInt(POINTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.olm_goal_complete_prompt, parent, false);
        if (layout != null) {
            ((TextView) layout.findViewById(R.id.points)).setText("+" + mPoints + " points");
            ((TextView) layout.findViewById(R.id.goal)).setText("Trophy earned: " + mGoal);
            layout.findViewById(R.id.ok_button).setOnClickListener(new ClosePrompt());
            layout.findViewById(R.id.view_button).setOnClickListener(new ClosePrompt());
        }
        return layout;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (VocabDetailsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    class ClosePrompt implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onClosePrompt();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}

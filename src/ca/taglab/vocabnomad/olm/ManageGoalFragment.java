package ca.taglab.vocabnomad.olm;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.types.Goal;

public class ManageGoalFragment extends Fragment {
    private static final String GOAL = "goal";
    private static final String UNLOCK = "unlock";
    private String mGoal;
    private boolean mUnlock;

    private static final int GOAL_LIMIT = 4;

    // Possibilities
    private static final int REACHED_LIMIT = 1;
    private static final int COMPLETED = 2;
    private static final int LOCKED = 3;
    private static final int IS_GOAL = 4;
    private static final int NOT_GOAL = 5;

    private View mLayout;

    private TagDetailsListener mListener;

    public static ManageGoalFragment newInstance(String goal, boolean unlock) {
        ManageGoalFragment fragment = new ManageGoalFragment();
        Bundle args = new Bundle();
        args.putString(GOAL, goal);
        args.putBoolean(UNLOCK, unlock);
        fragment.setArguments(args);
        return fragment;
    }

    public ManageGoalFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGoal = getArguments().getString(GOAL);
            mUnlock = getArguments().getBoolean(UNLOCK, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.olm_manage_goal, parent, false);
        if (mLayout != null) {
            new CreateGoalButton().execute();
        }
        return mLayout;
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

    class CreateGoalButton extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            // Check to see if the tag is a completed goal
            if (Goal.isCompletedGoal(getActivity(), mGoal)) {
                return COMPLETED;
            }

            // Check to see if this tag is a goal first
            if (Goal.isActiveGoal(getActivity(), mGoal)) {
                return IS_GOAL;
            }

            // It is not a goal. Has the user exceeded the goal limit?
            if (Goal.isAtGoalLimit(getActivity(), GOAL_LIMIT)) {
                return REACHED_LIMIT;
            }

            // Check to see if the this tag is a manageable goal
            if (!mUnlock && !Goal.isManageableGoal(getActivity(), mGoal)) {
                return LOCKED;
            }

            // This is not a goal and the user still has not reached the limit
            return NOT_GOAL;
        }

        @Override
        protected void onPostExecute(Integer retValue) {
            ImageView image = (ImageView) mLayout.findViewById(R.id.image);
            TextView text = (TextView) mLayout.findViewById(R.id.text);

            switch (retValue) {
                case REACHED_LIMIT:
                    mLayout.setVisibility(View.GONE);
                    break;
                case COMPLETED:
                    text.setText("Completed");
                    image.setImageResource(R.drawable.trophy);
                    mLayout.setOnClickListener(null);
                    break;
                case LOCKED:
                    text.setText("Locked");
                    image.setImageResource(R.drawable.lock);
                    mLayout.setOnClickListener(null);
                    if (mListener != null) {
                        mListener.onGoalLocked();
                    }
                    break;
                case NOT_GOAL:
                    text.setText("Add Goal");
                    image.setImageResource(R.drawable.add_normal_black);
                    mLayout.setOnClickListener(new AddGoal(mGoal));
                    break;
                case IS_GOAL:
                    text.setText("Remove Goal");
                    image.setImageResource(R.drawable.remove_normal_black);
                    mLayout.setOnClickListener(new RemoveGoal(mGoal));
                    break;
            }
        }
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
                    Cursor cursor = getActivity().getContentResolver().query(
                            Contract.Tag.getUri(),
                            Contract.Tag.PROJECTION,
                            Contract.Tag.NAME + "=?",
                            new String[]{name},
                            null, null
                    );

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            // Find the tag's device ID and add the goal to the database
                            long id = cursor.getLong(cursor.getColumnIndex(Contract.Tag._ID));
                            Goal.addGoal(getActivity(), id, name);
                        }
                        cursor.close();
                    }
                    new CreateGoalButton().execute();
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
                    Goal.deleteGoal(getActivity(), name);
                    new CreateGoalButton().execute();
                }
            }).start();
        }
    }

}

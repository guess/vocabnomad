package ca.taglab.vocabnomad.olm;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.types.Goal;


public class GoalsListFragment extends Fragment {
    private static final String TAG = "GoalsListFragment";
    private int mNumGoals;
    private boolean running = false;
    private View mList;
    private View mAddButton;
    private View mProgress;

    private ArrayList<String> mGoals;

    @Override
    public void onResume() {
        super.onResume();
        if (!running) {
            running = true;
            mList.setVisibility(View.INVISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            new SetGoals().execute();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNumGoals = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.olm_goal_list, container, false);
        if (layout != null) {
            mProgress = layout.findViewById(R.id.progress);
            mList = layout.findViewById(R.id.fragment_container);
            mAddButton = layout.findViewById(R.id.add_goal);
            mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), SearchGoalActivity.class);
                    intent.putExtra(SearchGoalActivity.ADD_GOAL, true);
                    startActivity(intent);
                }
            });
        }
        return layout;
    }

    class SetGoals extends AsyncTask<Void, Void, ArrayList<Fragment>> {

        @Override
        protected ArrayList<Fragment> doInBackground(Void... voids) {
            ArrayList<String> goals = new ArrayList<String>();
            ArrayList<Fragment> fragments = new ArrayList<Fragment>();
            Cursor cursor = Goal.getActiveGoals(getActivity());
            if (cursor != null) {
                ActiveGoalFragment fragment;
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String goal = cursor.getString(cursor.getColumnIndex(Contract.Goals.GOAL_NAME));
                    Log.i(TAG, "Name: " + goal);
                    fragment = ActiveGoalFragment.newInstance(goal);
                    fragments.add(cursor.getPosition(), fragment);
                    goals.add(goal);
                    cursor.moveToNext();
                }
                cursor.close();
            }

            if (!hasGoalsChanged(goals)) {
                // Do not update the UI if the goals are the same
                return null;
            }

            return fragments;
        }

        private boolean hasGoalsChanged(ArrayList<String> curGoals) {
            boolean isChanged = true;

            // Load new goals if the array is not in memory, or if the list sizes are different
            if (mGoals != null && mGoals.size() == curGoals.size()) {
                isChanged = false;
                // Since the list sizes are the same, make sure all of the goals are the same
                for (String goal : curGoals) {
                    if (!mGoals.contains(goal)) {
                        isChanged = true;
                    }
                }
            }

            if (isChanged) {
                // Update the goals in memory if the list has changed
                mGoals = curGoals;
            }

            return isChanged;


        }

        @Override
        protected void onPostExecute(final ArrayList<Fragment> fragments) {
            FragmentTransaction ft;

            if (fragments == null) {
                mList.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                running = false;
                return;
            }

            // Remove all previous goals
            ft = getChildFragmentManager().beginTransaction();
            for (int i = 0; i < mNumGoals; i++) {
                switch (i) {
                    case 0:
                        ft.remove(getChildFragmentManager().findFragmentById(R.id.goal1));
                        break;
                    case 1:
                        ft.remove(getChildFragmentManager().findFragmentById(R.id.goal2));
                        break;
                    case 2:
                        ft.remove(getChildFragmentManager().findFragmentById(R.id.goal3));
                        break;
                    case 3:
                        ft.remove(getChildFragmentManager().findFragmentById(R.id.goal4));
                        break;
                }
            }
            ft.commit();

            // Hide the add goals button if there are 4 goals
            mNumGoals = fragments.size();
            if (mNumGoals < 4) {
                mAddButton.setVisibility(View.VISIBLE);
            } else {
                mAddButton.setVisibility(View.GONE);
            }

            // Show the list again and remove the progress_bar bar
            mList.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);

            // Populate the list with the currently active goals
            ft = getChildFragmentManager().beginTransaction();
            for (int i = 0; i < mNumGoals; i++) {
                switch (i) {
                    case 0:
                        ft.replace(R.id.goal1, fragments.get(i));
                        break;
                    case 1:
                        ft.replace(R.id.goal2, fragments.get(i));
                        break;
                    case 2:
                        ft.replace(R.id.goal3, fragments.get(i));
                        break;
                    case 3:
                        ft.replace(R.id.goal4, fragments.get(i));
                        break;
                }
            }
            ft.setCustomAnimations(R.anim.slide_from_bottom, R.anim.slide_from_bottom);
            ft.commit();

            running = false;
        }
    }

}

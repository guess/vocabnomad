package ca.taglab.vocabnomad.olm;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

    @Override
    public void onResume() {
        super.onResume();
        if (!running) {
            running = true;
            mList.setVisibility(View.INVISIBLE);
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
        if (layout != null) mList = layout.findViewById(R.id.fragment_container);
        return layout;
    }

    class SetGoals extends AsyncTask<Void, Void, ArrayList<Fragment>> {

        @Override
        protected ArrayList<Fragment> doInBackground(Void... voids) {
            ArrayList<Fragment> fragments = new ArrayList<Fragment>();
            Cursor cursor = Goal.getActiveGoals(getActivity());
            if (cursor != null) {
                ActiveGoalFragment fragment;
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Log.i(TAG, "Name: " + cursor.getString(cursor.getColumnIndex(Contract.Goals.GOAL_NAME)));
                    fragment = ActiveGoalFragment.newInstance(
                            cursor.getString(cursor.getColumnIndex(Contract.Goals.GOAL_NAME)));
                    fragments.add(cursor.getPosition(), fragment);
                    cursor.moveToNext();
                }
                cursor.close();
            }
            return fragments;
        }

        @Override
        protected void onPostExecute(final ArrayList<Fragment> fragments) {
            if (fragments == null || fragments.isEmpty()) {
                running = false;
                return;
            }

            // Remove all old fragments
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            for (int i = 0; i < mNumGoals; i++) {
                ft.remove(getChildFragmentManager().findFragmentByTag(Integer.toString(i)));
            }
            ft.commit();

            mList.setVisibility(View.VISIBLE);

            // Add all new fragments
            mNumGoals = fragments.size();
            for (int i = 0; i < mNumGoals; i++) {
                getChildFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_from_bottom, R.anim.slide_from_bottom)
                        .add(R.id.fragment_container, fragments.get(i), Integer.toString(i))
                        .commit();
            }

            running = false;
        }
    }

}

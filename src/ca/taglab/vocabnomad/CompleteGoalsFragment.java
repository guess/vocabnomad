package ca.taglab.vocabnomad;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.types.Goal;
import ca.taglab.vocabnomad.widgets.SimpleCursorLoader;


public class CompleteGoalsFragment extends Fragment {
    private SimpleCursorAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.olm_goal_complete_list, container, false);
        if (layout != null) {
            GridView grid = (GridView) layout.findViewById(R.id.gridview);

            mAdapter = new SimpleCursorAdapter(
                    getActivity(),
                    R.layout.olm_goal_complete_list_item,
                    null,
                    new String[]{Contract.Goals.GOAL_NAME, Contract.Goals.LEVEL},
                    new int[]{R.id.title, R.id.level},
                    0
            );

            grid.setAdapter(mAdapter);

            new GoalLoader(getActivity()).startLoading();
        }
        return layout;
    }




    class GoalLoader extends SimpleCursorLoader {

        public GoalLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
            return Goal.getCompletedGoals(getActivity(), 0);
        }

        @Override
        public void deliverResult(Cursor cursor) {
            super.deliverResult(cursor);
            mAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onDestroyView() {
        if (mAdapter.getCursor() != null) {
            mAdapter.getCursor().close();
        }
        super.onDestroyView();
    }
}
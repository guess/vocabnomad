package ca.taglab.vocabnomad.olm;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.adapter.CompletedGoalsAdapter;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.types.Goal;
import ca.taglab.vocabnomad.widgets.ImageOpt;
import ca.taglab.vocabnomad.widgets.SimpleCursorLoader;


public class CompleteGoalsFragment extends Fragment {
    private CompletedGoalsAdapter mAdapter;
    private GoalLoader mLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.olm_goal_complete_list, container, false);
        if (layout != null) {
            GridView grid = (GridView) layout.findViewById(R.id.gridview);

            mAdapter = new CompletedGoalsAdapter(getActivity(), null, 0);

            grid.setAdapter(mAdapter);

            mLoader = new GoalLoader(getActivity());
            mLoader.startLoading();
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
        mLoader.reset();
        super.onDestroyView();
    }
}

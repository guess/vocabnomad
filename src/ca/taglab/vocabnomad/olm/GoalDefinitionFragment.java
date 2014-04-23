package ca.taglab.vocabnomad.olm;



import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link GoalDefinitionFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GoalDefinitionFragment extends Fragment {
    private static final String GOAL = "goal";
    private String mGoal;

    private View mLayout;

    private TagDetailsListener mListener;

    public static GoalDefinitionFragment newInstance(String goal) {
        GoalDefinitionFragment fragment = new GoalDefinitionFragment();
        Bundle args = new Bundle();
        args.putString(GOAL, goal);
        fragment.setArguments(args);
        return fragment;
    }
    public GoalDefinitionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGoal = getArguments().getString(GOAL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.card_item, parent, false);
        if (mLayout != null) {
            mLayout.setVisibility(View.GONE);
            TextView definition = (TextView) mLayout.findViewById(R.id.body);
            definition.setVisibility(View.VISIBLE);
            new LoadDefinition(definition).execute(mGoal);
            TextView title = (TextView) mLayout.findViewById(R.id.title);
            title.setText("Definition");
            mLayout.findViewById(R.id.image).setVisibility(View.GONE);
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


    class LoadDefinition extends AsyncTask<String, Void, String> {
        private TextView mText;

        LoadDefinition(TextView text) {
            this.mText = text;
        }

        @Override
        protected String doInBackground(String... goals) {
            String definition = null;

            String goal = goals[0];
            Cursor cursor = getActivity().getContentResolver().query(
                    Contract.Word.getUri(),
                    Contract.Word.PROJECTION,
                    Contract.Word.ENTRY + "=?",
                    new String[] { goal },
                    null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    definition = cursor.getString(cursor.getColumnIndex(Contract.Word.DEFINITION));
                }
                cursor.close();
            }

            return definition;
        }

        @Override
        protected void onPostExecute(String definition) {
            if (!TextUtils.isEmpty(definition)) {
                mLayout.setVisibility(View.VISIBLE);
                mText.setText(definition);
            } else if (mListener != null) {
                mListener.noDefinitionExists();
            }
        }
    }

}

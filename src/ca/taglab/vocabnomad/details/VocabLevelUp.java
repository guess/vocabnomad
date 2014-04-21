package ca.taglab.vocabnomad.details;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.types.VocabLevel;
import ca.taglab.vocabnomad.widgets.TypefacedTextView;

public class VocabLevelUp extends Fragment {
    private static final String WORD_ID = "word_id";
    private static final String WRITE_POINTS = "write";
    private static final String READ_POINTS = "read";
    private static final String SPEAK_POINTS = "speak";
    private static final String LISTEN_POINTS = "listen";

    private long mWordId;
    private int mWritePoints;
    private int mReadPoints;
    private int mSpeakPoints;
    private int mListenPoints;

    private View mLayout;

    private VocabDetailsListener mListener;


    public static VocabLevelUp newInstance(long id, int write, int read, int speak, int listen) {
        VocabLevelUp fragment = new VocabLevelUp();
        Bundle args = new Bundle();
        args.putLong(WORD_ID, id);
        args.putInt(WRITE_POINTS, write);
        args.putInt(READ_POINTS, read);
        args.putInt(SPEAK_POINTS, speak);
        args.putInt(LISTEN_POINTS, listen);
        fragment.setArguments(args);
        return fragment;
    }

    public VocabLevelUp() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWordId = getArguments().getLong(WORD_ID);
            mWritePoints = getArguments().getInt(WRITE_POINTS);
            mReadPoints = getArguments().getInt(READ_POINTS);
            mSpeakPoints = getArguments().getInt(SPEAK_POINTS);
            mListenPoints = getArguments().getInt(LISTEN_POINTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.details_level_up, parent, false);
        if (mLayout != null) {
            ((TextView) mLayout.findViewById(R.id.write)).setText("+" + mWritePoints + " exp");
            ((TextView) mLayout.findViewById(R.id.read)).setText("+" + mReadPoints + " exp");
            ((TextView) mLayout.findViewById(R.id.listen)).setText("+" + mListenPoints + " exp");
            ((TextView) mLayout.findViewById(R.id.speak)).setText("+" + mSpeakPoints + " exp");
            new LevelUp().execute();
        }
        return mLayout;
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    class LevelUp extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            return VocabLevel.levelUp(getActivity(), mWordId);
        }

        @Override
        protected void onPostExecute(Integer level) {
            ((TextView) mLayout.findViewById(R.id.level)).setText("Level " + level + "!");
        }
    }

}

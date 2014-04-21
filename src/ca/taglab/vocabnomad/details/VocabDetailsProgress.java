package ca.taglab.vocabnomad.details;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import ca.taglab.vocabnomad.R;


public class VocabDetailsProgress extends Fragment {
    public static final String TAG = "VocabDetailsProgress";

    private static final String PROGRESS = "progress";
    private static final String SKILL = "skill";
    public static final String MAX_PROGRESS = "max_progress";

    private int mProgress;
    private int mSkill;
    private int mMaxProgress;

    public static final int NONE = 0;
    public static final int READ = 1;
    public static final int WRITE = 2;
    public static final int SPEAK = 3;
    public static final int LISTEN = 4;


    private VocabDetailsListener mListener;


    public static VocabDetailsProgress newInstance(int skill, int progress, int max) {
        VocabDetailsProgress fragment = new VocabDetailsProgress();
        Bundle args = new Bundle();
        args.putInt(SKILL, skill);
        args.putInt(PROGRESS, progress);
        args.putInt(MAX_PROGRESS, max);
        fragment.setArguments(args);
        return fragment;
    }


    public VocabDetailsProgress() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSkill = getArguments().getInt(SKILL);
            mProgress = getArguments().getInt(PROGRESS);
            mMaxProgress = getArguments().getInt(MAX_PROGRESS);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.details_progress, parent, false);
        if (layout != null) {
            ProgressBar progress = (ProgressBar) layout.findViewById(R.id.progress);
            progress.setMax(mMaxProgress);
            progress.setProgress(mProgress);

            ImageView image = (ImageView) layout.findViewById(R.id.skill);
            switch (mSkill) {
                case NONE:
                    image.setVisibility(View.GONE);
                    break;
                case READ:
                    image.setImageResource(R.drawable.read);
                    break;
                case WRITE:
                    image.setImageResource(R.drawable.write);
                    break;
                case LISTEN:
                    image.setImageResource(R.drawable.listen);
                    break;
                case SPEAK:
                    image.setImageResource(R.drawable.speak);
                    break;
            }
        }

        return layout;
    }


    public void onProgressCompleted() {
        if (mListener != null) {
            mListener.onProgressComplete();
        }
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

}

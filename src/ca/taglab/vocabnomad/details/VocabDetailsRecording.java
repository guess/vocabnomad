package ca.taglab.vocabnomad.details;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.SQLException;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;


public class VocabDetailsRecording extends AudioRecorderFragment {
    public static final String TAG = "VocabDetailsRecording";

    private static final String WORD_ID = "word_id";
    private long mWordId;
    private ViewGroup mList;

    // Recorder
    private boolean mIsRecording = false;
    private boolean mIsSpeaking = false;

    // Audio player
    private String mPlaying = null;
    private AudioPlayer mPlayer;

    private VocabDetailsListener mListener;

    public static VocabDetailsRecording newInstance(long id) {
        VocabDetailsRecording fragment = new VocabDetailsRecording();
        Bundle args = new Bundle();
        args.putLong(WORD_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWordId = getArguments().getLong(WORD_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.details_recordings, container, false);
        mPlayer = new AudioPlayer();

        if (layout != null) {
            mList = (ViewGroup) layout.findViewById(R.id.recordings);
            layout.findViewById(R.id.speak).setOnClickListener(new RecordButtonClickListener());
        }
        return layout;
    }


    @Override
    protected void onAmplitudeChanged(int amplitude) {
        if (amplitude > 1800) {
            Log.d(TAG, "Amplitude: " + amplitude);
            mIsSpeaking = true;
        }
    }

    @Override
    protected long entryId() {
        return mWordId;
    }

    private View createRecordingView(final String path) {
        if (TextUtils.isEmpty(path)) return null;

        final View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.details_recordings_item, mList, false);

        if (view != null) {
            view.setTag(path);
            ((TextView) view.findViewById(R.id.recording_name)).setText(path);
            view.setOnClickListener(new RecordingItemClickListener());
        }

        return view;
    }

    private void addRecordingView(View view) {
        if (view != null) {
            mList.addView(view, 0);
        }
    }

    @Override
    public void onDestroyView() {
        mPlayer.release();
        super.onDestroyView();
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

    class RecordingItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String path = (String) view.getTag();
            ImageView button = (ImageView) view.findViewById(R.id.recording_play);
            mPlayer.onClick(path, button);
        }
    }

    class RecordButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            ImageView button = (ImageView) view.findViewById(R.id.record);
            TextView status = (TextView) view.findViewById(R.id.status);
            if (!startRecording()) {
                // Stop the recording
                mIsRecording = false;
                button.setImageResource(R.drawable.record);
                status.setText(getResources().getString(R.string.start_recording));
                String path = stopRecording();
                new AddRecording().execute(path);
            } else {
                // The recording has been started
                mIsRecording = true;
                button.setImageResource(R.drawable.stop);
                status.setText(getResources().getString(R.string.stop_recording));
                new SpeakingFeedback().start();
            }
        }
    }

    private class SpeakingFeedback extends Thread {
        @Override
        public void run() {
            while (mIsRecording) {
                if (mIsSpeaking && mListener != null) {
                    mListener.onProgressIncrement(VocabDetailsProgress.SPEAK);
                    mIsSpeaking = false;
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class AudioPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener {
        ImageView mButton;
        private String mPath;

        public AudioPlayer() {
            super();
            setOnCompletionListener(this);
        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            stopPlaying();
        }

        @Override
        public void release() {
            stopPlaying();
            super.release();
        }

        private void stopPlaying() {
            reset();
            if (mListener != null) {
                mListener.onStopProgressIncrement();
            }
            if (mButton != null) {
                mButton.setImageResource(R.drawable.play);
            }
        }

        public void onClick(String path, ImageView button) {
            if (path != null) {
                if (!isPlaying()) {
                    // The audio player is not playing anything. Play this.
                    this.mButton = button;
                    this.mPath = path;
                    startPlaying();
                } else if (path.equalsIgnoreCase(mPath)) {
                    // The audio recorder is playing something. Stop if it is the clicked button.
                    stopPlaying();
                }
            }
        }

        private void startPlaying() {
            try {
                setDataSource(this.mPath);
                prepare();
                start();
                if (mListener != null) {
                    mListener.onStartProgressIncrement(VocabDetailsProgress.LISTEN);
                }
                if (mButton != null) {
                    mButton.setImageResource(R.drawable.stop);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error: Media player failed preparing");
                stopPlaying();
            }
        }

    }


    class AddRecording extends AsyncTask<String, Void, View> {

        @Override
        protected View doInBackground(String... paths) {
            String path = paths[0];
            addRecordingToDatabase(path);
            return createRecordingView(path);
        }

        @Override
        protected void onPostExecute(View view) {
            addRecordingView(view);
        }

        private void addRecordingToDatabase(String path) {
            ContentValues values = new ContentValues();
            values.put(Contract.VoiceClip.WORD_ID, mWordId);
            values.put(Contract.VoiceClip.LOCATION, path);

            Uri uri = ContentUris.withAppendedId(Contract.VoiceClip.CONTENT_URI, mWordId);

            if (uri != null) {
                try {
                    // Try to insert the entry in the database
                    getActivity().getContentResolver().insert(uri, values);
                } catch (SQLException e) {
                    // The entry already exists, try to update it
                    getActivity().getContentResolver().update(uri, values, null, null);
                }
            }
        }
    }
}

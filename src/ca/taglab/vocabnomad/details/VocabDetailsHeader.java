package ca.taglab.vocabnomad.details;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.widgets.ImageOpt;


public class VocabDetailsHeader extends Fragment implements TextToSpeech.OnInitListener {
    public static final String TAG = "VocabDetailsHeader";

    private static final String WORD_ID = "word_id";
    private Uri mWordUri;
    private long mWordId;

    private TextToSpeech TTS;

    private View mLayout;

    private VocabDetailsListener mListener;

    public static VocabDetailsHeader newInstance(long id) {
        VocabDetailsHeader fragment = new VocabDetailsHeader();
        Bundle args = new Bundle();
        args.putLong(WORD_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public VocabDetailsHeader() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWordId = getArguments().getLong(WORD_ID);
            mWordUri = ContentUris.withAppendedId(Contract.Word.getUri(), mWordId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.details_word, container, false);
        if (mLayout != null) {

            new LoadHeader().execute();

            // TODO: Load the level
            ((TextView) mLayout.findViewById(R.id.level)).setText("Level: 1");
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
        if (TTS != null) {
            TTS.stop();
            TTS.shutdown();
        }
    }

    @Override
    public void onInit(int status) {
        // Status can either be TextToSpeech.SUCCESS or TextToSpeech.ERROR
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US English
            int result = TTS.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Language data is missing or the language is not supported
                Log.e(TAG, "Language is not available");
            }
        } else {
            // Initialization failed
            Log.e(TAG, "Could not initialize TextToSpeech");
        }
    }

    private void initListening(final String body) {
        TTS = new TextToSpeech(getActivity(), this);
        TTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            mListener.onStartProgressIncrement(VocabDetailsProgress.LISTEN);
                        }
                    }
                });
            }

            @Override
            public void onDone(String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            mListener.onStopProgressIncrement();
                        }
                    }
                });
            }

            @Override
            public void onError(String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            mListener.onStopProgressIncrement();
                        }
                    }
                });
            }
        });

        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TTS.isSpeaking()) {
                    TTS.stop();
                } else {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
                    TTS.speak(body, TextToSpeech.QUEUE_FLUSH, map);
                }
            }
        });
    }

    class LoadHeader extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String path = null, word = "";

            Cursor cursor = getActivity().getContentResolver().query(mWordUri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    word = cursor.getString(cursor.getColumnIndex(Contract.Word.ENTRY));
                    path = Environment.getExternalStorageDirectory() + "/VocabNomad/" +
                            cursor.getString(cursor.getColumnIndex(Contract.Word.IMG));
                }
                cursor.close();
            }

            new LoadImage().execute(path);
            return word;
        }

        @Override
        protected void onPostExecute(String word) {
            ((TextView) mLayout.findViewById(R.id.title)).setText(word);
            initListening(word);
        }
    }

    class LoadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... paths) {
            if (paths[0] == null) return null;
            return ImageOpt.decodeSampledBitmapFromFile(paths[0], 100, 100);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView image = (ImageView) mLayout.findViewById(R.id.image);
            if (bitmap != null) {
                image.setImageBitmap(bitmap);
            } else {
                image.setImageResource(R.drawable.image_placeholder);
            }
        }
    }

}

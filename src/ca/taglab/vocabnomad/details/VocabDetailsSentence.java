package ca.taglab.vocabnomad.details;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;


public class VocabDetailsSentence extends Fragment implements TextToSpeech.OnInitListener {
    private static final String TAG = "VocabDetailsSentence";

    private static final String WORD_ID = "word_id";
    private static final String EDIT = "edit";
    private Uri mWordUri;
    private boolean isEdit;
    private EditText mEdit;
    private View mViewWord;

    private TextToSpeech TTS;
    private ImageView TTSStatus;

    private VocabDetailsListener mListener;


    public static VocabDetailsSentence newInstance(long id, boolean isEdit) {
        VocabDetailsSentence fragment = new VocabDetailsSentence();
        Bundle args = new Bundle();
        args.putLong(WORD_ID, id);
        args.putBoolean(EDIT, isEdit);
        fragment.setArguments(args);
        return fragment;
    }


    public VocabDetailsSentence() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            long id = getArguments().getLong(WORD_ID);
            isEdit = getArguments().getBoolean(EDIT);
            mWordUri = ContentUris.withAppendedId(Contract.Word.getUri(), id);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.details_sentence, parent, false);
        if (layout != null) {
            mEdit = (EditText) layout.findViewById(R.id.edit);
            mEdit.addTextChangedListener(new VocabWriteWatcher(mListener));
            TextView text = (TextView) layout.findViewById(R.id.text);
            mViewWord = layout.findViewById(R.id.view);

            TTSStatus = (ImageView) layout.findViewById(R.id.listen);

            if (isEdit) {
                new GetSentence(mEdit).execute();
                layout.findViewById(R.id.option_write).setBackgroundColor(Color.parseColor("#3049E20E"));
                mViewWord.setVisibility(View.GONE);
                mEdit.setVisibility(View.VISIBLE);
            } else {
                new GetSentence(text).execute();
                mViewWord.setVisibility(View.VISIBLE);
                mEdit.setVisibility(View.GONE);
            }

            layout.findViewById(R.id.option_write).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onEditPressed();
                }
            });
        }
        return layout;
    }


    private void onEditPressed() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isEdit) {
                    ContentValues values = new ContentValues();
                    String sentence = "";
                    if (mEdit.getText() != null) sentence = mEdit.getText().toString();
                    values.put(Contract.Word.SENTENCE, sentence);
                    getActivity().getContentResolver().update(mWordUri, values, null, null);
                }
                if (mListener != null) {
                    mListener.onEditPressed(VocabDetailsListener.SENTENCE, isEdit);
                }
            }
        }).start();
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (TTS != null) {
            TTS.stop();
            TTS.shutdown();
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
                        TTSStatus.setImageResource(R.drawable.stop);
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
                        TTSStatus.setImageResource(R.drawable.play);
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
                        TTSStatus.setImageResource(R.drawable.play);
                        if (mListener != null) {
                            mListener.onStopProgressIncrement();
                        }
                    }
                });
            }
        });

        mViewWord.setOnClickListener(new View.OnClickListener() {
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


    /**
     * Get the sentence from the database
     */
    class GetSentence extends AsyncTask<Void, Void, String> {
        TextView view;

        GetSentence(TextView view) {
            this.view = view;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String sentence = "";

            Cursor cursor = getActivity().getContentResolver().query(mWordUri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    sentence = cursor.getString(cursor.getColumnIndex(Contract.Word.SENTENCE));
                }
                cursor.close();
            }

            return sentence;
        }

        @Override
        protected void onPostExecute(String sentence) {
            this.view.setText(sentence);
            initListening(sentence);
            if (TextUtils.isEmpty(sentence) && !isEdit) onEditPressed();
        }
    }
}

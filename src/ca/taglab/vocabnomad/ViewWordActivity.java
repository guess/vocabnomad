package ca.taglab.vocabnomad;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.UserEvents;
import ca.taglab.vocabnomad.types.Goal;
import ca.taglab.vocabnomad.types.VocabLevel;
import ca.taglab.vocabnomad.types.Word;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class ViewWordActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final String TAG = "ViewWordActivity";
    private TextToSpeech mTts;
    private Word word;

    TextView wordView, sentenceView, definitionView;
    CheckBox shared;

    private boolean isTtsOn;

    Button mPlay, mRecord;
    TextView mAudioMessage;

    private static String mSoundFileName, mRecordFileName;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;

    boolean mStartRecording;
    boolean mStartPlaying;

    boolean firstUse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_word);

        SharedPreferences settings = getApplicationContext().getSharedPreferences(TAG, 0);
        final SharedPreferences.Editor editor = settings.edit();
        firstUse = settings.getBoolean("first", true);
        if (firstUse) {
            findViewById(R.id.tip).setVisibility(View.VISIBLE);
        }

        // Save the audio file in external storage
        initRecorder();

        // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        mTts = new TextToSpeech(this, this);


        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.search_bg));
        actionBar.setTitle("");

        mAudioMessage = (TextView) findViewById(R.id.audio);

        mStartPlaying = true;
        mPlay = (Button) findViewById(R.id.play);
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //v.setBackgroundResource(R.drawable.stop);
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    mPlay.setBackgroundResource(R.drawable.stop);

                    UserEvents.log(
                            Contract.UserEvents.PLAY_RECORDING,
                            word.getId(),
                            word.getServerId(),
                            0, 0, "w: " + word.getWord() + " - f: " + mSoundFileName
                    );
                } else {
                    mPlay.setBackgroundResource(R.drawable.play);
                }
                mStartPlaying = !mStartPlaying;
            }
        });
        mPlay.setVisibility(View.GONE);

        mStartRecording = true;
        mRecord = (Button) findViewById(R.id.record);
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecord.setBackgroundResource(R.drawable.stop);
                    mAudioMessage.setText(R.string.say_something);

                    UserEvents.log(
                            Contract.UserEvents.RECORD_VOICE,
                            word.getId(),
                            word.getServerId(),
                            0, 0, "w: " + word.getWord() + " - f: " + mRecordFileName
                    );
                } else {
                    mRecord.setBackgroundResource(R.drawable.record);
                    mAudioMessage.setText(R.string.your_recording);
                }
                mStartRecording = !mStartRecording;
            }
        });

        wordView = (TextView) findViewById(R.id.word);
        wordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTtsOn) {
                    mTts.speak(word.getWord(), TextToSpeech.QUEUE_FLUSH, null);
                    UserEvents.log(
                            Contract.UserEvents.HEAR_VOC,
                            word.getId(),
                            word.getServerId(),
                            0, 0, word.getWord()
                    );

                    if (firstUse) {
                        findViewById(R.id.tip).setVisibility(View.GONE);
                        editor.putBoolean("first", false);
                        editor.commit();
                    }
                }
            }
        });

        sentenceView = (TextView) findViewById(R.id.sentence);
        sentenceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTtsOn) {
                    mTts.speak(word.getSentence(), TextToSpeech.QUEUE_FLUSH, null);
                    UserEvents.log(
                            Contract.UserEvents.HEAR_SENTENCE,
                            word.getId(),
                            word.getServerId(),
                            0, 0, word.getSentence()
                    );

                    if (firstUse) {
                        findViewById(R.id.tip).setVisibility(View.GONE);
                        editor.putBoolean("first", false);
                        editor.commit();
                    }
                }
            }
        });

        definitionView = (TextView) findViewById(R.id.definition);
        definitionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTtsOn) {
                    mTts.speak(word.getDefinition(), TextToSpeech.QUEUE_FLUSH, null);
                    UserEvents.log(
                            Contract.UserEvents.HEAR_DEFINITION,
                            word.getId(),
                            word.getServerId(),
                            0, 0, word.getDefinition()
                    );

                    if (firstUse) {
                        findViewById(R.id.tip).setVisibility(View.GONE);
                        editor.putBoolean("first", false);
                        editor.commit();
                    }
                }
            }
        });

        shared = (CheckBox) findViewById(R.id.shared);
        shared.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        word.setShared(isChecked);
                        try {
                            word.commit(getContentResolver());
                            UserEvents.log(
                                    (isChecked ? Contract.UserEvents.SHARE : Contract.UserEvents.UNSHARE),
                                    word.getId(),
                                    word.getServerId(),
                                    0, 0, null
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        refreshView();

    }

    public void initRecorder() {
        // Save the audio file in external storage
        File folder = new File(Environment.getExternalStorageDirectory() + "/VocabNomad/audio");
        boolean isFolderCreated = true;
        if (!folder.exists()) {
            Log.d(TAG, "Creating audio folder");
            isFolderCreated = folder.mkdirs();
        }

        if (isFolderCreated) {
            mRecordFileName = folder.getAbsolutePath() + "/"
                    + getIntent().getExtras().getLong("id") + "-" + new Date().getTime() + ".3gp";
            Log.d(TAG, "File path of audio recording: " + mRecordFileName);
        } else {
            mRecordFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.3gp";
            Log.d(TAG, "Error: Audio folder was not found or created");
        }
    }

    public void refreshView() {
        long id = getIntent().getExtras().getLong("id");
        new GetPoints().execute(id);
        word = new Word();
        word.refresh(ContentUris.withAppendedId(Contract.Word.getUri(), id), getContentResolver());

        wordView.setText(word.getWord());
        sentenceView.setText(word.getSentence());
        definitionView.setText(word.getDefinition());
        shared.setChecked(word.isShared());

        if (!TextUtils.isEmpty(word.getImageFilePath())) {
            File f = new File(Environment.getExternalStorageDirectory() + "/VocabNomad", word.getImageFilePath());
            ((ImageView) findViewById(R.id.image))
                    .setImageBitmap(VocabListActivity.getImage(f, 150));
        } else {
            ((ImageView) findViewById(R.id.image))
                    .setImageResource(R.drawable.image_placeholder_normal);
        }

        new GetTags().execute(word);

        new GetVoiceClip().execute(word.getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserEvents.log(Contract.UserEvents.VIEW_DETAILED_VOC, word.getId(), word.getServerId(), 0, 0, word.getWord());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.view, menu);

        SharedPreferences settings = getApplicationContext().getSharedPreferences(TAG, 0);
        if (settings.getBoolean("tts", true)) {
            isTtsOn = true;
            menu.findItem(R.id.action_tts).setIcon(R.drawable.speaker_on);
        } else {
            isTtsOn = false;
            menu.findItem(R.id.action_tts).setIcon(R.drawable.speaker_muted);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, VocabListActivity.class));
                return true;

            case R.id.action_edit:
                Intent intent = new Intent(this, EditWordActivity.class);
                intent.putExtra("id", word.getId());
                startActivityForResult(intent, 0);
                return true;

            case R.id.action_tts:
                if (isTtsOn) {
                    item.setIcon(R.drawable.speaker_muted);
                    mTts.stop();
                } else {
                    item.setIcon(R.drawable.speaker_on);
                }
                isTtsOn = !isTtsOn;

                SharedPreferences settings = getApplicationContext().getSharedPreferences(TAG, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("tts", isTtsOn);
                editor.commit();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }

        if (word.cursor != null) {
            word.cursor.close();
            word.cursor = null;
        }

        if (!mStartPlaying) {
            stopPlaying();
        }

        if (!mStartRecording) {
            stopRecording();
        }

        setResult(RESULT_CANCELED);

        super.onDestroy();
    }

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english. If it's not available, result will indicate this.
            int result = mTts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Language data is missing or the language is not supported.
                Log.e(TAG, "Language is not available");
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_OK:
                refreshView();
                break;

            default:
                break;
        }
    }

    class GetTags extends AsyncTask<Word, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Word... params) {
            return getContentResolver().query(
                    Uri.withAppendedPath(word.getUri(), "tags"),
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        protected void onPostExecute(Cursor tags) {
            super.onPostExecute(tags);

            LinearLayout list = (LinearLayout) findViewById(R.id.tags);
            list.removeAllViewsInLayout();

            if (tags != null) {
                tags.moveToFirst();
                while (!tags.isAfterLast()) {
                    final TextView tag = (TextView) LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.tag, list, false);
                    tag.setText(tags.getString(tags.getColumnIndex(Contract.Tag.NAME)));
                    tag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.putExtra("tag", ((TextView) v).getText().toString());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                    list.addView(tag, 0);
                    tags.moveToNext();
                }
                tags.close();
            }
        }
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
            mPlay.setVisibility(View.GONE);
        } else {
            stopRecording();
            mPlay.setVisibility(View.VISIBLE);
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlay.setBackgroundResource(R.drawable.play);
                mAudioMessage.setText(R.string.your_recording);
                mStartPlaying = true;
            }
        });
        try {
            mPlayer.setDataSource(mSoundFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mRecordFileName);
        Log.d(TAG, "Recording to: " + mRecordFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        new SaveVoiceClip().execute(word.getId());
    }

    class GetVoiceClip extends AsyncTask<Long, Void, String> {

        @Override
        protected String doInBackground(Long... params) {
            Cursor cursor;
            String path = null;
            long id = params[0];

            cursor = getContentResolver().query(
                    ContentUris.withAppendedId(Contract.VoiceClip.CONTENT_URI, id),
                    null, null, null, null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndex(Contract.VoiceClip.LOCATION));
                }
                cursor.close();
            }

            return path;
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            if (path != null) {
                mPlay.setVisibility(View.VISIBLE);
                mSoundFileName = path;
            }
        }
    }

    class SaveVoiceClip extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... params) {
            long id = params[0];
            mSoundFileName = mRecordFileName;

            ContentValues values = new ContentValues();
            values.put(Contract.VoiceClip.WORD_ID, id);
            values.put(Contract.VoiceClip.LOCATION, mSoundFileName);
            initRecorder();

            try {
                getContentResolver().insert(ContentUris.withAppendedId(Contract.VoiceClip.CONTENT_URI, id), values);
            } catch (SQLException e) {
                getContentResolver().update(ContentUris.withAppendedId(Contract.VoiceClip.CONTENT_URI, id), values, null, null);
            }

            return null;
        }
    }

    class GetPoints extends AsyncTask<Long, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Long... ids) {
            long id = ids[0];

            if (Goal.isVocabInActiveGoal(ViewWordActivity.this, id)) {
                if (VocabLevel.hasPassedForgetDate(ViewWordActivity.this, id)) {
                    VocabLevel.levelUp(ViewWordActivity.this, id);
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean gotPoints) {
            if (gotPoints) {
                Toast.makeText(ViewWordActivity.this, "+1", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

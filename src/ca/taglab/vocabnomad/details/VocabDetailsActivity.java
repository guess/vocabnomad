package ca.taglab.vocabnomad.details;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseIntArray;
import android.view.View;

import java.lang.ref.WeakReference;
import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.types.Goal;
import ca.taglab.vocabnomad.types.VocabLevel;

public class VocabDetailsActivity extends FragmentActivity implements VocabDetailsListener {
    public static final String WORD_ID = "id";
    public static final String SKILL = "skill";
    public static final String PROGRESS = "progress";

    private static final int MAX_PROGRESS = 250;

    private long mWordId;
    private boolean mIsGoal = false;
    private Handler mHandler;
    private int mProgress = 0;

    private DrawProgressForDuration mDrawerListener;
    private int mNumDrawing = 0;

    // Experience points
    private SparseIntArray experience;

    private static WeakReference<VocabDetailsActivity> wrActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_word_activity);

        wrActivity = new WeakReference<VocabDetailsActivity>(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mWordId = bundle.getLong(WORD_ID);
        }

        // Vocabulary header
        VocabDetailsHeader header = VocabDetailsHeader.newInstance(mWordId);

        // Vocabulary details
        VocabDetailsRecording recordings = VocabDetailsRecording.newInstance(mWordId);
        VocabDetailsSentence sentence = VocabDetailsSentence.newInstance(mWordId, false);
        VocabDetailsDefinition definition = VocabDetailsDefinition.newInstance(mWordId, false);
        VocabDetailsTags tags = VocabDetailsTags.newInstance(mWordId, false);
        VocabDetailsShared shared = VocabDetailsShared.newInstance(mWordId);

        // Add the fragments to the screen
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.word_header, header)
                .replace(R.id.recordings, recordings)
                .replace(R.id.sentence, sentence)
                .replace(R.id.definition, definition)
                .replace(R.id.tags, tags)
                .replace(R.id.shared, shared)
                .commit();

        initExperiencePoints();
        new ActivateProgress().execute();
    }

    private void initExperiencePoints() {
        experience = new SparseIntArray();
        experience.put(VocabDetailsProgress.READ, 0);
        experience.put(VocabDetailsProgress.WRITE, 0);
        experience.put(VocabDetailsProgress.LISTEN, 0);
        experience.put(VocabDetailsProgress.SPEAK, 0);
    }

    private void addExperience(int skill) {
        int current = experience.get(skill);
        experience.put(skill, ++current);
    }

    @Override
    public void onEditPressed(int id, boolean isEditing) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        switch (id) {
            case HEADER:
                break;
            case SENTENCE:
                VocabDetailsSentence sentence = VocabDetailsSentence.newInstance(mWordId, !isEditing);
                transaction.replace(R.id.sentence, sentence);
                transaction.commit();
                break;
            case DEFINITION:
                VocabDetailsDefinition definition = VocabDetailsDefinition.newInstance(mWordId, !isEditing);
                transaction.replace(R.id.definition, definition).commit();
                break;
            case TAGS:
                VocabDetailsTags tags = VocabDetailsTags.newInstance(mWordId, !isEditing);
                transaction.replace(R.id.tags, tags).commit();
                break;
            case SHARED:
                break;
        }
    }

    @Override
    public void onTagPressed(String name) {
        Intent intent = new Intent();
        intent.putExtra("tag", name);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onProgressComplete() {
        // Open the level up prompt
        VocabLevelUp dialogue = VocabLevelUp.newInstance(
                mWordId,
                experience.get(VocabDetailsProgress.WRITE),
                experience.get(VocabDetailsProgress.READ),
                experience.get(VocabDetailsProgress.SPEAK),
                experience.get(VocabDetailsProgress.LISTEN)
        );
        getSupportFragmentManager().beginTransaction()
                .add(R.id.word_details, dialogue)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onStartProgressIncrement(int skill) {
        if (mNumDrawing == 0) {
            // If it is the first time calling this, create a new object
            mDrawerListener = new DrawProgressForDuration(skill);
            mDrawerListener.start();
        }
        mNumDrawing++;
    }

    @Override
    public void onStopProgressIncrement() {
        if (--mNumDrawing == 0) {
            // Only stop when all callers have stopped progress
            mDrawerListener.stopDrawing();
        }
    }

    @Override
    public void onProgressIncrement(int skill) {
        if (mIsGoal) {
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putInt(SKILL, skill);
            bundle.putInt(PROGRESS, ++mProgress);
            message.setData(bundle);
            mHandler.sendMessage(message);
            addExperience(skill);
        }
    }

    @Override
    public void onClosePrompt() {
        // Close the prompt by popping the back stack
        getSupportFragmentManager().popBackStack();

        // Close the experience points fragment and update the vocabulary header
        VocabDetailsHeader header = VocabDetailsHeader.newInstance(mWordId);
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentById(R.id.word_progress))
                .replace(R.id.word_header, header)
                .commit();
    }

    class ActivateProgress extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean isGoal = Goal.isVocabInActiveGoal(VocabDetailsActivity.this, mWordId);
            boolean isForgotten = VocabLevel.hasPassedForgetDate(VocabDetailsActivity.this, mWordId);
            return isGoal && isForgotten;
        }

        @Override
        protected void onPostExecute(Boolean isGoal) {
            mIsGoal = isGoal;
            if (isGoal) {

                VocabDetailsProgress progress = VocabDetailsProgress
                        .newInstance(VocabDetailsProgress.NONE, 0, MAX_PROGRESS);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.word_progress, progress).commit();

                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (wrActivity.get() != null && !wrActivity.get().isFinishing()) {
                            Bundle bundle = msg.getData();
                            if (bundle != null) {
                                int skill = bundle.getInt(SKILL);
                                int progress = bundle.getInt(PROGRESS);

                                VocabDetailsProgress fragment = VocabDetailsProgress
                                        .newInstance(skill, progress, MAX_PROGRESS);

                                wrActivity.get().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.word_progress, fragment)
                                        .commitAllowingStateLoss();
                            }
                        }
                    }
                };

                new DrawProgress(VocabDetailsProgress.READ, 25).start();
            }
        }
    }

    private class DrawProgressForDuration extends Thread {
        private int skill;
        private boolean isDrawing;

        DrawProgressForDuration(int skill) {
            this.skill = skill;
            this.isDrawing = true;
        }

        public void stopDrawing() {
            isDrawing = false;
        }

        @Override
        public void run() {
            while (isDrawing) {
                onProgressIncrement(skill);
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *  Incrementally draw an amount of progress for a particular skill on the progress bar.
     */
    private class DrawProgress extends Thread {
        private int skill;
        private int amount;

        DrawProgress(int skill, int amount) {
            this.skill = skill;
            this.amount = amount;
        }

        @Override
        public void run() {
            for (int i = 0; i < amount; i++) {
                onProgressIncrement(skill);
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

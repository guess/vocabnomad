package ca.taglab.vocabnomad.details;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.types.Word;

public class VocabDetailsActivity extends FragmentActivity implements VocabDetailsListener {
    public static final String WORD_ID = "id";
    private Word word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_word_activity);
        word = new Word();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            long id = bundle.getLong(WORD_ID);
            Uri uri = ContentUris.withAppendedId(Contract.Word.getUri(), id);
            word.refresh(uri, getContentResolver());
        }

        // Vocabulary header
        VocabDetailsHeader header = VocabDetailsHeader.newInstance(word.getId());

        // Vocabulary details
        VocabDetailsRecording recordings = VocabDetailsRecording.newInstance(word.getId());
        VocabDetailsSentence sentence = VocabDetailsSentence.newInstance(word.getId(), false);
        VocabDetailsDefinition definition = VocabDetailsDefinition.newInstance(word.getId(), false);
        VocabDetailsTags tags = VocabDetailsTags.newInstance(word.getId(), false);
        VocabDetailsShared shared = VocabDetailsShared.newInstance(word.getId());

        // Add the fragments to the screen
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.word_header, header)
                .replace(R.id.recordings, recordings)
                .replace(R.id.sentence, sentence)
                .replace(R.id.definition, definition)
                .replace(R.id.tags, tags)
                .replace(R.id.shared, shared)
                .commit();
    }

    @Override
    public void onEditPressed(int id, boolean isEditing) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        switch (id) {
            case HEADER:
                break;
            case SENTENCE:
                VocabDetailsSentence sentence = VocabDetailsSentence.newInstance(word.getId(), !isEditing);
                transaction.replace(R.id.sentence, sentence);
                transaction.commit();
                break;
            case DEFINITION:
                VocabDetailsDefinition definition = VocabDetailsDefinition.newInstance(word.getId(), !isEditing);
                transaction.replace(R.id.definition, definition).commit();
                break;
            case TAGS:
                VocabDetailsTags tags = VocabDetailsTags.newInstance(word.getId(), !isEditing);
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
}

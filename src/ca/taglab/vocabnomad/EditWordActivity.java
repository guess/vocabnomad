package ca.taglab.vocabnomad;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.UserEvents;
import ca.taglab.vocabnomad.rest.DataSyncRestService;
import ca.taglab.vocabnomad.types.Tag;
import ca.taglab.vocabnomad.types.Word;
import ca.taglab.vocabnomad.types.WordTag;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditWordActivity extends Activity {
    public static final String TAG = "EditWordActivity";

    private static final int CAMERA_REQUEST = 1;
    private static final int LIBRARY_REQUEST = 2;

    private Word word;
    private long word_id;

    private EditText entry, definition, sentence, mNewTagName;
    private ImageView photo;
    private Button mAddTag;

    private boolean edited;

    private boolean photoExists;
    public String mCurrentPhotoPath;

    private ViewGroup mContainerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_word);

        edited = false;

        word_id = getIntent().getLongExtra("id", 0);
        word = new Word();

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.search_bg));
        actionBar.setTitle("");

        photo = (ImageView) findViewById(R.id.photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContextMenu(v);
            }
        });
        registerForContextMenu(photo);

        entry = (EditText) findViewById(R.id.word);
        entry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String text = ((EditText) v).getText().toString();

                if (!hasFocus && !text.equals(word.getWord())) {
                    UserEvents.log(
                            Contract.UserEvents.WORD_EDIT,
                            word.getId(),
                            word.getServerId(),
                            0, 0,
                            ((EditText) v).getText().toString()
                    );
                    edited = true;
                }
            }
        });

        definition = (EditText) findViewById(R.id.definition);
        definition.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String text = ((EditText) v).getText().toString();

                if (!hasFocus && !text.equals(word.getDefinition())) {
                    UserEvents.log(
                            Contract.UserEvents.DEFINITION_EDIT,
                            word.getId(),
                            word.getServerId(),
                            0, 0,
                            ((EditText) v).getText().toString()
                    );
                    edited = true;
                }
            }
        });

        sentence = (EditText) findViewById(R.id.sentence);
        sentence.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String text = ((EditText) v).getText().toString();

                if (!hasFocus && !text.equals(word.getSentence())) {
                    UserEvents.log(
                            Contract.UserEvents.SENTENCE_EDIT,
                            word.getId(),
                            word.getServerId(),
                            0, 0,
                            ((EditText) v).getText().toString()
                    );
                    edited = true;
                }
            }
        });

        mNewTagName = (EditText) findViewById(R.id.new_tag);

        mContainerView = (ViewGroup) findViewById(R.id.container);

        mAddTag = (Button) findViewById(R.id.add_tag);
        mAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mNewTagName.getText().toString().trim();
                UserEvents.log(
                        Contract.UserEvents.ADD_TAGS,
                        word != null ? word.getId() : 0,
                        word != null ? word.getServerId() : 0,
                        0, 0, name
                );
                add_tag(name);
                mNewTagName.setText("");
                edited = true;
            }
        });


        refresh(getApplicationContext());

        if (word_id > 0) {
            Cursor cursor = getContentResolver().query(
                    Uri.withAppendedPath(word.getUri(), "tags"),
                    null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                final int mNameCol = cursor.getColumnIndex(Contract.Tag.NAME);
                while (!cursor.isAfterLast()) {
                    if (!TextUtils.isEmpty(cursor.getString(mNameCol))) {
                        add_tag(cursor.getString(mNameCol));
                    }
                    cursor.moveToNext();
                }
                cursor.close();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (word.getId() > 0) {
            UserEvents.log(Contract.UserEvents.EDIT_VOC, word.getId(), word.getServerId(), 0, 0, word.getWord());
        } else {
            UserEvents.log(Contract.UserEvents.ADD_VOC, 0, 0, 0, 0, word.getWord());
        }
    }


    @Override
    public void onBackPressed() {
        cancel();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.edit, menu);

        if (word.getId() <= 0) {
            menu.removeItem(R.id.action_delete);
            invalidateOptionsMenu();
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancel();
                return true;

            case R.id.action_save:
                save();
                return true;

            case R.id.action_delete:
                delete();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void delete() {
        clearFocus();
        UserEvents.log(Contract.UserEvents.DELETE_VOC, word.getId(), word.getServerId(), 0, 0, word.getWord());
        word.delete(getContentResolver());
        NavUtils.navigateUpTo(this, new Intent(this, VocabListActivity.class));
    }


    public void cancel() {
        clearFocus();

        if (edited) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.quit)
                    .setMessage(R.string.really_quit)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UserEvents.log(
                                    Contract.UserEvents.CANCEL_VOC_EDIT,
                                    word.getId(),
                                    word.getServerId(),
                                    0,
                                    0,
                                    word.getWord()
                            );

                            finish();
                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        } else {
            UserEvents.log(Contract.UserEvents.CANCEL_VOC_EDIT, word.getId(), word.getServerId(), 0, 0, word.getWord());
            finish();
        }
    }

    /**
     * Save the changes made to the word.
     */
    public void save() {

        clearFocus();

        if (entry.getText().toString().trim().isEmpty()) {
            // Make sure the word is not empty
            Toast.makeText(
                    getApplicationContext(),
                    "Word cannot be blank",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String logType;
        Tag tag;

        word.setWord(entry.getText().toString().trim());
        word.setSentence(sentence.getText().toString().trim());
        word.setDefinition(definition.getText().toString().trim());
        word.setShared(word.isShared());

        if (word.getId() > 0) {
            logType = Contract.UserEvents.SAVE_VOC_EDIT;
        } else {
            logType = Contract.UserEvents.ADD_VOC;
        }

        try {
            word.refresh(
                    word.commit(getContentResolver()),
                    getContentResolver()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        UserEvents.log(logType, word.getId(), word.getServerId(), 0, 0, word.getWord());

                /*
                 *  Delete all tags associated with this word.
                 */
        ContentValues delete = new ContentValues();
        delete.put(Contract.WordTag.DELETED, 1);
        delete.put(Contract.WordTag.DATE_MODIFIED, "/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
        getContentResolver().update(
                Contract.WordTag.getUri(),
                delete,
                Contract.WordTag.WORD_ID + "=" + word.getId(),
                null
        );

                /*
                 *  Add all new tags associated with this word.
                 */
        for (int i = 0; i < mContainerView.getChildCount(); i++) {
            Log.i(
                    "EditWordActivity",
                    ((TextView) mContainerView.getChildAt(i).findViewById(android.R.id.text1)).getText().toString()
            );

            tag = new Tag();
            tag.setTag(((TextView) mContainerView.getChildAt(i).findViewById(android.R.id.text1)).getText().toString());

            try {
                tag.refresh(
                        tag.commit(getContentResolver()),
                        getContentResolver()
                );

                // Add the TAG ID and TAG SERVER ID to
                ContentValues values = new ContentValues();
                values.put(Contract.UserEvents.TAG_ID, tag.getId());
                values.put(Contract.UserEvents.TAG_SID, tag.getServerId());
                getContentResolver().update(
                        Contract.UserEvents.CONTENT_URI,
                        values,
                        Contract.UserEvents.TYPE + "=? AND " + Contract.UserEvents.USER_ENTRY + "=?",
                        new String[] {
                                Long.toString(UserEvents.mUserEventIds.get(Contract.UserEvents.ADD_TAGS)),
                                tag.getTag()
                        }
                );

                delete.put(Contract.WordTag.DELETED, 0);
                int updated = getContentResolver().update(
                        Contract.WordTag.getUri(),
                        delete,
                        Contract.WordTag.WORD_ID + "=? AND " + Contract.WordTag.TAG_ID + "=?",
                        new String[] { Long.toString(word.getId()), Long.toString(tag.getId())}
                );

                if (updated <= 0) {
                    /* Insert a new wordtag */
                    Log.i(TAG, word.getWord() + " + " + tag.getTag() + " inserted");
                    WordTag wordTag = new WordTag(word, tag);
                    wordTag.setDateModified("/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
                    wordTag.commit(getContentResolver());
                } else {
                    Log.i(TAG, word.getWord() + " + " + tag.getTag() + " updated");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        new DataSyncRestService.Send(getApplicationContext(), DataSyncRestService.VOCAB).run();
        new DataSyncRestService.Send(getApplicationContext(), DataSyncRestService.TAGS).run();
        new DataSyncRestService.Refresh(getApplicationContext(), DataSyncRestService.VOCAB).run();

        setResult(RESULT_OK);
        finish();
    }

    /**
     * Update the UI with the last saved state of the word.
     */
    private void refresh(final Context context) {
        if (word_id > 0) {

            word.refresh (
                    ContentUris.withAppendedId(Contract.Word.getUri(), word_id),
                    context.getContentResolver()
            );

            entry.setText(word.getWord());
            sentence.setText(word.getSentence());
            definition.setText(word.getDefinition());

            if (!TextUtils.isEmpty(word.getImageFilePath())) {
                File f = new File(Environment.getExternalStorageDirectory() + "/VocabNomad", word.getImageFilePath());
                photo.setImageBitmap(VocabListActivity.getImage(f, 150));
                photoExists = true;
            } else {
                photo.setImageResource(R.drawable.image_placeholder);
                photoExists = false;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            /* Took a photo with the camera */
            File f = new File(Environment.getExternalStorageDirectory() + "/VocabNomad", mCurrentPhotoPath);
            photo.setImageBitmap(VocabListActivity.getImage(f, 150));
            word.setImageFilePath(mCurrentPhotoPath);
            edited = true;
            photoExists = true;

            UserEvents.log(
                    Contract.UserEvents.IMG_EDIT,
                    word.getId(),
                    word.getServerId(),
                    0, 0, mCurrentPhotoPath
            );
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (photoExists) {
            MenuItem item = menu.add("Remove photo");
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    photo.setImageResource(R.drawable.image_placeholder);
                    word.setImageFilePath("");
                    mCurrentPhotoPath = null;
                    edited = true;
                    photoExists = false;
                    return true;
                }
            });
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.photo, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                return true;
            case R.id.action_camera:
                UserEvents.log(
                        Contract.UserEvents.IMG_PHOTO,
                        word.getId(),
                        word.getServerId(),
                        0, 0, null
                );
                dispatchTakePictureIntent(CAMERA_REQUEST);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void dispatchTakePictureIntent(int actionCode) {
        try {
            File f = createImageFile();
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            startActivityForResult(takePictureIntent, actionCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";

        // Create the directory if it doesn't exist
        new File(Environment.getExternalStorageDirectory() + "/VocabNomad/" + UserManager.getUserId()).mkdirs();

        File image = File.createTempFile(
                imageFileName,
                ".JPG",
                new File(Environment.getExternalStorageDirectory() + "/VocabNomad/" + UserManager.getUserId())
        );

        mCurrentPhotoPath = UserManager.getUserId() + "/" + image.getName();
        return image;
    }

    private void add_tag(final String name) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(
                    getApplicationContext(),
                    "Tag cannot be blank",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        final ViewGroup newView = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.list_item_tag, mContainerView, false);

        ((TextView) newView.findViewById(android.R.id.text1)).setText(name);

        newView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContainerView.removeView(newView);
                UserEvents.log(Contract.UserEvents.DELETE_TAGS,
                        word != null ? word.getId() : 0,
                        word != null ? word.getServerId(): 0,
                        0, 0,
                        (String) ((TextView) newView.findViewById(android.R.id.text1)).getText()
                );

            }
        });

        mContainerView.addView(newView, 0);
    }


    private void clearFocus() {
        sentence.clearFocus();
        definition.clearFocus();
        entry.clearFocus();
    }
}

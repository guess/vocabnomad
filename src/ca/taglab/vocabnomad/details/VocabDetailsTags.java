package ca.taglab.vocabnomad.details;



import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.types.Tag;
import ca.taglab.vocabnomad.types.Word;
import ca.taglab.vocabnomad.types.WordTag;


public class VocabDetailsTags extends Fragment {
    public static final String TAG = "VocabDetailsTags";

    private static final String WORD_ID = "word_id";
    private static final String EDIT = "edit";

    private long mWordId;
    private Uri mWordUri;
    private boolean isEdit;

    private View mLayout;

    private VocabDetailsListener mListener;

    public static VocabDetailsTags newInstance(long wordId, boolean isEdit) {
        VocabDetailsTags fragment = new VocabDetailsTags();
        Bundle args = new Bundle();
        args.putLong(WORD_ID, wordId);
        args.putBoolean(EDIT, isEdit);
        fragment.setArguments(args);
        return fragment;
    }

    public VocabDetailsTags() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWordId = getArguments().getLong(WORD_ID);
            mWordUri = ContentUris.withAppendedId(Contract.Word.getUri(), mWordId);
            isEdit = getArguments().getBoolean(EDIT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.details_tags, container, false);
        if (mLayout != null) {
            if (isEdit) {
                mLayout.findViewById(R.id.tags).setVisibility(View.GONE);
                mLayout.findViewById(R.id.edit).setVisibility(View.VISIBLE);
                mLayout.findViewById(R.id.option_write).setBackgroundColor(Color.parseColor("#3049E20E"));
                initAddTag();
            } else {
                mLayout.findViewById(R.id.tags).setVisibility(View.VISIBLE);
                mLayout.findViewById(R.id.edit).setVisibility(View.GONE);
            }

            mLayout.findViewById(R.id.option_write).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onEditPressed();
                }
            });

            new GetTags().execute();
        }
        return mLayout;
    }

    private void onTagClicked(String name) {
        if (mListener != null) {
            mListener.onTagPressed(name);
        }
    }

    private void onEditPressed() {
        if (mListener != null) {
            mListener.onEditPressed(VocabDetailsListener.TAGS, isEdit);
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

    private void initAddTag() {
        mLayout.findViewById(R.id.add_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text_box = (EditText) mLayout.findViewById(R.id.new_tag);
                if (text_box.getText() != null) {
                    String name = text_box.getText().toString();
                    if (name != null && !TextUtils.isEmpty(name.trim())) {
                        new AddTag(text_box.getText().toString()).execute();
                    }
                    text_box.setText("");
                }
            }
        });
    }

    private ViewGroup getEditItem(final String name, final long id) {
        if (name == null || TextUtils.isEmpty(name.trim())) return null;

        final ViewGroup list = (ViewGroup) mLayout.findViewById(R.id.container);
        final ViewGroup tag = (ViewGroup) LayoutInflater.from(getActivity())
                .inflate(R.layout.list_item_tag, list, false);
        if (tag != null) {
            ((TextView) tag.findViewById(R.id.text)).setText(name);
            tag.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    list.removeView(tag);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // Delete the tag from the word
                            Uri word_tags_uri = Uri.withAppendedPath(mWordUri, "tags");
                            if (word_tags_uri != null) {
                                Uri uri = ContentUris.withAppendedId(word_tags_uri, id);
                                if (uri != null) {
                                    getActivity().getContentResolver().delete(uri, null, null);
                                }
                            }
                        }
                    }).start();
                }
            });
        }
        return tag;
    }

    private View getViewItem(final String name) {
        if (name == null || TextUtils.isEmpty(name.trim())) return null;

        final ViewGroup list = (ViewGroup) mLayout.findViewById(R.id.tags);
        final TextView tag = (TextView) LayoutInflater.from(getActivity())
                .inflate(R.layout.tag, list, false);
        if (tag != null) {
            tag.setTextSize(18);
            tag.setText(name);
            tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onTagClicked(name);
                }
            });
        }
        return tag;
    }

    class AddTag extends AsyncTask<Void, Void, View> {
        private String name;

        AddTag(String name) {
            this.name = name;
        }

        @Override
        protected View doInBackground(Void... voids) {
            long tag_id;

            // Check if the tag already exists
            tag_id = getTag(name);

            // If the tag does not exist, insert it
            if (tag_id < 0) tag_id = insertTag(name);

            // If the tag still doesn't exist, you're screwed
            if (tag_id < 0) {
                Log.e(TAG, "Error: Tag \'" + name + "\' was not added");
                return null;
            }

            // Check if the vocab-tag pair already exists and is not deleted
            // If not, create the view and return it
            if (wordTagExists(mWordId, tag_id)) return null;

            if (updateWordTag(mWordId, tag_id) <= 0) {
                // The vocab-tag pair does not exist in the database, insert one
                insertWordTag(mWordId, tag_id);
            }

            return getEditItem(name, tag_id);
        }

        @Override
        protected void onPostExecute(View view) {
            if (view != null) {
                ((ViewGroup) mLayout.findViewById(R.id.container)).addView(view, 0);
            }
        }

        private long getTag(String name) {
            long id = -1;

            Cursor cursor = getActivity().getContentResolver()
                    .query(
                            Contract.Tag.getUri(),
                            Contract.Tag.PROJECTION,
                            Contract.Tag.NAME + "=?",
                            new String[] { name },
                            null, null
                    );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    id = cursor.getLong(cursor.getColumnIndex(Contract.Tag._ID));
                }
                cursor.close();
            }

            return id;
        }

        private long insertTag(String name) {
            ContentValues values = new ContentValues();
            values.put(Contract.Tag.NAME, name);
            values.put(Contract.Tag.USER_ID, UserManager.getUserId());
            values.put(Contract.Tag.SERVER_ID, 0);
            values.put(Contract.Tag.DELETED, 0);
            values.put(Contract.Tag.DATE_MODIFIED, "/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
            Uri uri = getActivity().getContentResolver().insert(Contract.Tag.getUri(), values);
            return (uri != null ? ContentUris.parseId(uri) : -1);
        }

        private boolean wordTagExists(long word, long tag) {
            boolean exists = false;

            Cursor cursor = getActivity().getContentResolver().query(
                    Contract.WordTag.getUri(),
                    Contract.WordTag.PROJECTION,
                    Contract.WordTag.WORD_ID + "=? AND " + Contract.WordTag.TAG_ID + "=? AND " +
                    Contract.WordTag.DELETED + "=? AND " + Contract.WordTag.USER_ID + "=?",
                    new String[] { Long.toString(word), Long.toString(tag), Long.toString(0),
                    Long.toString(UserManager.getUserId()) },
                    null, null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    exists = true;
                }
                cursor.close();
            }

            return exists;
        }

        private void insertWordTag(long word, long tag) {
            ContentValues values = new ContentValues();
            values.put(Contract.WordTag.USER_ID, UserManager.getUserId());
            values.put(Contract.WordTag.WORD_ID, word);
            values.put(Contract.WordTag.WORD_SID, getWordServerId(word));
            values.put(Contract.WordTag.TAG_ID, tag);
            values.put(Contract.WordTag.TAG_SID, getTagServerId(tag));
            values.put(Contract.WordTag.DATE_MODIFIED, "/Date(" + Long.toString(new Date().getTime()) + "-0500)/");

            getActivity().getContentResolver().insert(Contract.WordTag.getUri(), values);
        }

        private int updateWordTag(long word, long tag) {
            ContentValues values = new ContentValues();
            values.put(Contract.WordTag.DELETED, 0);
            return getActivity().getContentResolver().update(
                    Contract.WordTag.getUri(),
                    values,
                    Contract.WordTag.WORD_ID + "=? AND " + Contract.WordTag.TAG_ID + "=?",
                    new String[] { Long.toString(word), Long.toString(tag) }
            );
        }

        private long getTagServerId(long tag) {
            long server_id = 0;

            Cursor cursor = getActivity().getContentResolver().query(
                    Contract.Tag.getUri(),
                    Contract.Tag.PROJECTION,
                    Contract.Tag._ID + "=?",
                    new String[] { Long.toString(tag) },
                    null, null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    server_id = cursor.getLong(cursor.getColumnIndex(Contract.Tag.SERVER_ID));
                }
                cursor.close();
            }

            return server_id;
        }

        private long getWordServerId(long word) {
            long server_id = 0;

            Cursor cursor = getActivity().getContentResolver().query(
                    Contract.Word.getUri(),
                    Contract.Word.PROJECTION,
                    Contract.Word._ID + "=?",
                    new String[] { Long.toString(word) },
                    null, null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    server_id = cursor.getLong(cursor.getColumnIndex(Contract.Word.SERVER_ID));
                }
                cursor.close();
            }

            return server_id;
        }

    }

    class GetTags extends AsyncTask<Void, Void, ArrayList<View>> {

        @Override
        protected ArrayList<View> doInBackground(Void... voids) {
            ArrayList<View> views = new ArrayList<View>();

            Uri uri = Uri.withAppendedPath(mWordUri, "tags");
            if (uri == null) return views;
            Log.d(TAG, "Tags URI: " + uri);

            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) return views;
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex(Contract.View.NAME));
                long id = cursor.getLong(cursor.getColumnIndex(Contract.View._ID));
                View item = (isEdit ? getEditItem(name, id) : getViewItem(name));
                if (item != null) views.add(item);
                cursor.moveToNext();
            }

            cursor.close();
            return views;
        }

        @Override
        protected void onPostExecute(ArrayList<View> views) {
            ViewGroup list = (ViewGroup) (
                    isEdit?
                    mLayout.findViewById(R.id.container) :
                    mLayout.findViewById(R.id.tags)
            );

            for (View view : views) {
                list.addView(view, 0);
            }

            if (!isEdit && views.isEmpty()) onEditPressed();
        }
    }

}

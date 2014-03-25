package ca.taglab.vocabnomad.adapter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.ViewWordActivity;
import ca.taglab.vocabnomad.VocabActivity;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.db.UserEvents;
import ca.taglab.vocabnomad.types.Word;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class VocabAdapter extends CursorAdapter {

    private LruCache<Long, Bitmap> mMemoryCache;
    private Handler mFilterHandler;
    private Context mContext;

    static class ViewHolder {
        TextView word;
        TextView sentence;
        LinearLayout tags;
        LinearLayout synonyms;
        TextView synTitle;
        ImageView photo;
        long id;
        long sid;
    }

    public VocabAdapter(Context context, Cursor cursor, int flags, Handler handler) {
        super(context, cursor, flags);
        initMemoryCache();
        this.mFilterHandler = handler;
        this.mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.list_item_word, parent, false);

        holder.word = (TextView) v.findViewById(R.id.word);
        holder.sentence = (TextView) v.findViewById(R.id.sentence);
        holder.tags = (LinearLayout) v.findViewById(R.id.tags);
        holder.photo = (ImageView) v.findViewById(R.id.image);
        holder.synTitle = (TextView) v.findViewById(R.id.title);
        holder.synonyms = (LinearLayout) v.findViewById(R.id.synonyms);

        v.setTag(holder);
        bindView(v, context, cursor);

        return v;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        Word word = new Word(cursor);
        ViewHolder holder = (ViewHolder) view.getTag();

        // Reset synonyms
        holder.synTitle.setVisibility(View.GONE);
        //holder.synonyms.removeAllViewsInLayout();
        holder.synonyms.setVisibility(View.GONE);

        // Log that the word has been seen in the vocabulary list
        UserEvents.log(Contract.UserEvents.VIEW_VOC, word.getId(), word.getServerId(), 0, 0, word.getWord());

        holder.id = word.getId();
        holder.sid = word.getServerId();
        holder.word.setText(word.getWord());

        if (!TextUtils.isEmpty(word.getSentence())) {
            holder.sentence.setText(word.getSentence());
        } else {
            holder.sentence.setText("");
        }

        final Bitmap bitmap = getBitmapFromMemCache(word.getId());
        if (bitmap != null) {
            holder.photo.setImageBitmap(bitmap);
        } else {
            String image = word.getImageFilePath();
            holder.photo.setImageResource(R.drawable.image_placeholder_normal);
            if (image != null) {
                File f = new File(Environment.getExternalStorageDirectory() + "/VocabNomad", word.getImageFilePath());
                new LoadImage(word.getId(), f).execute(holder);
            }
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewWordActivity.class);
                intent.putExtra("id", ((ViewHolder) v.getTag()).id);
                ((Activity) context).startActivityForResult(intent, VocabActivity.VIEW_WORD);
            }
        });

        holder.tags.removeAllViewsInLayout();
        new LoadTags(context, word.getId()).execute(holder);

        new LoadSynonyms(context, word.getId()).execute(holder);
    }


    /**
     * Load the image for a word in the background.
     */
    class LoadImage extends AsyncTask<ViewHolder, Void, Bitmap> {
        ViewHolder holder;
        File file;
        long id;

        LoadImage(long id, File file) {
            this.id = id;
            this.file = file;
        }

        @Override
        protected Bitmap doInBackground(ViewHolder... params) {
            holder = params[0];
            return getImage(file, 100);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (holder.id == this.id && bitmap != null) {
                addBitmapToMemCache(holder.id, bitmap);
                holder.photo.setImageBitmap(bitmap);
            }
        }
    }


    /**
     * Load the synonyms for a word in the background.
     */
    class LoadSynonyms extends AsyncTask<ViewHolder, Void, Cursor> {
        ViewHolder holder;
        Context context;
        long id;

        LoadSynonyms(Context context, long id) {
            this.context = context;
            this.id = id;
        }

        @Override
        protected Cursor doInBackground(ViewHolder... params) {
            boolean viewed = false;
            Cursor synonyms, names = null;
            holder = params[0];

            // Get the synonyms for this word
            Uri.Builder builder = Contract.Word.getUri().buildUpon();
            ContentUris.appendId(builder, holder.sid);
            builder.appendEncodedPath(Contract.Synonyms.TABLE);
            Uri synonymsUri = builder.build();
            synonyms = mContext.getContentResolver().query(
                    synonymsUri,
                    Contract.Synonyms.PROJECTION,
                    null,
                    null,
                    null
            );

            if (synonyms != null && synonyms.moveToFirst()) {
                // There are synonyms for this word

                // Check to see if the word has been viewed at least 4 times
                if (hasBeenViewed(holder.id, 4)) {
                    viewed = true;
                }

                // Check to see if any of its synonyms have been viewed at least 4 times
                while (!synonyms.isAfterLast() && !viewed) {
                    if (hasBeenViewed(synonyms.getLong(synonyms.getColumnIndex(Contract.Synonyms.ENTRY_SYN_SID)), 4)) {
                        viewed = true;
                    }
                    synonyms.moveToNext();
                }

                if (viewed) {
                    synonyms.moveToFirst();
                    // Get a new cursor that contains the names of synonyms
                    String selection = "";
                    while (!synonyms.isAfterLast()) {
                        if (!TextUtils.isEmpty(selection)) selection += " OR ";
                        selection += Contract.Word.SERVER_ID + "="
                                + synonyms.getLong(synonyms.getColumnIndex(Contract.Synonyms.ENTRY_SYN_SID));
                        synonyms.moveToNext();
                    }

                    if (!TextUtils.isEmpty(selection)) {
                        names = context.getContentResolver().query(
                                Contract.Word.getUri(),
                                Contract.Word.PROJECTION,
                                selection,
                                null,
                                null
                        );
                    }

                    synonyms.close();
                    return names;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);

            if (holder.id == this.id && cursor != null && cursor.moveToFirst()) {
                Log.i("VocabAdapter", "Display synonyms for " + holder.word.getText().toString());
                holder.synTitle.setVisibility(View.VISIBLE);
                holder.synonyms.removeAllViewsInLayout();
                holder.synonyms.setVisibility(View.VISIBLE);

                while (!cursor.isAfterLast()) {

                    final ViewGroup newView = (ViewGroup) LayoutInflater.from(context).inflate(
                            R.layout.list_item_synonym,
                            holder.synonyms,
                            false
                    );

                    // Inflate the view
                    ViewHolder synHolder = new ViewHolder();
                    synHolder.word = (TextView) newView.findViewById(R.id.word);
                    synHolder.photo = (ImageView) newView.findViewById(R.id.image);
                    newView.setBackgroundResource(R.drawable.list_item_bg);

                    // Add information to the view
                    synHolder.word.setText(cursor.getString(cursor.getColumnIndex(Contract.Word.ENTRY)));
                    synHolder.id = cursor.getLong(cursor.getColumnIndex(Contract.Word._ID));
                    synHolder.sid = cursor.getLong(cursor.getColumnIndex(Contract.Word.SERVER_ID));
                    newView.setTag(synHolder);

                    // Load the synonym's image
                    final Bitmap bitmap = getBitmapFromMemCache(synHolder.id);
                    if (bitmap != null) {
                        synHolder.photo.setImageBitmap(bitmap);
                    } else {
                        String image = cursor.getString(cursor.getColumnIndex(Contract.Word.IMG));
                        synHolder.photo.setImageResource(R.drawable.image_placeholder_normal);
                        if (image != null) {
                            File f = new File(Environment.getExternalStorageDirectory() + "/VocabNomad", image);
                            new LoadImage(synHolder.id, f).execute(synHolder);
                        }
                    }

                    // Log that a synonym is being visible
                    UserEvents.log(
                            Contract.UserEvents.REC_SYN,
                            synHolder.id,
                            synHolder.sid,
                            0, 0,
                            synHolder.word.getText().toString()
                    );

                    //((TextView) newView.findViewById(R.id.word)).setText(cursor.getString(cursor.getColumnIndex(Contract.Word.ENTRY)));
                    //newView.setBackgroundResource(R.drawable.list_item_bg);
                    //newView.setTag(cursor.getLong(cursor.getColumnIndex(Contract.Word._ID)));

                    newView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ViewHolder synHolder = (ViewHolder) v.getTag();
                            UserEvents.log(
                                    Contract.UserEvents.VIEW_SYN,
                                    synHolder.id,
                                    synHolder.sid,
                                    0, 0,
                                    synHolder.word.getText().toString()
                            );
                            Intent intent = new Intent(context, ViewWordActivity.class);
                            intent.putExtra("id", synHolder.id);
                            ((Activity) context).startActivityForResult(intent, VocabActivity.VIEW_WORD);
                        }
                    });

                    holder.synonyms.addView(newView, 0);

                    cursor.moveToNext();
                }
            }
        }
    }


    /**
     * Return true if the word with ID "wordID" has been viewed at least "min" times.
     *
     * @param wordId    Word ID
     * @param min       Minimum times viewed
     * @return          True if the word has been viewed at least min times, False otherwise.
     */
    private boolean hasBeenViewed(long wordId, int min) {
        Cursor cursor;
        String selection;

        selection = "("
                + Contract.UserEvents.TYPE + "=" + UserEvents.mUserEventIds.get(Contract.UserEvents.VIEW_DETAILED_VOC)
                + " OR "
                + Contract.UserEvents.TYPE + "=" + UserEvents.mUserEventIds.get(Contract.UserEvents.VIEW_VOC)
                + ") AND " +
                Contract.UserEvents.VOCAB_ID + "=" + wordId;

        cursor = DatabaseHelper.getInstance(mContext)
                .query(Contract.UserEvents.TABLE, new String[]{"COUNT(*) AS _COUNT"},
                        selection, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getInt(0) >= min) {
                cursor.close();
                return true;
            }
            cursor.close();
        }

        return false;
    }


    /**
     * Load the tags for a word in the background.
     */
    class LoadTags extends AsyncTask<ViewHolder, Void, Cursor> {
        ViewHolder holder;
        long id;
        Context context;

        LoadTags(Context context, long id) {
            this.context = context;
            this.id = id;
        }

        @Override
        protected Cursor doInBackground(ViewHolder... params) {
            holder = params[0];
            Uri.Builder builder = ContentUris.withAppendedId(
                    Contract.Word.getUri(),
                    this.id
            ).buildUpon();

            builder.appendEncodedPath(Contract.Tag.TABLE);

            return context.getContentResolver().query(
                    builder.build(),
                    Contract.Tag.PROJECTION,
                    null, null, null
            );
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            if (holder.id == this.id) {
                holder.tags.removeAllViewsInLayout();
                if (cursor != null) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        final TextView newView = (TextView) LayoutInflater.from(context).inflate(
                                R.layout.tag, holder.tags, false);
                        newView.setText(cursor.getString(cursor.getColumnIndex(Contract.Tag.NAME)));
                        newView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Message msg = new Message();
                                msg.obj = ((TextView) v).getText().toString();
                                mFilterHandler.dispatchMessage(msg);
                            }
                        });
                        holder.tags.addView(newView, 0);
                        cursor.moveToNext();
                    }
                    cursor.close();
                }
            } else {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }



    /**
     * Return a Bitmap representation of the given File.
     *
     * @param f             Image file
     * @param max_size      Maximum size of the Bitmap
     *
     * @return              Bitmap representation of the File
     */
    public static Bitmap getImage(File f, int max_size) {
        //File f = new File(Environment.getExternalStorageDirectory() + "/VocabNomad", path);
        Bitmap b = null;

        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > max_size || o.outWidth > max_size) {
                scale = (int)Math.pow(2, (int) Math.round(Math.log(max_size /
                        (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return b;
    }


    /**
     * Initialize the LRU cache for images.
     */
    private void initMemoryCache() {
        // Get max available VM memory, exceeding this amount will throw an OutOfMemory exception.
        // Stored in kilobytes as LruCache takes an int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void addBitmapToMemCache(Long key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(Long key) {
        return mMemoryCache.get(key);
    }

}

package ca.taglab.vocabnomad.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.widget.CursorAdapter;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.widgets.ImageOpt;
import ca.taglab.vocabnomad.widgets.TypefacedTextView;


public class CompletedGoalsAdapter extends CursorAdapter {

    private LruCache<String, ArrayList<Bitmap>> mMemoryCache;
    private Context mContext;

    static class ViewHolder {
        TypefacedTextView name;
        TypefacedTextView level;
        TypefacedTextView expires;
        ImageView image1, image2, image3, image4;
    }

    public CompletedGoalsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        initMemoryCache();
        this.mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.olm_goal_complete_list_item, parent, false);

        if (v != null) {
            holder.name = (TypefacedTextView) v.findViewById(R.id.title);
            holder.level = (TypefacedTextView) v.findViewById(R.id.level);
            holder.expires = (TypefacedTextView) v.findViewById(R.id.expires);
            holder.image1 = (ImageView) v.findViewById(R.id.image1);
            holder.image2 = (ImageView) v.findViewById(R.id.image2);
            holder.image3 = (ImageView) v.findViewById(R.id.image3);
            holder.image4 = (ImageView) v.findViewById(R.id.image4);

            v.setTag(holder);
            bindView(v, context, cursor);
        }

        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String goal = cursor.getString(cursor.getColumnIndex(Contract.Goals.GOAL_NAME));
        holder.name.setText(goal);
        holder.level.setText(cursor.getString(cursor.getColumnIndex(Contract.Goals.LEVEL)));

        // Reset all images
        holder.image1.setImageResource(R.drawable.image_placeholder_normal);
        holder.image2.setImageResource(R.drawable.image_placeholder_normal);
        holder.image3.setImageResource(R.drawable.image_placeholder_normal);
        holder.image4.setImageResource(R.drawable.image_placeholder_normal);

        final ArrayList<Bitmap> bitmaps = getBitmapFromMemCache(goal);
        if (bitmaps != null && !bitmaps.isEmpty()) {
            setBitmaps(holder, bitmaps);
        } else {
            new LoadImages(goal).execute(holder);
        }
    }

    private void setBitmaps(ViewHolder holder, ArrayList<Bitmap> bitmaps) {
        if (bitmaps != null && !bitmaps.isEmpty()) {
            switch (bitmaps.size()) {
                case 4:
                    holder.image1.setImageBitmap(bitmaps.get(3));
                case 3:
                    holder.image2.setImageBitmap(bitmaps.get(2));
                case 2:
                    holder.image3.setImageBitmap(bitmaps.get(1));
                case 1:
                    holder.image4.setImageBitmap(bitmaps.get(0));
            }
        }
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

        mMemoryCache = new LruCache<String, ArrayList<Bitmap>>(cacheSize) {
            @Override
            protected int sizeOf(String key, ArrayList<Bitmap> bitmaps) {
                // The cache size will be measured in kilobytes rather than number of items.
                int size = 0;
                for (Bitmap bitmap : bitmaps) size += bitmap.getByteCount();
                return size / 1024;
            }
        };
    }

    private void addBitmapToMemCache(String key, ArrayList<Bitmap> bitmaps) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmaps);
        }
    }

    private ArrayList<Bitmap> getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }


    class LoadImages extends AsyncTask<ViewHolder, Void, ArrayList<Bitmap>> {
        private ViewHolder mHolder;
        private String mTitle;

        LoadImages(String name) {
            this.mTitle = name;
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(ViewHolder... holders) {
            ArrayList<String> paths = getFilePaths(mTitle);
            mHolder = holders[0];

            ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
            for (String path : paths) {
                String prefix = Environment.getExternalStorageDirectory() + "/VocabNomad/";
                bitmaps.add(ImageOpt.decodeSampledBitmapFromFile(prefix + path, 50, 50));
            }

            return bitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
            if (bitmaps == null || bitmaps.isEmpty()) return;
            if (mHolder.name.getText() != null && mHolder.name.getText().equals(mTitle)) {
                addBitmapToMemCache(mTitle, bitmaps);
                setBitmaps(mHolder, bitmaps);
            }
        }

        /**
         * Get an array of file paths for vocabulary associated with tag 'name'
         * @param name  Name of the tag to get vocabulary images for
         * @return      Bitmap full of file paths for vocabulary associated with the 'name'
         */
        public ArrayList<String> getFilePaths(String name) {
            ArrayList<String> paths = new ArrayList<String>();
            DatabaseHelper db = DatabaseHelper.getInstance(mContext);

            Cursor cursor = db.query(
                    Contract.View.TABLE,
                    new String[] { Contract.View.IMG },
                    Contract.View.NAME + "=? COLLATE NOCASE AND " + Contract.View.IMG + " IS NOT NULL LIMIT 4",
                    new String[] { name },
                    null, null, null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        paths.add(cursor.getString(cursor.getColumnIndex(Contract.View.IMG)));
                        cursor.moveToNext();
                    }
                }
                cursor.close();
            }

            return paths;
        }
    }
}

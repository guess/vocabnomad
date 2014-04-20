package ca.taglab.vocabnomad;

import android.app.ActionBar;
import android.app.ExpandableListActivity;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ca.taglab.vocabnomad.adapter.VocabListAdapter;
import ca.taglab.vocabnomad.auth.LanguageActivity;
import ca.taglab.vocabnomad.auth.LoginActivity;
import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.db.UserEvents;
import ca.taglab.vocabnomad.details.VocabDetailsActivity;
import ca.taglab.vocabnomad.rest.DataSyncRestService;
import ca.taglab.vocabnomad.rest.RestService;
import ca.taglab.vocabnomad.types.Word;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class VocabListActivity extends ExpandableListActivity implements ExpandableListView.OnGroupClickListener {
    public static final String TAG = "VocabListActivity";

    // Activity request codes:
    private static final int USER_LOGIN = 1;
    private static final int SEARCH = 2;

    // Query handler:
    public static final int TOKEN_WORD = 0;
    public static final int TOKEN_SYN = 1;

    View progressBar;

    static class ViewHolder {
        TextView word;
        TextView sentence;
        LinearLayout tags;
        ImageView photo;
        long id;
    }

    private class VocabAdapter extends VocabListAdapter {

        public VocabAdapter(Context context, int groupLayout, int childLayout,
                                String[] groupFrom, int[] groupTo, String[] childrenFrom,int[] childrenTo) {
            super(context, groupLayout, childLayout, groupFrom, groupTo, childrenFrom, childrenTo);
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View v = convertView;
            Word word = new Word(getChild(groupPosition, childPosition));

            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.list_item_synonym, null);
                holder = new ViewHolder();
                holder.word = (TextView) v.findViewById(R.id.word);
                holder.photo = (ImageView) v.findViewById(R.id.image);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            v.findViewById(R.id.title).setVisibility(View.GONE);

            if (childPosition == 0) {
                v.findViewById(R.id.title).setVisibility(View.VISIBLE);
            }

            holder.word.setText(word.getWord());

            if (!TextUtils.isEmpty(word.getImageFilePath())) {
                File f = new File(Environment.getExternalStorageDirectory() + "/VocabNomad", word.getImageFilePath());
                holder.photo.setImageBitmap(getImage(f, 100));
            } else {
                holder.photo.setImageResource(R.drawable.image_placeholder_normal);
            }

            return v;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View v = convertView;
            Word word = new Word(getGroup(groupPosition));

            // Log that the word has been seen in the vocabulary list
            UserEvents.log(Contract.UserEvents.VIEW_VOC, word.getId(), word.getServerId(), 0, 0, word.getWord());

            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.list_item_word, null);
                holder = new ViewHolder();
                holder.word = (TextView) v.findViewById(R.id.word);
                holder.sentence = (TextView) v.findViewById(R.id.sentence);
                holder.photo = (ImageView) v.findViewById(R.id.image);
                holder.tags = (LinearLayout) v.findViewById(R.id.tags);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            holder.id = getGroupId(groupPosition);
            holder.word.setText(word.getWord());

            if (!TextUtils.isEmpty(word.getSentence())) {
                holder.sentence.setText(word.getSentence());
            } else {
                holder.sentence.setText("");
            }

            String image = word.getImageFilePath();
            holder.photo.setImageResource(R.drawable.image_placeholder_normal);
            if (image != null) {
                File f = new File(Environment.getExternalStorageDirectory() + "/VocabNomad", word.getImageFilePath());
                new LoadImage(word.getId(), f).execute(holder);
            }

            holder.tags.removeAllViewsInLayout();
            new LoadTags(getGroupId(groupPosition)).execute(holder);

            return v;
        }

        @Override
        public void setGroupCursor(Cursor cursor) {
            super.setGroupCursor(cursor);

            // Expand all group of words
            // Any words that have not been viewed min times will not have any children
            //for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            //    getExpandableListView().expandGroup(i);
            //}
        }
    }

    private static final class QueryHandler extends AsyncQueryHandler {
        private CursorTreeAdapter mAdapter;

        public QueryHandler(Context context, CursorTreeAdapter adapter) {
            super(context.getContentResolver());
            this.mAdapter = adapter;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, final Cursor cursor) {
            switch (token) {
                case TOKEN_WORD:
                    mAdapter.setGroupCursor(cursor);
                    mAdapter.notifyDataSetChanged();
                    break;

                case TOKEN_SYN:
                    int groupPosition = (Integer) cookie;
                    mAdapter.setChildrenCursor(groupPosition, cursor);
                    break;
            }
        }
    }

    class LoadTags extends AsyncTask<ViewHolder, Void, Cursor> {
        ViewHolder holder;
        long id;

        LoadTags(long id) {
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

            return getContentResolver().query(
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
                        final TextView newView = (TextView) LayoutInflater.from(getApplicationContext()).inflate(
                                R.layout.tag, holder.tags, false);
                        newView.setText(cursor.getString(cursor.getColumnIndex(Contract.Tag.NAME)));
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
                holder.photo.setImageBitmap(bitmap);
            }
        }
    }


    public QueryHandler mQueryHandler;
    private CursorTreeAdapter mAdapter;
    private String mFilter;

    Handler syncFinishedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case DataSyncRestService.VOCAB:
                    Log.i(TAG, "Words finished syncing");
                    break;
                case DataSyncRestService.TAGS:
                    Log.i(TAG, "Tags finished syncing");
                    break;
                case DataSyncRestService.VTPAIRS:
                    Log.i(TAG, "VTPairs finished syncing");
                    break;
            }

            if (msg.what == DataSyncRestService.VTPAIRS) {
                UserEvents.log(
                        Contract.UserEvents.DATA_SYNC,
                        0, 0, 0, 0, null
                );

                if (!TextUtils.isEmpty(mFilter)) {
                    mQueryHandler.startQuery(TOKEN_WORD, null, Contract.Word.getUri(),
                            Contract.View.WORD, mFilter, null, Contract.View.ENTRY + " COLLATE NOCASE");
                }

                progressBar.setVisibility(View.GONE);
                getExpandableListView().setVisibility(View.VISIBLE);
            }
        }
    };

    Handler jitFinishedHandler = new Handler() {

        public void handleMessage(Message msg) {
            new DataSyncRestService.Refresh(getApplicationContext(), DataSyncRestService.VOCAB)
                    .run(syncFinishedHandler);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.word_list);

        progressBar = findViewById(R.id.progress);

        checkUserLogin();

        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.search_bg));
        actionBar.setIcon(R.drawable.launch_icon);
        actionBar.setTitle("");

        // Set up our adapter
        mAdapter = new VocabAdapter(
                this,
                R.layout.list_item_word,
                android.R.layout.simple_expandable_list_item_1,
                new String[] { Contract.View.ENTRY },
                new int[] { R.id.word },
                new String[] { Contract.View.ENTRY },
                new int[] { android.R.id.text1 }
        );

        setListAdapter(mAdapter);

        mQueryHandler = new QueryHandler(this, mAdapter);

        getExpandableListView().setOnGroupClickListener(this);

        //mQueryHandler.startQuery(TOKEN_WORD, null, Contract.Word.getUri(), Contract.View.WORD,
        //        null, null, Contract.View.ENTRY + " COLLATE NOCASE");

        mAdapter.setGroupCursor(null);
    }

    private void checkUserLogin() {
        try {
            DatabaseHelper.getInstance(getApplicationContext()).open();
        } catch(Exception e) {
            Log.e(TAG, "An error occurred when opening the VocabNomad database");
        }

        UserManager.login(getApplicationContext());

        if (!UserManager.isLoggedIn()) {
            startActivityForResult(new Intent(this, LoginActivity.class), USER_LOGIN);
        } else {
            if (UserManager.getMotherTongue() <= 0) {
                startActivity(new Intent(this, LanguageActivity.class));
            } else {
                progressBar.setVisibility(View.VISIBLE);
                getExpandableListView().setVisibility(View.GONE);
                new DataSyncRestService.Refresh(getApplicationContext(), DataSyncRestService.VOCAB)
                        .run(syncFinishedHandler);
            }
        }
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        UserEvents.log(Contract.UserEvents.VIEW_VOC, 0, 0, 0, 0, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode) {

            case USER_LOGIN:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(
                            getApplication(),
                            "Welcome to VocabNomad " + UserManager.getUsername(),
                            Toast.LENGTH_SHORT
                    ).show();
                    progressBar.setVisibility(View.VISIBLE);
                    getExpandableListView().setVisibility(View.GONE);
                    new DataSyncRestService.Refresh(getApplicationContext(), DataSyncRestService.VOCAB)
                            .run(syncFinishedHandler);
                }
                if (resultCode == RESULT_CANCELED) {
                    /* Close the application */
                    finish();
                }
                break;


            case SEARCH:
                if (resultCode == RESULT_OK) {
                    String tag = data.getStringExtra("tag");

                    if (data.getBooleanExtra("jit", false)) {
                        Log.i(TAG, "Executing JIT: " + tag);
                        progressBar.setVisibility(View.VISIBLE);
                        getExpandableListView().setVisibility(View.GONE);

                        RestService service = new RestService(
                                jitFinishedHandler,
                                getApplicationContext(),
                                Contract.JIT_URL
                                        + "/userid=" + UserManager.getUserId()
                                        + "/word=" + tag,
                                RestService.POST
                        );
                        service.execute();
                    }

                    LinearLayout filter = (LinearLayout) findViewById(R.id.filter);
                    findViewById(R.id.bar).setVisibility(View.VISIBLE);

                    final TextView newView = (TextView) LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.tag_search, filter, false);
                    newView.setText(tag);
                    filter.addView(newView, 0);

                    if (!TextUtils.isEmpty(mFilter)) {
                        mFilter += " OR ";
                    } else {
                        mFilter = "";
                    }

                    mFilter += Contract.View.NAME + "=\'" + tag + "\'";

                    mQueryHandler.startQuery(TOKEN_WORD, null, Contract.Word.getUri(),
                            Contract.View.WORD, mFilter, null, Contract.View.ENTRY + " COLLATE NOCASE");
                }

                break;
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        //Intent intent = new Intent(this, ViewWordActivity.class);
        Intent intent = new Intent(this, VocabDetailsActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        //Intent intent = new Intent(this, ViewWordActivity.class);
        Intent intent = new Intent(this, VocabDetailsActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vocab_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_add:
                startActivity(new Intent(this, EditWordActivity.class));
                return true;

            case R.id.menu_search:
                startActivityForResult(new Intent(this, VocabSearch.class), SEARCH);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Null out the group cursor.
        // This will cause the word group cursor and all synonym cursors to be closed.
        mAdapter.changeCursor(null);
        mAdapter = null;
    }

    public void closeFilter(View v) {
        LinearLayout filter = (LinearLayout) findViewById(R.id.filter);
        filter.removeAllViewsInLayout();
        findViewById(R.id.bar).setVisibility(View.GONE);

        invalidateOptionsMenu();

        mFilter = null;
        //mQueryHandler.startQuery(TOKEN_WORD, null, Contract.Word.getUri(),
        //        Contract.View.WORD, mFilter, null, Contract.View.ENTRY + " COLLATE NOCASE");
        mAdapter.setGroupCursor(null);

        progressBar.setVisibility(View.GONE);
        getExpandableListView().setVisibility(View.VISIBLE);
    }
}

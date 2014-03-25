package ca.taglab.vocabnomad.adapter;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.*;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.db.UserEvents;


public class VocabListAdapter extends SimpleCursorTreeAdapter {

    private static final String TAG = "Vocabulary list adapter";
    private Context mContext;

    private QueryHandler mQueryHandler;

    // Query handler:
    public static final int TOKEN_WORD = 0;
    public static final int TOKEN_SYN = 1;

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

    public VocabListAdapter(Context context, int groupLayout, int childLayout,
                               String[] groupFrom, int[] groupTo, String[] childrenFrom,int[] childrenTo) {
        super(context, null, groupLayout, groupFrom, groupTo, childLayout, childrenFrom, childrenTo);
        this.mContext = context;
        this.mQueryHandler = new QueryHandler(mContext, this);
    }

    @Override
    protected Cursor getChildrenCursor(final Cursor groupCursor) {
        // Given the word group, we return a cursor for all the synonyms within that group
        new LoadSynonyms().execute(groupCursor);

        return null;
    }

    class LoadSynonyms extends AsyncTask<Cursor, Void, Void> {

        @Override
        protected Void doInBackground(Cursor... params) {
            Cursor groupCursor = params[0];

            if (!hasBeenViewed(VocabListAdapter.this.getGroupId(groupCursor.getPosition()), 4)) {
                // The word has not been viewed at least 4 times
                return null;
            }

            Uri.Builder builder = Contract.Word.getUri().buildUpon();
            ContentUris.appendId(builder, groupCursor.getLong(groupCursor.getColumnIndex(Contract.View.SERVER_ID)));
            builder.appendEncodedPath(Contract.Synonyms.TABLE);
            Uri synonymsUri = builder.build();

            Log.i(TAG, "URI of found word:" + synonymsUri);

            Cursor cursor = mContext.getContentResolver().query(synonymsUri, Contract.Synonyms.PROJECTION, null, null, null);
            String selection = "";

            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    if (!cursor.isFirst()) selection += " OR ";
                    selection += Contract.View.SERVER_ID + "=" +
                            cursor.getString(cursor.getColumnIndex(Contract.Synonyms.ENTRY_SYN_SID));
                    cursor.moveToNext();
                }

                Log.i(TAG, "Synonym select: " + selection);

                mQueryHandler.startQuery(TOKEN_SYN, groupCursor.getPosition(), Contract.Word.getUri(),
                        Contract.View.WORD, selection, null, Contract.View.ENTRY);
            }

            if (cursor != null) {
                cursor.close();
            }

            return null;
        }
    }

    /**
     * Check to see if the word has been viewed at least min times.
     *
     * @param wordId    ID of the word
     * @param min       Minimum number of times the word should be seen by the user
     *
     * @return  True if the word has been viewed at least 'min' times. False otherwise.
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
}

package ca.taglab.vocabnomad.olm;


import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.db.UserEvents;

public class UserStats {
    public static final String TAG = "UserStats";

    public static final int READING = 1;
    public static final int WRITING = 2;
    public static final int SPEAKING = 3;
    public static final int LISTENING = 4;

    public static final String[] READING_ACTIONS = {
            Contract.UserEvents.VIEW_DETAILED_VOC,
            Contract.UserEvents.HEAR_DEFINITION,
            Contract.UserEvents.HEAR_SENTENCE,
            Contract.UserEvents.HEAR_VOC
    };

    public static final String[] WRITING_ACTIONS = {
            Contract.UserEvents.DEFINITION_EDIT,
            Contract.UserEvents.SENTENCE_EDIT,
            Contract.UserEvents.WORD_EDIT,
            Contract.UserEvents.L1_SEARCH,
            Contract.UserEvents.L2_SEARCH,
            Contract.UserEvents.JIT_VOC_REQUEST
    };

    public static final String[] SPEAKING_ACTIONS = {
            Contract.UserEvents.RECORD_VOICE
    };

    public static final String[] LISTENING_ACTIONS = {
            Contract.UserEvents.HEAR_VOC,
            Contract.UserEvents.HEAR_SENTENCE,
            Contract.UserEvents.HEAR_DEFINITION,
            Contract.UserEvents.PLAY_RECORDING
    };


    /**
     * Return a selection string given a list of user activities.
     * @param actions   List of activities
     * @return          Selection string for the database query
     */
    public static String getSelection(String... actions) {
        String selection = "";

        // Add constraints for each action
        for (int i = 0; i < actions.length; i++) {
            if (i > 0) selection += " OR ";
            selection += Contract.UserEvents.TYPE + "=" + UserEvents.mUserEventIds.get(actions[i]);
        }

        // Make sure it is for the currently logged in user
        return ADD_CONSTRAINT(selection, Contract.UserEvents.USER_ID, UserManager.getUserId());
    }


    public static long getCount(Context context, String selection) {
        long count = 0;

        Cursor cursor = context.getContentResolver().query(
                Contract.UserEvents.CONTENT_URI,
                new String[] { Contract.UserEvents.TYPE, Contract.UserEvents.USER_ID },
                selection,
                null, null
        );

        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }


    /**
     * Return the tags that are most used by the given actions
     * @param context   Activity or application context
     * @param actions   List of activities
     * @return          Tags that are used by the given activities, ordered by most used
     */
    public static Cursor getTags(Context context, String... actions) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        // Get the vocabulary that are associated with the given actions
        String vocabulary =
                "SELECT " + Contract.UserEvents.VOCAB_ID + " " +
                "FROM " +  Contract.UserEvents.TABLE + " " +
                "WHERE " + getSelection(actions);

        // Find the related tags associated with the vocabulary found above
        // Ordered by the tags most used by the given actions
        // Example: Actions=reading. The first tag is the most read category
        String join =
                "SELECT " + Contract.View.NAME + ", COUNT(" + Contract.View.NAME + ") AS tag_count " +
                "FROM " + Contract.View.TABLE + " " +
                "INNER JOIN (" + vocabulary + ") AS vocabulary " +
                "ON vocabulary." + Contract.UserEvents.VOCAB_ID + "=" +
                Contract.View.TABLE + "." + Contract.View.WORD_ID + " " +
                "GROUP BY " + Contract.View.NAME + " " +
                "ORDER BY tag_count DESC";

        return db.rawQuery(join, null);
    }


    /**
     * Return the cursor that contains the words most interacted with within a particular tag.
     * @param context   Activity or application context
     * @param tag       Tag
     * @return          Words most interacted within the tag.
     */
    public static Cursor getFavWords(Context context, String tag) {
        if (TextUtils.isEmpty(tag)) return null;
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        String vocabulary =
                "SELECT " + Contract.View.ENTRY + ", " + Contract.View.TABLE + "." + Contract.View.WORD_ID + " " +
                "FROM " + Contract.View.TABLE + " " +
                "WHERE " + Contract.View.NAME + "=?";

        // TODO: Might have to filter out the vocabulary list viewing???
        String join =
                "SELECT COUNT(vocabulary." + Contract.View.WORD_ID + ") as word_count, " + Contract.View.ENTRY + " " +
                "FROM (" + vocabulary + ") AS vocabulary " +
                "INNER JOIN " + Contract.UserEvents.TABLE + " " +
                "ON vocabulary." + Contract.View.WORD_ID + "=" +
                Contract.UserEvents.TABLE + "." + Contract.UserEvents.VOCAB_ID + " " +
                "GROUP BY vocabulary." + Contract.View.WORD_ID + " " +
                "ORDER BY word_count DESC";

        return db.rawQuery(join, new String[] { tag });
    }

    /**
     * Return a cursor containing the tags that are most related to a particular tag.
     * @param context   Activity or application context
     * @param tag       Tag
     * @return          Tags that are most related to a particular tag
     */
    public static Cursor getRelatedTags(Context context, String tag) {
        if (TextUtils.isEmpty(tag)) return null;
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        String vocabulary =
                "SELECT " + Contract.View.WORD_ID + " " +
                "FROM " + Contract.View.TABLE + " " +
                "WHERE " + Contract.View.NAME + "=?";

        String join =
                "SELECT " + Contract.View.NAME + ", COUNT(" + Contract.View.NAME + ") AS tag_count " +
                "FROM (" + vocabulary + ") AS vocabulary " +
                "INNER JOIN " + Contract.View.TABLE + " " +
                "ON (vocabulary." + Contract.View.WORD_ID + "=" +
                Contract.View.TABLE + "." + Contract.View.WORD_ID + " AND name !=?) " +
                "GROUP BY " + Contract.View.NAME + " " +
                "ORDER BY tag_count DESC";

        return db.rawQuery(join, new String[] { tag, tag });
    }


    /**
     * Add an equality constraint to the where clause.
     *
     * @param where	Existing where clause
     * @param col	Column name
     * @param val	Value
     *
     * @return		The new where clause
     */
    public static String ADD_CONSTRAINT(String where, String col, Object val) {
        return col + "=" + val + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
    }



}

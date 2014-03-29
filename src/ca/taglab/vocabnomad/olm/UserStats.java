package ca.taglab.vocabnomad.olm;


import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.db.UserEvents;

public class UserStats {

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

package ca.taglab.vocabnomad.db;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import ca.taglab.vocabnomad.auth.UserManager;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class UserEvents extends DAO {

    public static final String TAG = "UserEvents";

    public static HashMap<String, Long> mUserEventIds;

    private static Context context;


    /**
     * Initialize the Event Types dictionary with names and IDs.
     * @param context   Application context to access the database
     */
    public static void init(final Context context) {
        UserEvents.context = context;

        new Thread(new Runnable() {

            @Override
            public void run() {
                Cursor cursor;
                DatabaseHelper db = DatabaseHelper.getInstance(context);

                try {
                    db.open();

                    mUserEventIds = new HashMap<String, Long>();

                    cursor = db.query(
                            Contract.EventTypes.TABLE,
                            new String[] { Contract.EventTypes.NAME, Contract.EventTypes.ID },
                            null,
                            null,
                            null,
                            null,
                            null
                    );

                    if (cursor != null && cursor.moveToFirst()) {
                        final int mNamePosition = cursor.getColumnIndex(Contract.EventTypes.NAME);
                        final int mIdPosition = cursor.getColumnIndex(Contract.EventTypes.ID);

                        while (!cursor.isAfterLast()) {
                            Log.d(TAG, "USER_EVENTS[" + cursor.getString(mNamePosition) + "] =" + cursor.getLong(mIdPosition));
                            mUserEventIds.put(cursor.getString(mNamePosition), cursor.getLong(mIdPosition));
                            cursor.moveToNext();
                        }

                        cursor.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    /**
     * Log a particular user event.
     *
     * @param eventName     Name of the user event
     * @param wordID        Word device ID
     * @param wordSID       Word server ID
     * @param tagID         Tag device ID
     * @param tagSID        Tag server ID
     * @param userEntry     Inputted user entry
     */
    public static void log(final String eventName, final long wordID, final long wordSID,
                           final long tagID, final long tagSID, final String userEntry) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                String message;
                DatabaseHelper db = DatabaseHelper.getInstance(UserEvents.context);
                ContentValues values = new ContentValues();

                // INIT HASN'T FINISHED YET
                if (mUserEventIds == null) return;

                values.put(Contract.UserEvents.TYPE, mUserEventIds.get(eventName));
                message = eventName + " @ ";

                values.put(Contract.UserEvents.TIME, "/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
                message += values.getAsString(Contract.UserEvents.TIME);

                values.put(Contract.UserEvents.USER_ID, UserManager.getUserId());
                message += ": UserID=" + values.getAsString(Contract.UserEvents.USER_ID);

                values.put(Contract.UserEvents.USER_ENTRY, userEntry);
                if (!TextUtils.isEmpty(userEntry)) {
                    message += " UserEntry=" + values.getAsString(Contract.UserEvents.USER_ENTRY);
                }

                values.put(Contract.UserEvents.VOCAB_ID, wordID);
                values.put(Contract.UserEvents.VOCAB_SID, wordSID);
                if (wordID > 0) {
                    message += " WordID=" + values.getAsString(Contract.UserEvents.VOCAB_ID);
                    message += " WordSID=" + values.getAsString(Contract.UserEvents.VOCAB_SID);
                }

                values.put(Contract.UserEvents.TAG_ID, tagID);
                values.put(Contract.UserEvents.TAG_SID, tagSID);
                if (tagID > 0) {
                    message += " TagID=" + values.getAsString(Contract.UserEvents.TAG_ID);
                    message += " TagSID=" + values.getAsString(Contract.UserEvents.TAG_SID);
                }

                values.put(Contract.UserEvents.LONG, 0);
                values.put(Contract.UserEvents.LAT, 0);
                values.put(Contract.UserEvents.ALT, 0);
                values.put(Contract.UserEvents.LANG_MAP, 0);
                values.put(Contract.UserEvents.ACTIVITY, 0);
                values.put(Contract.UserEvents.LOCATION, 0);
                values.put(Contract.UserEvents.TIME_ID, 0);
                values.put(Contract.UserEvents.SERVER_ID, 0);

                if (db.insert(Contract.UserEvents.TABLE, null, values) > 0) {
                    Log.i(TAG, message);
                }
            }
        }).start();
    }

    public UserEvents() {
        super();
    }

    public UserEvents(Cursor cursor) {
        super(cursor);
    }

    public UserEvents(JSONObject object) {
        super(object);
    }

    @Override
    public String getIdColumnName() {
        return Contract.UserEvents.DEVICE_ID;
    }

    @Override
    public String[] getProjection() {
        return Contract.UserEvents.PROJECTION;
    }

    @Override
    public String[] getConstraints() {
        return new String[] {};
    }

    @Override
    public Uri getUri() {
        if (getId() > 0) {
            return ContentUris.withAppendedId(Contract.UserEvents.CONTENT_URI, getId());
        }
        return Contract.UserEvents.CONTENT_URI;
    }
}

package ca.taglab.vocabnomad.db;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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

    private static double longitude = 0, latitude = 0, altitude = 0;


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
                Location location;
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


                /*
                if ((location = getLocation()) != null) {
                    values.put(Contract.UserEvents.LONG, location.getLongitude());
                    message += " Long=" + location.getAltitude();

                    values.put(Contract.UserEvents.LAT, location.getLatitude());
                    message += " Lat=" + location.getAltitude();

                    values.put(Contract.UserEvents.ALT, location.getAltitude());
                    message += " Alt=" + location.getAltitude();
                } else {
                    values.put(Contract.UserEvents.LONG, 0);
                    message += " Long=" + 0;
                    values.put(Contract.UserEvents.LAT, 0);
                    message += " Lat=" + 0;
                    values.put(Contract.UserEvents.ALT, 0);
                    message += " Alt=" + 0;
                }*/

                values.put(Contract.UserEvents.LONG, longitude);
                message += " Long=" + longitude;
                values.put(Contract.UserEvents.LAT, latitude);
                message += " Lat=" + latitude;
                values.put(Contract.UserEvents.ALT, altitude);
                message += " Alt=" + altitude;


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

    /**
     * Get the user's current location
     * @return  The user's current location
     */
    private static Location getLocation() {
        LocationManager locationManager;
        String provider;

        // Get location manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Check if enabled and if not send user to the GPS settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        // Define the criteria how to select the location provider -> use default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Log.i(TAG, "Provider " + provider + " has been selected");

        return locationManager.getLastKnownLocation(provider);
    }


    public static void setLocation(double longitude, double latitude, double altitude) {
        UserEvents.longitude = longitude;
        UserEvents.latitude = latitude;
        UserEvents.altitude = altitude;
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

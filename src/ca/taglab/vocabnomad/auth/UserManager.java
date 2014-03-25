package ca.taglab.vocabnomad.auth;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.db.UserEvents;
import ca.taglab.vocabnomad.types.User;

import java.io.IOException;

public class UserManager {
    public static final String TAG = "UserManager";

    /**
     * The user that is currently logged in.
     */
    private static User mUser;


    /**
     * Find the user that is currently logged in to the application.
     * @param context  Activity's current context
     */
    public static void login(Context context) {
        Cursor cursor;

        try {
            DatabaseHelper.getInstance(context).open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        cursor = context.getContentResolver().query(
                Contract.User.CONTENT_URI,
                Contract.User.PROJECTION,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            mUser = new User(cursor);
        }

    }

    /**
     * Logout of the application.
     */
    public static void logout() {
        mUser = null;
    }


    /**
     * Return true if the user logged in, false otherwise.
     */
    public static boolean isLoggedIn() {
        return UserManager.mUser != null;
    }


    /**
     * Add a new user to the database and log them in.
     * @param context   Context of the application
     * @param user      User to add to the database
     */
    public static void addUser(Context context, User user) {
        try {
            DatabaseHelper.getInstance(context).open();
            Uri uri = user.commit(context.getContentResolver());
            UserManager.mUser = new User();
            UserManager.mUser.refresh(uri, context.getContentResolver());
            UserEvents.log(Contract.UserEvents.LOGIN, 0, 0, 0, 0, null);
        } catch (Exception e) {
            Log.e(TAG, "There was an error logging the user in.");
        }
    }


    /**
     * Get the logged in user's ID.
     * @return  User ID
     */
    public static long getUserId() {
        return mUser != null ? mUser.getId() : 0;
    }


    /**
     * Get the logged in user's username.
     * @return  Username
     */
    public static String getUsername() {
        return (mUser != null ? mUser.getUsername() : null);
    }


    /**
     * Get the logged in user's mother tongue.
     * @return  Mother tongue ID
     */
    public static long getMotherTongue() {
        return (mUser != null ? mUser.getMotherTongue() : 0);
    }


    /**
     * Get the URI for the logged in user.
     * @return  User content URI
     */
    public static Uri getUri() {
        return ContentUris.withAppendedId(Contract.User.CONTENT_URI, UserManager.getUserId());
    }

}

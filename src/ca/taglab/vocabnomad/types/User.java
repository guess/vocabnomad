package ca.taglab.vocabnomad.types;

import ca.taglab.vocabnomad.db.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import ca.taglab.vocabnomad.db.DAO;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class User extends DAO {


    //////////////////////////////////////////////////////////////////////////////
    //							    USER CONSTRUCTORS							//
    //////////////////////////////////////////////////////////////////////////////


    public User() {
        super();
    }


    public User(Cursor cursor) {
        super(cursor);
    }


    public User(JSONObject object) {
        super(object);
    }





    //////////////////////////////////////////////////////////////////////////////
    //			    	    	USER GETTERS & SETTERS	          				//
    //////////////////////////////////////////////////////////////////////////////


    /**	Set the user's name */
    public void setUsername(String username) {
        set(Contract.User.USERNAME, username);
    }


    /** Get user's name. */
    public String getUsername() {
        return (String) get(Contract.User.USERNAME, null);
    }


    /**	Set password for this user */
    public void setPassword(String password) {
        set(Contract.User.PASSWORD, password);
    }


    /** Get the password for this user. */
    public String getPassword() {
        return (String) get(Contract.User.PASSWORD, null);
    }


    /**
     * Set the mother tongue for this user.
     * @param motherTongue  ID of the mother tongue
     */
    public void setMotherTongue(long motherTongue) {
        set(Contract.User.MOTHER_TONGUE, motherTongue);
    }


    /**
     * Get the mother tongue for this user.
     * @return ID of the mother tongue. 0 if they have not indicated one.
     */
    public int getMotherTongue() {
        return (Integer) get(Contract.User.MOTHER_TONGUE, 0);
    }


    /**
     * Get the String representation of this Language object.
     */
    @Override
    public String toString() {
        return getUsername();
    }





    //////////////////////////////////////////////////////////////////////////////
    //							IMPLEMENTATION OF DAO							//
    //////////////////////////////////////////////////////////////////////////////


    @Override
    public void setJSONObject(JSONObject object) {
        try {
            /* Server-database inconsistencies */
            setUsername(object.getString("userName"));
            setPassword(object.getString("pwd"));
        } catch (JSONException e) {
        }

        super.setJSONObject(object);
    }


    @Override
    public Uri getUri() {
        if (getId() > 0) {
            return ContentUris.withAppendedId(Contract.User.CONTENT_URI, getId());
        }

        return Contract.User.CONTENT_URI;
    }


    @Override
    public String getIdColumnName() {
        return Contract.User.USER_ID;
    }


    @Override
    public String[] getProjection() {
        return Contract.User.PROJECTION;
    }


    @Override
    public String[] getConstraints() {
        return Contract.User.CONSTRAINTS;
    }
}

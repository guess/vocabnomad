package ca.taglab.vocabnomad.types;

import android.text.TextUtils;
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

public class Language extends DAO {


    //////////////////////////////////////////////////////////////////////////////
    //							LANGUAGE CONSTRUCTORS							//
    //////////////////////////////////////////////////////////////////////////////


    public Language() {
        super();
    }


    public Language(Cursor cursor) {
        super(cursor);
    }


    public Language(JSONObject object) {
        super(object);
    }





    //////////////////////////////////////////////////////////////////////////////
    //			    		LANGUAGE GETTERS & SETTERS	          				//
    //////////////////////////////////////////////////////////////////////////////


    /**	Set the name of this language */
    public void setLanguage(String language) {
        set(Contract.Language.LANGUAGE, language);
    }


    /** Get the name of this language */
    public String getLanguage() {
        return (String) get(Contract.Language.LANGUAGE, null);
    }


    /** Get the String representation of this language */
    @Override
    public String toString() {
        return getLanguage();
    }





    //////////////////////////////////////////////////////////////////////////////
    //							IMPLEMENTATION OF DAO							//
    //////////////////////////////////////////////////////////////////////////////


    @Override
    public Uri getUri() {
        if (getId() > 0) {
            return ContentUris.withAppendedId(Contract.Language.CONTENT_URI, getId());
        }

        return Contract.Language.CONTENT_URI;
    }


    @Override
    public String getIdColumnName() {
        return Contract.Language.LANG_ID;
    }


    @Override
    public String[] getProjection() {
        return Contract.Language.PROJECTION;
    }


    @Override
    public String[] getConstraints() {
        return Contract.Language.CONSTRAINTS;
    }

}

package ca.taglab.vocabnomad.types;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DAO;
import org.json.JSONObject;

import java.util.Date;

public class Tag extends DAO {


    //////////////////////////////////////////////////////////////////////////////
    //							    TAG CONSTRUCTORS							//
    //////////////////////////////////////////////////////////////////////////////


    public Tag() {
        super();
        init();
    }


    public Tag(Cursor cursor) {
        super(cursor);
        init();
    }


    public Tag(JSONObject object) {
        super(object);
        init();
    }


    public void init() {
        if (!values.containsKey(Contract.Tag.USER_ID))
            setOwner(UserManager.getUserId());

        setDeleted(false);
    }





    //////////////////////////////////////////////////////////////////////////////
    //			    	    	TAG GETTERS & SETTERS	          				//
    //////////////////////////////////////////////////////////////////////////////


    /**	Set the tag */
    public void setTag(String tag) {
        set(Contract.Tag.NAME, tag);
    }


    /** Get the tag */
    public String getTag() {
        return (String) get(Contract.Tag.NAME, null);
    }


    /**	Set the tag's server ID */
    public void setServerId(long serverId) {
        set(Contract.Tag.SERVER_ID, serverId);
    }


    /** Get the tag's server ID */
    public long getServerId() {
        return ((Integer) get(Contract.Tag.SERVER_ID, 0)).longValue();
    }


    /**	Set the date the tag was last modified */
    public void setDateModified(String dateModified) {
        set(Contract.Tag.DATE_MODIFIED, dateModified);
    }


    /** Get the date the tag was last modified */
    public String getDateModified() {
        return (String) get(Contract.Tag.DATE_MODIFIED, null);
    }


    /**	Set the owner of the tag */
    public void setOwner(long id) {
        set(Contract.Tag.USER_ID, id);
    }


    /** Get the owner of the tag */
    public long getOwner() {
        return ((Integer) get(Contract.Tag.USER_ID, 0)).longValue();
    }


    /** Set the tag's deleted status */
    public void setDeleted(boolean deleted) {
        set(Contract.Tag.DELETED, (deleted? 1 : 0));
    }


    /** Check to see if the tag has been deleted */
    public boolean isDeleted() {
        return ((Integer) get(Contract.Tag.DELETED, 0)) > 0;
    }


    /**
     * Get the String representation of this Language object.
     */
    @Override
    public String toString() {
        return getTag();
    }





    //////////////////////////////////////////////////////////////////////////////
    //							IMPLEMENTATION OF DAO							//
    //////////////////////////////////////////////////////////////////////////////

    @Override
    public Uri commit(ContentResolver resolver) throws Exception {
        Uri uri;
        Cursor cursor;

        /*
         * Make sure that the tag does not already exist in the database.
         */

        cursor = resolver.query(
                Contract.Tag.getUri(),
                null,
                Contract.Tag.NAME + "=?",
                new String[] { values.getAsString(Contract.Tag.NAME) },
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            uri = new Tag(cursor).getUri();
            cursor.close();
            return uri;
        }

        /*
         * Tag does not exist in the database.
         */

        //if (values.size() > 0 && !values.containsKey(Contract.Tag.DATE_MODIFIED)) {
        if (values.size() > 0) {
            setDateModified("/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
        }

        return super.commit(resolver);
    }


    @Override
    public Uri getUri() {
        if (getId() > 0) {
            return ContentUris.withAppendedId(Contract.Tag.getUri(), getId());
        }

        return Contract.Tag.getUri();
    }


    @Override
    public String getIdColumnName() {
        return Contract.Tag.TAG_ID;
    }


    @Override
    public String[] getProjection() {
        return Contract.Tag.PROJECTION;
    }


    @Override
    public String[] getConstraints() {
        return Contract.Tag.CONSTRAINTS;
    }

}

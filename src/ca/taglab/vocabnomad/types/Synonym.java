package ca.taglab.vocabnomad.types;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DAO;
import org.json.JSONObject;

public class Synonym extends DAO {

    //////////////////////////////////////////////////////////////////////////////
    //							SYNONYM CONSTRUCTORS							//
    //////////////////////////////////////////////////////////////////////////////


    public Synonym() {
        super();
    }


    public Synonym(Cursor cursor) {
        super(cursor);
    }


    public Synonym(JSONObject object) {
        super(object);
    }





    //////////////////////////////////////////////////////////////////////////////
    //			    		LANGUAGE GETTERS & SETTERS	          				//
    //////////////////////////////////////////////////////////////////////////////


    /** Get the String representation of this language */
    @Override
    public String toString() {
        return null;
    }





    //////////////////////////////////////////////////////////////////////////////
    //							IMPLEMENTATION OF DAO							//
    //////////////////////////////////////////////////////////////////////////////


    @Override
    public Uri getUri() {
        if (getId() > 0) {
            return ContentUris.withAppendedId(Contract.Synonyms.CONTENT_URI, getId());
        }
        return Contract.Synonyms.CONTENT_URI;
    }


    @Override
    public String getIdColumnName() {
        return Contract.Synonyms.SYN_ID;
    }


    @Override
    public String[] getProjection() {
        return Contract.Synonyms.PROJECTION;
    }


    @Override
    public String[] getConstraints() {
        return new String[] {};
    }
}

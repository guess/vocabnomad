package ca.taglab.vocabnomad.types;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DAO;
import org.json.JSONObject;

import java.util.Date;

public class Word extends DAO {


    //////////////////////////////////////////////////////////////////////////////
    //							    WORD CONSTRUCTORS							//
    //////////////////////////////////////////////////////////////////////////////


    public Word() {
        super();
        init();
    }


    public Word(Cursor cursor) {
        super(cursor);
        init();
    }


    public Word(JSONObject object) {
        super(object);
        init();
    }


    private void init() {
        if (!values.containsKey(Contract.Word.USER_ID))
            setOwner(UserManager.getUserId());

        if (!values.containsKey(Contract.Word.DELETED))
            setDeleted(false);

        if (!values.containsKey(Contract.Word.SHARED))
            setShared(false);

        if (!values.containsKey(Contract.Word.WEIGHT))
            setWeight((float) 1.0);
    }





    //////////////////////////////////////////////////////////////////////////////
    //			    	    	WORD GETTERS & SETTERS	          				//
    //////////////////////////////////////////////////////////////////////////////


    /**	Set the word */
    public void setWord(String word) {
        set(Contract.Word.ENTRY, word);
    }


    /** Get the word */
    public String getWord() {
        return (String) get(Contract.Word.ENTRY, null);
    }


    /**	Set the server ID for the word */
    public void setServerId(long id) {
        set(Contract.Word.SERVER_ID, id);
    }


    /** Get the server ID for the word */
    public long getServerId() {
        return ((Integer) get(Contract.Word.SERVER_ID, 0)).longValue();
    }


    /**	Set the path where the word's image is located */
    public void setImageFilePath(String path) {
        set(Contract.Word.IMG, path);
    }


    /** Get the path where the word's image is located, null if it doesn't exist. */
    public String getImageFilePath() {
        return (String) get(Contract.Word.IMG, null);
    }


    /**	Set the word's definition */
    public void setDefinition(String definition) {
        set(Contract.Word.DEFINITION, definition);
    }


    /** Get the word's definition */
    public String getDefinition() {
        return (String) get(Contract.Word.DEFINITION, null);
    }


    /**	Set the word's sentence */
    public void setSentence(String sentence) {
        set(Contract.Word.SENTENCE, sentence);
    }


    /** Get the word's sentence */
    public String getSentence() {
        return (String) get(Contract.Word.SENTENCE, null);
    }


    /**	Set the word's owner */
    public void setOwner(long id) {
        set(Contract.Word.USER_ID, id);
    }


    /** Get the word's owner */
    public long getOwner() {
        return ((Integer) get(Contract.Word.USER_ID, 0)).longValue();
    }


    /**	Set the word's shared status */
    public void setShared(boolean shared) {
        set(Contract.Word.SHARED, (shared) ? 1 : 0);
    }


    /** Return true if the word is shared, false otherwise */
    public boolean isShared() {
        return ((Integer) get(Contract.Word.SHARED, 0) > 0);
    }


    /**	Set the word's deleted status */
    public void setDeleted(boolean deleted) {
        set(Contract.Word.DELETED, (deleted ? 1 : 0));
    }


    /** Return true if the word is deleted, false otherwise */
    public boolean isDeleted() {
        return ((Integer) get(Contract.Word.DELETED, 0) > 0);
    }


    /**	Set the date the word was added */
    public void setDateAdded(String dateAdded) {
        set(Contract.Word.DATE_ADDED, dateAdded);
    }


    /** Get the date the word was added */
    public String getDateAdded() {
        return (String) get(Contract.Word.DATE_ADDED, null);
    }


    /**	Set the date the word was last modified */
    public void setDateModified(String dateModified) {
        set(Contract.Word.DATE_MODIFIED, dateModified);
    }


    /** Get the date the word was last modified */
    public String getDateModified() {
        return (String) get(Contract.Word.DATE_MODIFIED, null);
    }


    /**	Set the word's weight */
    public void setWeight(float weight) {
        set(Contract.Word.WEIGHT, weight);
    }


    /** Get the word's weight */
    public float getWeight() {
        return (Float) get(Contract.Word.WEIGHT, 1.0);
    }


    /**
     * Get the String representation of this Language object.
     */
    @Override
    public String toString() {
        return getWord();
    }





    //////////////////////////////////////////////////////////////////////////////
    //							IMPLEMENTATION OF DAO							//
    //////////////////////////////////////////////////////////////////////////////

    @Override
    public Uri commit(ContentResolver resolver) throws Exception {
        if (values.size() > 0) {

            if (!values.containsKey(Contract.Word.DATE_MODIFIED) || !values.containsKey(Contract.Word.WORD_ID)) {
                /* Word has been modified */
                setDateModified("/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
            }

            if (getDateAdded() == null && !values.containsKey(Contract.Word.DATE_ADDED)) {
                /* Word is just being added */
                setDateAdded("/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
            }
        }

        Log.i("Word", values.toString());

        return super.commit(resolver);
    }


    @Override
    public Uri getUri() {
        if (getId() > 0) {
            return ContentUris.withAppendedId(Contract.Word.getUri(), getId());
        }

        return Contract.Word.getUri();
    }


    @Override
    public String getIdColumnName() {
        return Contract.Word.WORD_ID;
    }


    @Override
    public String[] getProjection() {
        return Contract.Word.PROJECTION;
    }


    @Override
    public String[] getConstraints() {
        return Contract.Word.CONSTRAINTS;
    }
}

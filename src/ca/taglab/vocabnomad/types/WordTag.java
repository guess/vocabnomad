package ca.taglab.vocabnomad.types;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DAO;
import org.json.JSONObject;

import java.util.Date;

public class WordTag extends DAO {


    //////////////////////////////////////////////////////////////////////////////
    //							    USER CONSTRUCTORS							//
    //////////////////////////////////////////////////////////////////////////////


    public WordTag() {
        super();
    }


    public WordTag(Word word, Tag tag) {
        setOwner(word.getOwner());
        setWordId(word.getId());
        setWordServerId(word.getServerId());
        setTagId(tag.getId());
        setTagServerId(tag.getServerId());
        setDeleted(0);
        setServerID(0);
        setDateModified("/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
    }


    public WordTag(Cursor cursor) {
        super(cursor);
    }


    public WordTag(JSONObject object) {
        super(object);
    }





    //////////////////////////////////////////////////////////////////////////////
    //			    	    	USER GETTERS & SETTERS	          				//
    //////////////////////////////////////////////////////////////////////////////


    /**	Set the word tag pair's server ID */
    public void setServerID(long serverID) {
        set(Contract.WordTag.SERVER_ID, serverID);
    }


    /** Get the word tag pair's server ID */
    public long getServerId() {
        return (Long) get(Contract.WordTag.SERVER_ID, 0);
    }


    /**	Set the word's device ID */
    public void setWordId(long wordId) {
        set(Contract.WordTag.WORD_ID, wordId);
    }


    /** Get the word's device ID */
    public long getWordId() {
        return (Long) get(Contract.WordTag.WORD_ID, 0);
    }


    /**	Set the word's server ID */
    public void setWordServerId(long wordServerId) {
        set(Contract.WordTag.WORD_SID, wordServerId);
    }


    /** Get the word's server ID */
    public long getWordServerId() {
        return (Long) get(Contract.WordTag.WORD_SID, 0);
    }


    /**	Set the tag's device ID */
    public void setTagId(long tagId) {
        set(Contract.WordTag.TAG_ID, tagId);
    }


    /** Get the tag's device ID */
    public long getTagId() {
        return (Long) get(Contract.WordTag.TAG_ID, 0);
    }


    /**	Set the tag's server ID */
    public void setTagServerId(long tagServerId) {
        set(Contract.WordTag.TAG_SID, tagServerId);
    }


    /** Get the tag's server ID */
    public long getTagServerId() {
        return (Long) get(Contract.WordTag.TAG_SID, 0);
    }


    /**	Set the owner's user ID */
    public void setOwner(long id) {
        set(Contract.WordTag.USER_ID, id);
    }


    /** Get the owner's user ID */
    public long getOwner() {
        return (Long) get(Contract.WordTag.USER_ID, 0);
    }


    /** Get the deleted status of the word tag */
    public boolean isDeleted() {
        return (Long) get(Contract.WordTag.DELETED, 0) > 0;
    }


    /** Set the deleted status of the word tag */
    public void setDeleted(long deleted) {
        set(Contract.WordTag.DELETED, deleted);
    }


    /**	Set the date the word tag pair was last modified */
    public void setDateModified(String dateModified) {
        set(Contract.WordTag.DATE_MODIFIED, dateModified);
    }


    /** Get the date the word tag pair was last modified */
    public String getDateModified() {
        return (String) get(Contract.WordTag.DATE_MODIFIED, null);
    }





    //////////////////////////////////////////////////////////////////////////////
    //							IMPLEMENTATION OF DAO							//
    //////////////////////////////////////////////////////////////////////////////


    @Override
    public Uri commit(ContentResolver resolver) throws Exception {
        Cursor c;

        /*
         * WordTag does not include a Word ID.
         * In this case it should contain a ServerID, so get the Word with the specified ServerID.
         */
        if (!values.containsKey(Contract.WordTag.WORD_ID) || values.getAsLong(Contract.WordTag.WORD_ID) <= 0) {
            c = resolver.query(
                    Contract.Word.getUri(),
                    null,
                    Contract.Word.SERVER_ID + "=?",
                    new String[] { values.getAsString(Contract.WordTag.WORD_SID) },
                    null,
                    null
            );

            if (c != null && c.moveToFirst()) {
                setWordId(new Word(c).getId());
                Word word = new Word(c);
                Log.i("WordTag Word Modified", word.getWord() + ": " + word.getDateModified());
                setDateModified("/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
                c.close();
            }
        }

        /*
         * WordTag does not include a Tag ID.
         * In this case it should contain a ServerID, so get the Tag with the specified ServerID.
         */
        if (!values.containsKey(Contract.WordTag.TAG_ID) || values.getAsLong(Contract.WordTag.TAG_ID) <= 0) {
            c = resolver.query(
                    Contract.Tag.getUri(),
                    null,
                    Contract.Tag.SERVER_ID + "=?",
                    new String[] { values.getAsString(Contract.WordTag.TAG_SID) },
                    null,
                    null
            );

            if (c != null && c.moveToFirst()) {
                setTagId(new Tag(c).getId());
                setDateModified("/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
                c.close();
            }
        }

        /*
        cursor = resolver.query(
                Contract.WordTag.getUri(),
                Contract.WordTag.PROJECTION,
                Contract.WordTag.WORD_ID + "=? AND " + Contract.WordTag.TAG_ID + "=?",
                new String[] { values.getAsString(Contract.WordTag.WORD_ID), values.getAsString(Contract.WordTag.TAG_ID)},
                null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            Uri uri = new WordTag(cursor).getUri();
            cursor.close();
            return uri;
        }
        */

        return super.commit(resolver);
    }


    @Override
    public Uri getUri() {
        if (getId() > 0) {
            return ContentUris.withAppendedId(Contract.WordTag.getUri(), getId());
        }

        return Contract.WordTag.getUri();
    }


    @Override
    public String getIdColumnName() {
        return Contract.WordTag.DEVICE_ID;
    }


    @Override
    public String[] getProjection() {
        return Contract.WordTag.PROJECTION;
    }


    @Override
    public String[] getConstraints() {
        return Contract.WordTag.CONSTRAINTS;
    }
}

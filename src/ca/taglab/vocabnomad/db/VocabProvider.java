package ca.taglab.vocabnomad.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class VocabProvider extends ContentProvider {

    public static final String TAG = "VocabProvider";

    public static final String AUTHORITY = Contract.AUTHORITY;


    public static final int LANGUAGES = 1;
    public static final int LANGUAGE_ID = 2;

    public static final int USERS = 3;
    public static final int USER_ID = 4;

    public static final int WORDS = 5;
    public static final int WORD_ID = 6;
    public static final int WORD_TAGS = 7;
    public static final int WORD_TAG_ID = 8;

    public static final int TAGS = 9;
    public static final int TAG_ID = 10;
    public static final int TAG_WORDS = 11;
    public static final int TAG_WORD_ID = 12;

    public static final int VTPAIRS = 14;
    public static final int VTPAIRS_ID = 15;

    public static final int DELETED_WORDS = 16;

    public static final int SYNONYMS = 17;
    public static final int WORD_SYN = 18;

    public static final int USER_EVENTS = 19;
    public static final int USER_EVENTS_ID = 20;

    public static final int VOICE_CLIPS = 21;
    public static final int VOICE_CLIP_ID = 22;

    private static UriMatcher sUriMatcher;

    static {

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(AUTHORITY, "languages", LANGUAGES);
        sUriMatcher.addURI(AUTHORITY, "languages/#", LANGUAGE_ID);

        sUriMatcher.addURI(AUTHORITY, "users", USERS);
        sUriMatcher.addURI(AUTHORITY, "users/#", USER_ID);

        sUriMatcher.addURI(AUTHORITY, "users/#/vocab", WORDS);
        sUriMatcher.addURI(AUTHORITY, "users/#/vocab/#", WORD_ID);
        sUriMatcher.addURI(AUTHORITY, "users/#/vocab/#/tags", WORD_TAGS);
        sUriMatcher.addURI(AUTHORITY, "users/#/vocab/#/tags/#", WORD_TAG_ID);
        sUriMatcher.addURI(AUTHORITY, "users/#/vocab/#/synonyms", WORD_SYN);
        sUriMatcher.addURI(AUTHORITY, "synonyms/#", SYNONYMS);
        sUriMatcher.addURI(AUTHORITY, "synonyms", SYNONYMS);

        sUriMatcher.addURI(AUTHORITY, "users/#/deleted_vocab", DELETED_WORDS);

        sUriMatcher.addURI(AUTHORITY, "users/#/tags", TAGS);
        sUriMatcher.addURI(AUTHORITY, "users/#/tags/#", TAG_ID);
        sUriMatcher.addURI(AUTHORITY, "users/#/tags/#/vocab", TAG_WORDS);
        sUriMatcher.addURI(AUTHORITY, "users/#/tags/#/vocab/#", TAG_WORD_ID);

        sUriMatcher.addURI(AUTHORITY, "users/#/vocabTagPairs", VTPAIRS);
        sUriMatcher.addURI(AUTHORITY, "users/#/vocabTagPairs/#", VTPAIRS_ID);

        sUriMatcher.addURI(AUTHORITY, "userEvents", USER_EVENTS);
        sUriMatcher.addURI(AUTHORITY, "userEvents/#", USER_EVENTS_ID);

        sUriMatcher.addURI(AUTHORITY, "voice", VOICE_CLIPS);
        sUriMatcher.addURI(AUTHORITY, "voice/#", VOICE_CLIP_ID);
    }

    DatabaseHelper db;



    /**
     * Create a new instance of the vocabulary content provider.
     */
    @Override
    public boolean onCreate() {
        db = DatabaseHelper.getInstance(getContext());
        return false;
    }





    /**
     * Get the MIME type of the specified URI
     *
     * @param uri	The URI of the object
     * @return		The MIME time of the object
     */
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {

            case LANGUAGES:
                return Contract.Language.CONTENT_TYPE;

            case LANGUAGE_ID:
                return Contract.Language.CONTENT_LANG_TYPE;

            case USERS:
                return Contract.User.CONTENT_TYPE;

            case USER_ID:
                return Contract.User.CONTENT_USER_TYPE;

            case SYNONYMS:
            case TAG_WORDS:
            case WORDS:
                return Contract.Word.CONTENT_TYPE;

            case TAG_WORD_ID:
            case WORD_ID:
                return Contract.Word.CONTENT_WORD_TYPE;

            case WORD_TAGS:
            case TAGS:
                return Contract.Tag.CONTENT_TYPE;

            case WORD_TAG_ID:
            case TAG_ID:
                return Contract.Tag.CONTENT_TAG_TYPE;

            case VTPAIRS:
                return Contract.WordTag.CONTENT_TYPE;

            case VTPAIRS_ID:
                return Contract.WordTag.CONTENT_WORDTAG_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }





    /**
     * Query objects in the database.
     *
     * @param uri			The URI of the object
     * @param projection	A list of columns to be queried
     * @param where			The where clause
     * @param whereArgs		The arguments for the where clause
     * @param sortOrder		The order of elements
     *
     * @return				A cursor containing the queried objects
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String where,
                        String[] whereArgs, String sortOrder) {

        Cursor c;
        String table, group = null, having = null;
        int match = sUriMatcher.match(uri);

        switch(match) {


            //////////////////////////////////////////////////////
            //						LANGUAGES					//
            //////////////////////////////////////////////////////

            case LANGUAGE_ID:
			/*
			 * Query a specific language from the Language table.
			 * Path: //languages/#
			 */
                where = ADD_CONSTRAINT(where, Contract.Language._ID, ContentUris.parseId(uri));

            case LANGUAGES:
			/*
			 * Query the Language table of the database.
			 * Path: //languages
			 */
                table = Contract.Language.TABLE;
                if (projection == null) projection = Contract.Language.PROJECTION;
                break;


            //////////////////////////////////////////////////////
            //						USERS						//
            //////////////////////////////////////////////////////

            case USER_ID:
			/*
			 * Query a specific user from the User table.
			 * Path: //users/#
			 */
                where = ADD_CONSTRAINT(where, Contract.User._ID, ContentUris.parseId(uri));

            case USERS:
			/*
			 * Query the user's table of the database.
			 * Path: //users
			 */
                table = Contract.User.TABLE;
                if (projection == null) projection = Contract.User.PROJECTION;
                break;


            //////////////////////////////////////////////////////
            //						TAGS						//
            //////////////////////////////////////////////////////

            case TAG_WORD_ID:
			/*
			 * Query a specific word that is associated with a specified tag.
			 * Path: //users/#/tags/#/vocab/#
			 */
                where = ADD_CONSTRAINT(where, Contract.View.WORD_ID, ContentUris.parseId(uri));

            case TAG_WORDS:
			/*
			 * Query all words that are associated with a specified tag.
			 * Path: //users/#/tags/#/vocab
			 */
                table = Contract.View.TABLE;
                if (projection == null) projection = Contract.View.WORD;
                where = ADD_CONSTRAINT(where, Contract.View.USER_ID, uri.getPathSegments().get(1));
                where = ADD_CONSTRAINT(where, Contract.View.TAG_ID, uri.getPathSegments().get(3));
                break;

            case TAG_ID:
			/*
			 * Query a specific tag from the Tags table.
			 * Path: //users/#/tags/#
			 */
                where = ADD_CONSTRAINT(where, Contract.Tag._ID, ContentUris.parseId(uri));

            case TAGS:
			/*
			 * Query the Tags table.
			 * Path: //users/#/tags
			 */
                table = Contract.Tag.TABLE;
                if (projection == null) projection = Contract.Tag.PROJECTION;
                where = ADD_CONSTRAINT(where, Contract.Tag.USER_ID, uri.getPathSegments().get(1));
                where = ADD_CONSTRAINT(where, Contract.Tag.DELETED, 0);
                break;


            //////////////////////////////////////////////////////
            //						WORDS						//
            //////////////////////////////////////////////////////

            case WORD_TAG_ID:
			/*
			 * Query a specific tag that is associated with a specified word.
			 * Path: //users/#/vocab/#/tags/#
			 */
                where = ADD_CONSTRAINT(where, Contract.View.TAG_ID, ContentUris.parseId(uri));

            case WORD_TAGS:
			/*
			 * Query the tags that are associated with a specified word.
			 * Path: //users/#/vocab/#/tags
			 */
                table = Contract.View.TABLE;
                if (projection == null) projection = Contract.View.TAG;
                where = ADD_CONSTRAINT(where, Contract.View.USER_ID, uri.getPathSegments().get(1));
                where = ADD_CONSTRAINT(where, Contract.View.WORD_ID, uri.getPathSegments().get(3));
                where = ADD_CONSTRAINT(where, Contract.View.VTP_DELETED, 0);
                break;

            case WORD_ID:
			/*
			 * Query a specific word in the Vocabulary table.
			 * Path: //users/#/vocab/#
			 */
                where = ADD_CONSTRAINT(where, Contract.Word._ID, ContentUris.parseId(uri));

            case DELETED_WORDS:
            case WORDS:
			/*
			 * Query the Vocabulary table.
			 * Path: //users/#/vocab
			 * Path: //users/#/deleted_vocab
			 */
                table = Contract.View.TABLE;
                if (projection == null) projection = Contract.View.WORD;
                where = ADD_CONSTRAINT(where, Contract.View.USER_ID, uri.getPathSegments().get(1));
                group = Contract.View.WORD_ID;

                if (match == DELETED_WORDS) {
                    where = ADD_CONSTRAINT(where, Contract.View.DELETED, 1);
                } else {
                    where = ADD_CONSTRAINT(where, Contract.View.DELETED, 0);
                }

                Log.i("Word", where);
                break;

            case WORD_SYN:
            /*
             * Query the Synonyms table.
             * Path: //users/#/vocab/#/synonyms
             */
                table = Contract.Synonyms.TABLE;
                if (projection == null) projection = Contract.Synonyms.PROJECTION;
                where = ADD_CONSTRAINT(where, Contract.Synonyms.USER_ID, uri.getPathSegments().get(1));
                where = ADD_CONSTRAINT(where, Contract.Synonyms.ENTRY_SID, uri.getPathSegments().get(3));

                Log.i("Synonyms: ", where);
                break;


            case SYNONYMS:
                table = Contract.Synonyms.TABLE;
                if (projection == null) projection = Contract.Synonyms.PROJECTION;
                where = ADD_CONSTRAINT(where, Contract.Synonyms._ID, uri.getPathSegments().get(1));
                break;


            //////////////////////////////////////////////////////
            //					WORD-TAG PAIRS					//
            //////////////////////////////////////////////////////

            case VTPAIRS_ID:
			/*
			 * Query a specific Vocabulary-Tag Pair.
			 * Path: //users/#/vocabTagPairs/#
			 */
                where = ADD_CONSTRAINT(where, Contract.WordTag._ID, ContentUris.parseId(uri));

            case VTPAIRS:
			/*
			 * Query the Vocabulary-Tag Pairs table.
			 * Path: //users/#/vocabTagPairs/
			 */
                table = Contract.WordTag.TABLE;
                if (projection == null) projection = Contract.WordTag.PROJECTION;
                where = ADD_CONSTRAINT(where, Contract.WordTag.USER_ID, uri.getPathSegments().get(1));
                break;

            case USER_EVENTS_ID:
                where = ADD_CONSTRAINT(where, Contract.UserEvents._ID, ContentUris.parseId(uri));

            case USER_EVENTS:
            /*
             * Query the UserEvents table.
             * Path: //events
             */
                table = Contract.UserEvents.TABLE;
                if (projection == null) projection = Contract.WordTag.PROJECTION;
                break;

            case VOICE_CLIP_ID:
                table = Contract.VoiceClip.TABLE;
                if (projection == null) projection = Contract.VoiceClip.PROJECTION;
                where = ADD_CONSTRAINT(where, Contract.VoiceClip.WORD_ID, ContentUris.parseId(uri));
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        c = db.query(table, projection, where, whereArgs, group, having, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }





    /**
     * Insert a new object into the database.
     *
     * @param uri			The URI of the object to insert
     * @param initialValues	The content values containing the object's information
     *
     * @throws android.database.SQLException    If an error occurred while inserting the object
     *
     * @return				The URI of the newly inserted object
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) throws SQLException {
        String table;
        long id;
        ContentValues values;

        if (initialValues != null)
            values = new ContentValues(initialValues);
        else
            values = new ContentValues();

        switch(sUriMatcher.match(uri)) {

            case LANGUAGE_ID:
            case LANGUAGES:
                table = Contract.Language.TABLE;
                break;

            case USER_ID:
            case USERS:
                table = Contract.User.TABLE;
                break;

            case WORD_ID:
                throw new SQLException("Word already exists in the database");

            case WORDS:
                table = Contract.Word.TABLE;
                break;

            case TAG_ID:
                throw new SQLException("Tag already exists in the database.");

            case TAGS:
                table = Contract.Tag.TABLE;
                break;

            case VTPAIRS_ID:
                throw new SQLException("Vocabulary-Tag Pair already exists in the database");

            case VTPAIRS:
                table = Contract.WordTag.TABLE;
                Log.i(TAG, "WORDTAG CONTENT VALUES: " + values.toString());
                break;

            case SYNONYMS:
                table = Contract.Synonyms.TABLE;
                Log.i(TAG, "SYNONYM CONTENT VALUES: " + values.toString());
                break;

            case USER_EVENTS_ID:
                throw new SQLException("UserEvent already exists in the database");

            case USER_EVENTS:
                table = Contract.UserEvents.TABLE;
                break;

            case VOICE_CLIP_ID:
            case VOICE_CLIPS:
                table = Contract.VoiceClip.TABLE;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Insert entry into the table
        id = db.insert(table, null, values);

        if (id > 0) {
            Uri new_uri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(new_uri, null);
            return new_uri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }





    /**
     * Update an object in the database.
     *
     * @param uri		The URI of the object to update
     * @param values	The content values containing the updated information
     * @param where		The where clause
     * @param whereArgs	The arguments for the where clause
     *
     * @throws IllegalArgumentException when the URI is not recognized
     *
     * @return			The number of affected rows
     */
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        String table;
        int affected;

        // The call to notify the URI after deletion is explicit
        getContext().getContentResolver().notifyChange(uri, null);

        switch (sUriMatcher.match(uri)) {

            case LANGUAGE_ID:
                where = ADD_CONSTRAINT(where, Contract.Language.LANG_ID, ContentUris.parseId(uri));

            case LANGUAGES:
                table = Contract.Language.TABLE;
                break;

            case USER_ID:
                where = ADD_CONSTRAINT(where, Contract.User.USER_ID, ContentUris.parseId(uri));

            case USERS:
                table = Contract.User.TABLE;
                break;

            case TAG_ID:
                where = ADD_CONSTRAINT(where, Contract.Tag.TAG_ID, ContentUris.parseId(uri));

            case TAGS:
                table = Contract.Tag.TABLE;
                where = ADD_CONSTRAINT(where, Contract.Tag.USER_ID, uri.getPathSegments().get(1));
                //where = ADD_DATE_CONSTRAINT(where, values.getAsString(Contract.Tag.DATE_MODIFIED));
                break;

            case WORD_ID:
                where = ADD_CONSTRAINT(where, Contract.Word.WORD_ID, ContentUris.parseId(uri));

            case WORDS:
                table = Contract.Word.TABLE;
                where = ADD_CONSTRAINT(where, Contract.Word.USER_ID, uri.getPathSegments().get(1));
                //where = ADD_DATE_CONSTRAINT(where, values.getAsString(Contract.Word.DATE_MODIFIED));
                break;

            case VTPAIRS_ID:
                where = ADD_CONSTRAINT(where, Contract.WordTag.DEVICE_ID, ContentUris.parseId(uri));

            case VTPAIRS:
                table = Contract.WordTag.TABLE;
                where = ADD_CONSTRAINT(where, Contract.WordTag.USER_ID, uri.getPathSegments().get(1));
                break;

            case USER_EVENTS_ID:
                where = ADD_CONSTRAINT(where, Contract.UserEvents.DEVICE_ID, ContentUris.parseId(uri));

            case USER_EVENTS:
                table = Contract.UserEvents.TABLE;
                break;

            case VOICE_CLIP_ID:
                where = ADD_CONSTRAINT(where, Contract.VoiceClip.WORD_ID, ContentUris.parseId(uri));

            case VOICE_CLIPS:
                table = Contract.VoiceClip.TABLE;
                break;


            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        affected = db.update(table, values, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return affected;
    }





    /**
     * Delete an object from the database.
     * If the object is a word, set the "deleted" attribute to true.
     *
     * @param uri		The URI of the object to delete
     * @param where		The where clause
     * @param whereArgs	The where arguments
     *
     * @return			The number of rows that have been deleted
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        ContentValues values;
        int affected;
        String table;

        int match = sUriMatcher.match(uri);

        switch (match) {

            case LANGUAGE_ID:
                where = ADD_CONSTRAINT(where, Contract.Language.LANG_ID, ContentUris.parseId(uri));

            case LANGUAGES:
                table = Contract.Language.TABLE;
                break;

            case USER_ID:
                where = ADD_CONSTRAINT(where, Contract.User.USER_ID, ContentUris.parseId(uri));

            case USERS:
                table = Contract.User.TABLE;
                break;

            case TAG_ID:
                where = ADD_CONSTRAINT(where, Contract.Tag.TAG_ID, ContentUris.parseId(uri));

            case TAGS:
                values = new ContentValues();
                values.put(Contract.Tag.DELETED, 1);
                affected = db.update(Contract.Tag.TABLE, values, where, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return affected;

            case WORD_TAG_ID:
                where = ADD_CONSTRAINT(where, Contract.WordTag.TAG_ID, ContentUris.parseId(uri));

            case WORD_TAGS:
                where = ADD_CONSTRAINT(where, Contract.WordTag.USER_ID, uri.getPathSegments().get(1));
                where = ADD_CONSTRAINT(where, Contract.WordTag.WORD_ID, uri.getPathSegments().get(3));

            case VTPAIRS:
                table = Contract.Word.TABLE;
                break;

            case WORD_ID:
                where = ADD_CONSTRAINT(where, Contract.Word.WORD_ID, ContentUris.parseId(uri));

            case WORDS:
                values = new ContentValues();
                values.put(Contract.Word.DELETED, 1);
                affected = db.update(Contract.Word.TABLE, values, where, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return affected;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        affected = db.delete(table, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return affected;
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
    private String ADD_CONSTRAINT(String where, String col, Object val) {
        return col + "=" + val + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
    }

}

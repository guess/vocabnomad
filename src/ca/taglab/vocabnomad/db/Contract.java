package ca.taglab.vocabnomad.db;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import ca.taglab.vocabnomad.auth.UserManager;

/**
 * Abstract class for a Vocabulary term.
 */
public final class Contract {

    /** Authority for the content provider */
    public static final String AUTHORITY = "ca.taglab.vocabnomad";

    public static final String JIT_URL = "http://vnjitvocab.taglab.utoronto.ca/AlgorithmVocabulary";

    public static final String URL = "http://vndatasync.taglab.utoronto.ca/DataSyncRestService";

    public static final String DEFAULT_DATE = "1905-01-01-00-00";


    public static final class Skills {
        public static final String PREFERENCES = "vocabnomad_skills";
        public static final String READING = "reading";
        public static final String WRITING = "writing";
        public static final String SPEAKING = "speaking";
        public static final String LISTENING = "listening";
    }



    public static final class Language implements BaseColumns {

        /**
         * URL to access the Languages from the server.
         */
        public static final String URL = "http://vndatasync.taglab.utoronto.ca/DataSyncRestService/langs";


        //////////////////////////////////////////////////////////////////////////////
        //						LANGUAGE TABLE AND ATTRIBUTES		    			//
        //////////////////////////////////////////////////////////////////////////////

        /**
         * Language table name.
         */
        public static final String TABLE = "languages";

        /**
         * Language ID.
         */
        public static final String LANG_ID = "langID";

        /**
         * String representation of the Language.
         */
        public static final String LANGUAGE = "language";


        /**
         * Projection of a Language from the database.
         */
        public static final String[] PROJECTION = {
                LANG_ID + " AS " + _ID,
                LANGUAGE
        };


        /**
         * Columns that cannot be empty when inserting.
         */
        public static final String[] CONSTRAINTS = {
                LANG_ID,
                LANGUAGE
        };


        //////////////////////////////////////////////////////////////////////////////
        //							CONTENT PROVIDERS								//
        //////////////////////////////////////////////////////////////////////////////


        /**
         * URI references all languages.
         */
        public static final Uri LANGUAGE_URI = Uri.parse("content://" +
                AUTHORITY + "/" + Contract.Language.TABLE);


        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = LANGUAGE_URI;


        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of languages.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.vocabnomad.language";


        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single language.
         */
        public static final String CONTENT_LANG_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.vocabnomad.language";


    }



    public static final class User implements BaseColumns {

        /**
         * Vocab Sync Service URL for a user.
         */
        public static final String URL = "http://vndatasync.taglab.utoronto.ca/DataSyncRestService/user=";


        //////////////////////////////////////////////////////////////////////////////
        //							USER TABLE AND ATTRIBUTES						//
        //////////////////////////////////////////////////////////////////////////////

        /**
         * User table name.
         */
        public static final String TABLE = "users";

        /**
         * User ID.
         */
        public static final String USER_ID = "userID";

        /**
         * User's username.
         */
        //public static final String USERNAME = "userName";
        public static final String USERNAME = "username";

        /**
         * User password.
         */
        public static final String PASSWORD = "pwd";

        /**
         * User's native language.
         */
        public static final String MOTHER_TONGUE = "mothertongue";


        /**
         * Projection of a User from the database.
         */
        public static final String PROJECTION[] = {
                USER_ID + " AS " + _ID,
                USERNAME,
                PASSWORD,
                MOTHER_TONGUE
        };


        /**
         * The columns that cannot be blank when inserting into the database.
         */
        public static final String CONSTRAINTS[] = {
                USERNAME,
                PASSWORD
        };


        //////////////////////////////////////////////////////////////////////////////
        //							CONTENT PROVIDERS								//
        //////////////////////////////////////////////////////////////////////////////


        /**
         * URI references all users.
         */
        public static final Uri USER_URI = Uri.parse("content://" +
                AUTHORITY + "/" + User.TABLE);


        /**
         * The content:// style URI for this table.
         */
        public static final Uri CONTENT_URI = USER_URI;


        /**
         * The MIME type of a {@link #CONTENT_URI} providing a directory of users.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.vocabnomad.user";


        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single user.
         */
        public static final String CONTENT_USER_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.vocabnomad.user";

    }



    public static final class Tag implements BaseColumns {

        //////////////////////////////////////////////////////////////////////////////
        //						TAGS TABLE AND ATTRIBUTES							//
        //////////////////////////////////////////////////////////////////////////////

        /**
         * URL for the server.
         */
        public static final String URL = "http://vndatasync.taglab.utoronto.ca/DataSyncRestService/tags;";

        /**
         * Tags table name.
         */
        public static final String TABLE = "tags";

        /**
         * String representation of the Tag.
         */
        public static final String NAME = "name";

        /**
         * Owner of the Tag.
         */
        public static final String USER_ID = "userID";

        /**
         * Tag ID.
         */
        public static final String TAG_ID = "deviceID";

        /**
         * Tag Server ID.
         */
        public static final String SERVER_ID = "serverID";

        /**
         * Tag modified date.
         */
        public static final String DATE_MODIFIED = "dateModified";

        /**
         * Tag deleted field.
         */
        public static final String DELETED = "deleted";


        /**
         * Projection of a Language from the database.
         */
        public static final String[] PROJECTION = {
                NAME,
                USER_ID,
                TAG_ID + " AS " + _ID,
                SERVER_ID,
                DATE_MODIFIED,
                DELETED
        };


        /**
         * Columns that cannot be blank when inserting into the database.
         */
        public static final String[] CONSTRAINTS = {
                NAME,
                USER_ID,
                DATE_MODIFIED,
                DELETED
        };





        //////////////////////////////////////////////////////////////////////////////
        //							CONTENT PROVIDERS								//
        //////////////////////////////////////////////////////////////////////////////

        //static Uri.Builder builder = UserManager.getUri().buildUpon();

        /**
         * URI references all Tags.
         */
        //public static Uri CONTENT_URI = builder.appendPath(Contract.Tag.TABLE).build();

        public static Uri getUri() {
            Uri.Builder builder = UserManager.getUri().buildUpon();
            return builder.appendPath(Contract.Tag.TABLE).build();
        }


        /**
         * The MIME type of a directory of Tags.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.vocabnomad.tag";


        /**
         * The MIME type of a sub-directory of a single Tag.
         */
        public static final String CONTENT_TAG_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.vocabnomad.tag";

    }



    public static final class Word implements BaseColumns {


        //////////////////////////////////////////////////////////////////////////////
        //						VOCABULARY TABLE AND ATTRIBUTES						//
        //////////////////////////////////////////////////////////////////////////////

        /**
         * Server URL for Vocabulary.
         */
        public static final String URL = "http://vndatasync.taglab.utoronto.ca/DataSyncRestService/vocab;";

        /**
         * Vocabulary table name.
         */
        public static final String TABLE = "vocab";

        /**
         * String representation of a word.
         */
        public static final String ENTRY = "entry";

        /**
         * Word ID.
         */
        public static final String WORD_ID = "deviceID";

        /**
         * Word Server ID.
         */
        public static final String SERVER_ID = "serverID";

        /**
         * Owner of the word.
         */
        public static final String USER_ID = "userID";

        /**
         * Date the word was added.
         */
        public static final String DATE_ADDED = "dateAdded";

        /**
         * The modified date of the Word.
         */
        public static final String DATE_MODIFIED = "dateModified";

        /**
         * Image file path of the Word.
         */
        public static final String IMG = "img";

        /**
         * Definition of the Word.
         */
        public static final String DEFINITION = "definition";

        /**
         * Word usage in a sentence.
         */
        public static final String SENTENCE = "sentence";

        /**
         * Indicates if the Word is shared or not.
         */
        public static final String SHARED = "shared";

        /**
         * Indicates if the Word is deleted or not.
         */
        public static final String DELETED = "deleted";

        /**
         * The weight of the Word.
         */
        public static final String WEIGHT = "weight";


        /**
         * Projection of a Language from the database.
         */
        public static final String[] PROJECTION = {
                ENTRY,
                WORD_ID + " AS " + _ID,
                SERVER_ID,
                USER_ID,
                DATE_ADDED,
                DATE_MODIFIED,
                IMG,
                DEFINITION,
                SENTENCE,
                SHARED,
                DELETED,
                WEIGHT
        };


        /**
         * Columns that cannot be blank when inserting into the database.
         */
        public static final String[] CONSTRAINTS = {
                ENTRY,
                USER_ID,
                WEIGHT,
                DELETED,
                SHARED,
                DATE_MODIFIED,
                DATE_ADDED
        };





        //////////////////////////////////////////////////////////////////////////////
        //							CONTENT PROVIDERS								//
        //////////////////////////////////////////////////////////////////////////////

        //static Uri.Builder builder = UserManager.getUri().buildUpon();

        /**
         * URI references all Words.
         */
        //public static Uri CONTENT_URI = builder.appendPath(Contract.Word.TABLE).build();

        public static Uri getUri() {
            Uri.Builder builder = UserManager.getUri().buildUpon();
            return builder.appendPath(Contract.Word.TABLE).build();
        }


        /**
         * The MIME type of a directory of words.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.vocabnomad.word";


        /**
         * The MIME type of a single word.
         */
        public static final String CONTENT_WORD_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.vocabnomad.word";

    }



    public static final class WordTag implements BaseColumns {

        /**
         * Server URL for vocabulary tag pairs.
         */
        public static final String URL = "http://vndatasync.taglab.utoronto.ca/DataSyncRestService/vtpairs;";


        //////////////////////////////////////////////////////////////////////////////
        //						VTPAIRS TABLE AND ATTRIBUTES						//
        //////////////////////////////////////////////////////////////////////////////

        /**
         * Vocabulary-Tag Pairs table name.
         */
        public static final String TABLE = "vocabTagPairs";

        /**
         * ID of the WordTag.
         */
        public static final String DEVICE_ID = "dVocabTagPairID";

        /**
         * ID of the WordTag on the server.
         */
        public static final String SERVER_ID = "sVocabTagPairID";

        /**
         * ID of the Word.
         */
        public static final String WORD_ID = "vDeviceID";

        /**
         * ID of the Word on the server.
         */
        public static final String WORD_SID = "vServerID";

        /**
         * ID of the Tag.
         */
        public static final String TAG_ID = "tDeviceID";

        /**
         * ID of the Tag on the server.
         */
        public static final String TAG_SID = "tServerID";

        /**
         * Owner of the Word and Tag.
         */
        public static final String USER_ID = "userID";

        /**
         * Deleted status of the Word Tag Pair.
         */
        public static final String DELETED = "deleted";

        /**
         * Modified date of the Word Tag Pair.
         */
        public static final String DATE_MODIFIED = "dateModified";


        /**
         * Word-Tag projection.
         */
        public static final String[] PROJECTION = {
                DEVICE_ID + " AS " + _ID,
                SERVER_ID,
                WORD_ID,
                WORD_SID,
                TAG_ID,
                TAG_SID,
                USER_ID,
                DELETED,
                DATE_MODIFIED
        };


        /**
         * Columns that cannot be empty when inserting into the database.
         */
        public static final String[] CONSTRAINTS = {
                USER_ID,
                WORD_ID,
                TAG_ID,
                DELETED,
                DATE_MODIFIED
        };




        //////////////////////////////////////////////////////////////////////////////
        //							CONTENT PROVIDERS								//
        //////////////////////////////////////////////////////////////////////////////

        //static Uri.Builder builder = UserManager.getUri().buildUpon();

        /**
         * URI references all Vocab-Tag pairs.
         */
        //public static Uri CONTENT_URI = builder.appendPath(Contract.WordTag.TABLE).build();

        public static Uri getUri() {
            Uri.Builder builder = UserManager.getUri().buildUpon();
            return builder.appendPath(Contract.WordTag.TABLE).build();
        }


        /**
         * The MIME type of directory of word tags.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.vocabnomad.wordtag";


        /**
         * The MIME type of a single word tag.
         */
        public static final String CONTENT_WORDTAG_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.vocabnomad.wordtag";
    }



    public static final class View implements BaseColumns {

        /**
         * Vocabulary View table name.
         */
        public static final String TABLE = "VocabView";

        /**
         * Owner of the Vocabulary.
         */
        public static final String USER_ID = "userID";

        /**
         * Word ID.
         */
        public static final String WORD_ID = "deviceID";

        /**
         * Word Server ID.
         */
        public static final String SERVER_ID = "serverID";

        /**
         * String representation of the word.
         */
        public static final String ENTRY = "entry";

        /**
         * Sentence usage of the word.
         */
        public static final String SENTENCE = "sentence";

        /**
         * Definition of the word.
         */
        public static final String DEFINITION = "definition";

        /**
         * Image file path of the word.
         */
        public static final String IMG = "img";

        /**
         * Indicates if the word is shared or not.
         */
        public static final String SHARED = "shared";

        /**
         * Weight of the word.
         */
        public static final String WEIGHT = "weight";

        /**
         * Indicates if the word is deleted or not.
         */
        public static final String DELETED = "deleted";

        /**
         * Date the word was added.
         */
        public static final String DATE_ADDED = "dateAdded";

        /**
         * Modified date of the word and tag.
         */
        public static final String DATE_MODIFIED = "dateModified";

        /**
         * Name of the tag.
         */
        public static final String NAME = "name";

        /**
         * The device ID of the tag.
         */
        public static final String TAG_SERVER_ID = "tServerID";

        /**
         * The server ID of the tag.
         */
        public static final String TAG_ID = "tDeviceID";

        /**
         * The vocab-tag pair deleted status.
         */
        public static final String VTP_DELETED = "vtpDeleted";


        /**
         * Word query projection.
         */
        public static final String[] WORD = {
                ENTRY,
                SENTENCE,
                DEFINITION,
                IMG,
                SHARED,
                USER_ID,
                WORD_ID + " AS _id",
                SERVER_ID,
                DELETED,
                VTP_DELETED,
                WEIGHT,
                DATE_ADDED,
                DATE_MODIFIED
        };


        /**
         * Columns that cannot be empty when inserting into the database.
         */
        public static final String[] CONSTRAINTS = {
                USER_ID
        };


        /**
         * Tag query projection.
         */
        public static final String[] TAG = {
                NAME,
                USER_ID,
                TAG_ID + " AS _id",
                TAG_SERVER_ID,
                DATE_MODIFIED
        };

    }



    public static final class EventTypes implements BaseColumns {

        public static final String TABLE = "eventTypes";

        public static final String ID = "id";

        public static final String NAME = "eventTypeName";

        public static final String DESCRIPTION = "eventTypeDescription";

        public static final String LOGIN = "Login:A";
    }



    public static final class UserEvents implements BaseColumns {

        public static final String URL = "http://vndatasync.taglab.utoronto.ca/DataSyncRestService/userEvents;";

        public static final Uri CONTENT_URI = Uri.parse("content://" +
                AUTHORITY + "/" + UserEvents.TABLE);

        public static final String TABLE = "userEvents";

        public static final String SERVER_ID = "serverID";

        public static final String DEVICE_ID = "deviceID";

        public static final String USER_ID = "userID";

        public static final String TIME = "eventTime";

        public static final String TYPE = "eventTypeID";

        public static final String TIME_ID = "timeID";

        public static final String TAG_SID = "tagID";

        public static final String ACTIVITY = "activityID";

        public static final String LANG_MAP = "langMapID";

        public static final String LOCATION = "locID";

        public static final String VOCAB_SID = "vocID";

        public static final String USER_ENTRY = "userEntry";

        public static final String VOCAB_ID = "vocDID";

        public static final String TAG_ID = "tagDID";

        public static final String LONG = "longitude";

        public static final String LAT = "lat";

        public static final String ALT = "alt";

        public static final String[] PROJECTION = {
                DEVICE_ID + " AS _id",
                SERVER_ID,
                USER_ID,
                TIME,
                TYPE,
                TIME_ID,
                TAG_SID,
                ACTIVITY,
                LANG_MAP,
                LOCATION,
                VOCAB_SID,
                USER_ENTRY,
                VOCAB_ID,
                TAG_ID,
                LONG,
                LAT,
                ALT
        };


        /* User Events */

        public static final String DATA_SYNC = "Data Sync:A";

        public static final String L1_SEARCH = "L1 search:A";

        public static final String L2_SEARCH = "L2 search:A";

        public static final String TAG_SEARCH = "Tag Search:A";

        public static final String ADD_VOC = "Add Voc:A";

        public static final String EDIT_VOC = "Edit Voc:A";

        public static final String DELETE_VOC = "Delete Voc:A";

        public static final String VIEW_VOC = "View Voc:A";

        public static final String VIEW_DETAILED_VOC = "View Detailed Voc:A";

        public static final String LOGIN = "Login:A";

        public static final String ADD_TAGS = "Add tags:A";

        public static final String EDIT_TAGS = "Edit Tags:A";

        public static final String DELETE_TAGS = "Delete Tags:A";

        public static final String CANCEL_VOC_EDIT = "Cancel Voc Edit:A";

        public static final String SAVE_VOC_EDIT = "Save Voc Edit:A";

        public static final String SHARE = "Share:A";

        public static final String UNSHARE = "Unshare:A";

        public static final String IMG_EDIT = "Img Edit:A";

        public static final String JIT_IMG = "JIT Img:A";

        public static final String IMG_LIBRARY = "Img Library:A";

        public static final String IMG_PHOTO = "Img Photo:A";

        public static final String WORD_EDIT = "Word Edit:A";

        public static final String SENTENCE_EDIT = "Sentence Edit:A";

        public static final String DEFINITION_EDIT = "Defn Edit:A";

        public static final String JIT_DEFINITION = "JIT Defn:A";

        public static final String SCROLL_VOC = "Scroll Voc:A";

        public static final String REC_SYN = "Rec Syn:A";

        public static final String VIEW_SYN = "View Syn:A";

        public static final String DISMISS_SYN = "Dismiss Syn:A";

        public static final String HEAR_VOC = "Hear Voc:A";

        public static final String HEAR_SENTENCE = "Hear Sentence:A";

        public static final String HEAR_DEFINITION = "Hear Defn:A";

        public static final String JIT_VOC_REQUEST = "JIT Voc Request:A";

        public static final String RECORD_VOICE = "Record Voice:A";

        public static final String PLAY_RECORDING = "Play Recording:A";
    }



    public static final class Synonyms implements BaseColumns {

        public static final String URL = "http://vndatasync.taglab.utoronto.ca/DataSyncRestService/syn;";

        public static final Uri CONTENT_URI = Uri.parse("content://" +
                AUTHORITY + "/" + Synonyms.TABLE);

        public static final String TABLE = "synonyms";

        public static final String SYN_ID = "synID";

        public static final String ENTRY_SID = "vServerIDEntry";

        public static final String ENTRY_SYN_SID = "vServerIDSyn";

        public static final String USER_ID = "userID";

        public static final String[] PROJECTION = {
                SYN_ID + " AS " + _ID,
                ENTRY_SID,
                ENTRY_SYN_SID,
                USER_ID
        };

    }



    public static final class VoiceClip implements BaseColumns {

        public static final String CREATE_TABLE =
                "CREATE TABLE " + VoiceClip.TABLE + " ("
                + VoiceClip.WORD_ID + " INTEGER PRIMARY KEY, "
                + VoiceClip.LOCATION + " TEXT NOT NULL"
                + ");";

        public static void onCreate(SQLiteDatabase db) {
            Log.w(VoiceClip.class.getName(), "Creating database");
            db.execSQL(CREATE_TABLE);
        }

        public static final String TABLE = "voice";

        public static final String WORD_ID = "wordId";

        public static final String LOCATION = "location";

        public static final String[] PROJECTION = {
                LOCATION
        };

        public static final Uri CONTENT_URI = Uri.parse("content://" +
                AUTHORITY + "/" + VoiceClip.TABLE);

    }


    /**
     *  Database schema for a goal
     */
    public static final class Goals implements BaseColumns {

        public static final String CREATE_TABLE =
                "CREATE TABLE " + Goals.TABLE + " ("
                + Goals.GOAL_ID + " INTEGER PRIMARY KEY, "
                + Goals.GOAL_NAME + " TEXT, "
                + Goals.COMPLETED + " FLOAT NOT NULL DEFAULT 0, "
                + Goals.DELETED + " INTEGER NOT NULL DEFAULT 0, "
                + Goals.ACTIVE + " INTEGER NOT NULL DEFAULT 0, "
                + Goals.PROGRESS + " INTEGER, "
                + Goals.TOTAL + " INTEGER, "
                + Goals.LEVEL + " INTEGER NOT NULL DEFAULT 1, "
                + Goals.USER_ID + " INTEGER NOT NULL"
                + ");";

        public static final String DROP_TABLE =
                "DROP TABLE IF EXISTS " + Goals.TABLE;

        public static void onCreate(SQLiteDatabase db) {
            Log.w(Goals.class.getName(), "Creating database");
            db.execSQL(Goals.CREATE_TABLE);
        }

        public static void onDestroy(SQLiteDatabase db) {
            Log.w(Goals.class.getName(), "Deleting database");
            db.execSQL(Goals.DROP_TABLE);
        }

        public static final String[] PROJECTION = {
                Goals.GOAL_ID,
                Goals.GOAL_NAME,
                Goals.COMPLETED,
                Goals.DELETED,
                Goals.ACTIVE,
                Goals.PROGRESS,
                Goals.TOTAL,
                Goals.LEVEL,
                Goals.USER_ID
        };

        public static final String[] PROJECTION_ID = {
                Goals.GOAL_ID + " AS " + BaseColumns._ID,
                Goals.GOAL_NAME,
                Goals.COMPLETED,
                Goals.DELETED,
                Goals.ACTIVE,
                Goals.PROGRESS,
                Goals.TOTAL,
                Goals.LEVEL,
                Goals.USER_ID
        };

        /**
         * Goals table name.
         */
        public static final String TABLE = "goals";

        /**
         *  The device ID for a tag.
         */
        public static final String GOAL_ID = "goal_id";

        /**
         *  The name of the goal. Can be the name of a tag/category/topic or the name of a
         *  macro-skill (i.e., reading, writing, speaking, listening).
         */
        public static final String GOAL_NAME = "name";

        /**
         *  Ratio of the number completed = total / expired
         */
        public static final String COMPLETED = "completed";

        /**
         *  Boolean flag that determines if a goal has been deleted.
         */
        public static final String DELETED = "deleted";

        /**
         *  Boolean flag that determines if this is an active goal being worked on.
         */
        public static final String ACTIVE = "active";

        /**
         *  The progress_bar that has been made to complete the goal.
         */
        public static final String PROGRESS = "progress";

        /**
         *  The progress_bar needed to complete the goal.
         */
        public static final String TOTAL = "total";

        /**
         *  The level of the goal (i.e., number of iterations)
         */
        public static final String LEVEL = "level";

        /**
         *  The user ID of the learner who set this goal.
         */
        public static final String USER_ID = "userid";

    }


    /**
     *  The estimated level of knowledge a learner has for a particular vocabulary entry.
     *  Vocabulary is currently only added to this table when their associated tags are a 'goal'.
     */
    public static final class VocabLevel implements BaseColumns {

        public static final String CREATE_TABLE =
                "CREATE TABLE " + VocabLevel.TABLE + " ("
                        + VocabLevel.WORD_ID + " INTEGER PRIMARY KEY, "
                        + VocabLevel.LEVEL + " INTEGER NOT NULL DEFAULT 0, "
                        + VocabLevel.FORGET_DATE + " INTEGER NOT NULL DEFAULT 0, "
                        + VocabLevel.INTERVAL_LENGTH + " REAL NOT NULL DEFAULT 0, "
                        + VocabLevel.EF + " REAL NOT NULL DEFAULT 1.8, "
                        + VocabLevel.USER_ID + " INTEGER NOT NULL"
                        + ");";

        public static final String DROP_TABLE =
                "DROP TABLE IF EXISTS " + VocabLevel.TABLE;

        public static void onCreate(SQLiteDatabase db) {
            Log.w(VocabLevel.class.getName(), "Creating database");
            db.execSQL(VocabLevel.CREATE_TABLE);
        }

        public static void onDestroy(SQLiteDatabase db) {
            Log.w(VocabLevel.class.getName(), "Deleting database");
            db.execSQL(VocabLevel.DROP_TABLE);
        }

        public static final String[] PROJECTION = {
                VocabLevel.WORD_ID,
                VocabLevel.LEVEL,
                VocabLevel.FORGET_DATE,
                VocabLevel.INTERVAL_LENGTH,
                VocabLevel.EF,
                VocabLevel.USER_ID
        };

        /**
         * VocabLevel table name.
         */
        public static final String TABLE = "VocabLevel";

        /**
         *  Device ID of a vocabulary entry.
         */
        public static final String WORD_ID = "word_id";

        /**
         *  The date (time in seconds) that learners are expected to forget this vocabulary entry.
         */
        public static final String FORGET_DATE = "forget_date";

        /**
         *  The number of intervals the learner has gone through.
         *  The level increments when they are exposed to an entry after the 'forget date'
         */
        public static final String LEVEL = "level";

        /**
         *  The easiness factor of a vocabulary entry. This can be dynamically changed based
         *  on quizzing learners. Harder entries will have a lower EF and easier ones will
         *  have a higher EF. Should be a float in between 1.1 and 2.5
         *
         *  For now, the default is set in the middle at 1.8
         */
        public static final String EF = "ef";

        /**
         *  The total number of days this level lasts for (i.e., until the new 'forget date')
         *  IL(level) = IL(level-1) * EF
         */
        public static final String INTERVAL_LENGTH = "interval";

        /**
         *  The user ID of the learner's vocab level.
         */
        public static final String USER_ID = "userid";

    }


}

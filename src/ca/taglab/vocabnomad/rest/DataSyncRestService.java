package ca.taglab.vocabnomad.rest;

import android.app.IntentService;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DAO;
import ca.taglab.vocabnomad.db.UserEvents;
import ca.taglab.vocabnomad.types.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DataSyncRestService extends IntentService {
    private static final String TAG = "DataSyncRestService";

    /**
     * Name of the String extra put in the intent.
     * The content will be the JSON array string from the server.
     */
    public static final String CONTENT = "content";

    /**
     * Name of the integer extra put in the intent.
     */
    public static final String TYPE = "type";

    /**
     * Name of the integer extra put in the intent.
     * The method will be either getting or sending.
     */
    public static final String METHOD = "method";

    /**
     * Name of the result receiver put in the intent.
     * The receiver will notify the handler when a sync is done.
     */
    public static final String RECEIVER = "receiver";

    /**
     * The number of objects sent or received at a time.
     */
    public static final int LIMIT = 200;

    /**
     * The different object types.
     */
    public static final int LANGUAGES = 1;
    public static final int VOCAB = 2;
    public static final int TAGS = 3;
    public static final int VTPAIRS = 4;
    public static final int SYNONYMS = 5;
    public static final int USER_EVENTS = 6;

    /**
     * Method type that will be performed.
     */
    public static final String DATE_GET = "get";
    public static final int GET = 1;
    public static final String DATE_SEND = "send";
    public static final int SEND = 2;

    public DataSyncRestService() {
        super("DataSyncRestService");
    }

    /**
     * A convenience class to set up a server refresh.
     * This will update the server entries with updated content from the device.
     */
    public static class Send {
        private Context mContext;
        private int mType;

        public Send(Context context, int type) {
            this.mContext = context;
            this.mType = type;
        }

        public void run() {
            final Intent intent = new Intent(mContext, DataSyncRestService.class);
            intent.putExtra(DataSyncRestService.TYPE, mType);
            intent.putExtra(DataSyncRestService.CONTENT, "");
            intent.putExtra(DataSyncRestService.METHOD, SEND);
            mContext.startService(intent);
        }

    }

    /**
     * A convenience class to set up a device refresh.
     * This will update the local device entries with updated content from the server.
     */
    public static class Refresh {
        private Context mContext;
        private int mType;
        private String mSyncDate;
        private Handler mHandler;

        public Refresh(Context context, int type) {
            this.mContext = context;
            this.mType = type;

            // Get the date this object was last synced
            SharedPreferences settings = context.getSharedPreferences(TAG, 0);
            mSyncDate = settings.getString(UserManager.getUserId() + DATE_GET + this.mType, Contract.DEFAULT_DATE);
            Log.i(TAG, "Sync date: " + mSyncDate);
        }

        /**
         * Run the refresh with a handler that will get a message when each object has finished syncing.
         * @param handler   Completion handler
         */
        public void run(Handler handler) {
            this.mHandler = handler;
            run();
        }

        /**
         * Run the sync refresh.
         */
        public void run() {
            String url = null;
            switch (mType) {
                case LANGUAGES:
                    url = Contract.Language.URL;
                    break;
                case VOCAB:
                    url = Contract.Word.URL;
                    break;
                case TAGS:
                    url = Contract.Tag.URL;
                    break;
                case SYNONYMS:
                    url = Contract.Synonyms.URL;
                    break;
                case VTPAIRS:
                    url = Contract.WordTag.URL;
                    break;
                case USER_EVENTS:
                    url = Contract.UserEvents.URL;
                    break;
            }

            RestService service = new RestService(
                    handler,
                    this.mContext,
                    url,
                    RestService.GET
            );
            service.addHeader("Content-Type", "application/json");

            switch (mType) {
                case VOCAB:
                case SYNONYMS:
                case TAGS:
                case VTPAIRS:
                case USER_EVENTS:
                    service.addParam("userID", Long.toString(UserManager.getUserId()));
                    service.addParam("date", mSyncDate);
                    break;

            }

            service.execute();
        }

        /**
         * This handler is called when a JSON array is returned from the server when performing a get.
         */
        private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message message) {
                JSONArray array, subset;
                Log.i(TAG, (String) message.obj);

                try {
                    array = new JSONArray((String) message.obj);

                    /*
                     * Sync has been successful so update the time that this object was last synced.
                     */
                    SharedPreferences settings = mContext.getSharedPreferences(TAG, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
                    editor.putString(UserManager.getUserId() + DATE_GET + mType, format.format(new Date()));
                    editor.commit();

                    if (array.length() == 0 && mHandler != null) {
                        /*
                         * The device is up-to-date.
                         * Let the caller know that syncing is done.
                         */
                        mHandler.obtainMessage(mType).sendToTarget();
                    }


                    /*
                     * In the case that there is a lot of data being received, insert/update
                     * the data in chunks to avoid memory issues.
                     */
                    subset = new JSONArray();

                    for (int i = 0; i < array.length(); i++) {

                        // Add the object to the subset
                        subset.put(array.getJSONObject(i));

                        if (subset.length() >= LIMIT || i == array.length() - 1) {
                            /*
                             * The subset has either reached its limit, or have reached the end of the data set.
                             * Call the insert/update service to add the content.
                             */
                            final Intent intent = new Intent(mContext, DataSyncRestService.class);
                            intent.putExtra(DataSyncRestService.TYPE, mType);
                            intent.putExtra(DataSyncRestService.CONTENT, subset.toString());

                            if (i == array.length() - 1 && mHandler != null) {
                                /*
                                 * If the a handler was given in run(Handler) and it is the last of the data,
                                 * include the result receiver to notify the caller when the data has been added
                                 * to the database.
                                 */
                                ResultReceiver receiver;
                                receiver = new ResultReceiver(mHandler){
                                    @Override
                                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                                        mHandler.obtainMessage(resultCode).sendToTarget();
                                    }
                                };
                                intent.putExtra(DataSyncRestService.RECEIVER, receiver);
                            }

                            mContext.startService(intent);
                            subset = new JSONArray();
                        }
                    }

                    /*
                     * The next action to complete when this object of mType has finished syncing.
                     */
                    switch (mType) {
                        case VOCAB:
                            new DataSyncRestService.Send(mContext, VOCAB).run();
                            new DataSyncRestService.Refresh(mContext, SYNONYMS).run();
                            new DataSyncRestService.Refresh(mContext, TAGS).run(mHandler);
                            break;
                        case TAGS:
                            new DataSyncRestService.Send(mContext, TAGS).run();
                            new DataSyncRestService.Refresh(mContext, VTPAIRS).run(mHandler);
                            break;
                        case VTPAIRS:
                            new DataSyncRestService.Send(mContext, VTPAIRS).run();
                            new DataSyncRestService.Refresh(mContext, USER_EVENTS).run();
                            break;
                        case USER_EVENTS:
                            new DataSyncRestService.Send(mContext, USER_EVENTS).run();
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

    }


    private int type;
    private String content;
    private ResultReceiver receiver;


    @Override
    protected void onHandleIntent(Intent intent) {
        int method = intent.getIntExtra(METHOD, GET);

        type = intent.getIntExtra(TYPE, 0);
        content = intent.getStringExtra(CONTENT);
        receiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER);

        Log.i(TAG, content);

        if (type <= 0) {
            /* Unhandled type */
            return;
        }

        try {

            if (method == GET && !TextUtils.isEmpty(content)) {
                refresh();
            }

            if (method == SEND) {
                send();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Send updated data to the server.
     * @throws Exception
     */
    private void send() throws Exception {
        String url;
        Cursor cursor;

        // Get the date this object was last synced.
        SharedPreferences settings = getApplicationContext().getSharedPreferences(TAG, 0);
        long mSyncDate = settings.getLong(DATE_SEND + this.type, 0);
        Log.i(TAG, "Send sync date: " + "\\/Date(" + mSyncDate + "-0500)\\/");

        /*
         * Get the URL for this object type.
         * Get a cursor with all of the objects that have been updated since the last sync time.
         */
        switch (type) {
            case VOCAB:
                url = Contract.Word.URL;
                cursor = getContentResolver().query(
                        Contract.Word.getUri(),
                        Contract.Word.PROJECTION,
                        Contract.Word.DATE_MODIFIED + ">=?",
                        new String[] {"/Date(" + mSyncDate + "-0500)/"},
                        null
                );
                break;
            case TAGS:
                url = Contract.Tag.URL;
                cursor = getContentResolver().query(
                        Contract.Tag.getUri(),
                        Contract.Tag.PROJECTION,
                        Contract.Tag.DATE_MODIFIED + ">=?",
                        new String[] {"/Date(" + mSyncDate + "-0500)/"},
                        null
                );
                break;
            case VTPAIRS:
                url = Contract.WordTag.URL;
                cursor = getContentResolver().query(
                        Contract.WordTag.getUri(),
                        Contract.WordTag.PROJECTION,
                        Contract.WordTag.DATE_MODIFIED + ">=?",
                        new String[] {"/Date(" + mSyncDate + "-0500)/"},
                        null
                );
                break;
            case USER_EVENTS:
                url = Contract.UserEvents.URL;
                cursor = getContentResolver().query(
                        Contract.UserEvents.CONTENT_URI,
                        Contract.UserEvents.PROJECTION,
                        Contract.UserEvents.TIME + ">=?",
                        new String[] { "/Date(" + mSyncDate + "-0500)/"},
                        null
                );
                break;

            default:
                throw new Exception("Unknown type specified");
        }

        DAO object;
        JSONArray array = new JSONArray();

        if (cursor.moveToFirst()) {
            int count = 0;

            /*
             * Create a JSON array with the updated objects to send to the server.
             */
            while (!cursor.isAfterLast()) {

                switch (type) {
                    case VOCAB:
                        object = new Word(cursor);
                        Log.i(TAG, "Updated word: " + object.toString());
                        break;
                    case TAGS:
                        object = new Tag(cursor);
                        Log.i(TAG, "Updated tag: " + object.toString());
                        break;
                    case VTPAIRS:
                        object = new WordTag(cursor);
                        Log.i(TAG, "Updated word-tag pair: " + object.getId());
                        break;
                    case USER_EVENTS:
                        object = new UserEvents(cursor);
                        Log.i(TAG, "Updated userEvent: " + object.getId());
                        break;
                    default:
                        throw new Exception("Unknown type specified");
                }

                array.put(object.getJSONObject());

                /*
                 * Send the data in chunks.
                 * If the JSON array has more than the limit, send it to the server.
                 */
                if (++count % LIMIT == 0 || count == cursor.getCount()) {
                    RestService service = new RestService(
                            new Handler(),
                            getApplicationContext(),
                            url,
                            RestService.PUT
                    );
                    service.addHeader("Content-Type", "application/json");
                    service.addParam("userID", Long.toString(UserManager.getUserId()));
                    service.addParam("date", Contract.DEFAULT_DATE);
                    service.setEntity(array.toString());
                    service.execute();

                    Log.i(TAG, array.toString());
                    array = new JSONArray();
                }

                cursor.moveToNext();
            }

            /*
             * Update the sync date.
             * TODO: Use RestService handler to determine if the object has actually been synced before updating date.
             */
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(DATE_SEND + type, new Date().getTime());
            editor.commit();

        }

        cursor.close();

    }

    /**
     * Insert/Update data from the server into the local database.
     * @throws Exception
     */
    private void refresh() throws Exception {
        ContentValues values;
        long id;
        Cursor cursor = null;
        DAO object;

        JSONArray array = new JSONArray(content);
        for (int i = 0; i < array.length(); i++) {

            switch (type) {

                case LANGUAGES:
                    object = new Language(array.getJSONObject(i));
                    break;

                case VOCAB:
                    id = array.getJSONObject(i).getLong(Contract.Word.WORD_ID);
                    if (id > 0) {
                        /*
                         * The object already exists in the local database.
                         * In this case, we get our existing object from the database, and update its data
                         * to that of the JSON object.
                         */
                        cursor = getContentResolver().query(
                                ContentUris.withAppendedId(Contract.Word.getUri(), id),
                                Contract.Word.PROJECTION,
                                null, null, null
                        );
                        cursor.moveToFirst();
                        object = new Word(cursor);
                        object.setJSONObject(array.getJSONObject(i));

                        /*
                         * If data from the word changes, the Vocab-Tag pair must reflect those changes.
                         */
                        values = new ContentValues();
                        values.put(Contract.WordTag.WORD_SID, object.values.getAsLong(Contract.Word.SERVER_ID));
                        values.put(Contract.WordTag.DATE_MODIFIED, "/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
                        getContentResolver().update(
                                Contract.WordTag.getUri(),
                                values,
                                Contract.WordTag.WORD_ID + "=?",
                                new String[] { object.values.getAsString(Contract.Word.WORD_ID) }
                        );
                    } else {
                        /*
                         * The word is just being added to the database.
                         * Remove the word ID of 0, so that it gets a real ID when inserting it.
                         */
                        object = new Word(array.getJSONObject(i));
                        object.values.remove(Contract.Word.WORD_ID);
                    }
                    break;

                case TAGS:
                    /*
                     * Fill the tag with the data from the JSON object.
                     */
                    object = new Tag(array.getJSONObject(i));

                    if (object.values.getAsLong(Contract.Tag.TAG_ID) <= 0) {
                        /*
                         * The tag does not already exist in the database.
                         * Remove the tag ID of 0, so it can get a real ID when inserting it.
                         */
                        object.values.remove(Contract.Tag.TAG_ID);
                    } else {
                        /*
                         * The tag already exists in the database.
                         * Update all Vocab-Tag pairs on the device to match the data from the updated tag.
                         */
                        values = new ContentValues();
                        values.put(Contract.WordTag.TAG_SID, object.values.getAsLong(Contract.Tag.SERVER_ID));
                        values.put(Contract.WordTag.DATE_MODIFIED, "/Date(" + Long.toString(new Date().getTime()) + "-0500)/");
                        getContentResolver().update(
                                Contract.WordTag.getUri(),
                                values,
                                Contract.WordTag.TAG_ID + "=?",
                                new String[] { object.values.getAsString(Contract.Tag.TAG_ID) }
                        );
                    }
                    break;

                case SYNONYMS:
                    object = new Synonym(array.getJSONObject(i));
                    break;

                case VTPAIRS:
                    id = array.getJSONObject(i).getLong(Contract.WordTag.DEVICE_ID);
                    if (id > 0) {
                        /*
                         * The Vocab-Tag pair already exists.
                         * Get the existing one from the database and update the information on it with that of
                         * the JSON object.
                         */
                        cursor = getContentResolver().query(
                                ContentUris.withAppendedId(Contract.WordTag.getUri(), id),
                                Contract.WordTag.PROJECTION,
                                null, null, null
                        );
                        cursor.moveToFirst();
                        object = new WordTag(cursor);
                        object.setJSONObject(array.getJSONObject(i));
                    } else {
                        /*
                         * The Vocab-Tag pair does not exist in the database.
                         * Remove the ID of 0 so that the VTPair can get a real ID when it is inserted.
                         */
                        object = new WordTag(array.getJSONObject(i));
                        object.values.remove(Contract.WordTag.DEVICE_ID);
                    }
                    break;

                case USER_EVENTS:
                    id = array.getJSONObject(i).getLong(Contract.UserEvents.DEVICE_ID);
                    if (id > 0) {
                        cursor = getContentResolver().query(
                                ContentUris.withAppendedId(Contract.UserEvents.CONTENT_URI, id),
                                Contract.UserEvents.PROJECTION,
                                null, null, null
                        );
                        cursor.moveToFirst();
                        object = new UserEvents(cursor);
                        object.setJSONObject(array.getJSONObject(i));
                    } else {
                        object = new UserEvents(array.getJSONObject(i));
                        object.values.remove(Contract.UserEvents.DEVICE_ID);
                    }
                    break;

                default:
                    throw new Exception("Unknown type specified");
            }

            /*
             * Commit the object's changes to the database.
             * Refresh object so that it has the values of the newly committed object.
             */
            object.refresh(
                    object.commit(getContentResolver()),
                    getContentResolver()
            );

            if (object.getId() > 0) {
                switch (type) {
                    case LANGUAGES:
                        Log.i(TAG, "Language inserted/updated: " + ((Language) object).getLanguage());
                        break;
                    case VOCAB:
                        Log.i(TAG, "Word inserted/updated: " + ((Word) object).getWord());
                        break;
                    case TAGS:
                        Log.i(TAG, "Tag inserted/updated: " + ((Tag) object).getTag());
                        break;
                    case SYNONYMS:
                        Log.i(TAG, "Synonym inserted/updated: " + object.getId());
                        break;
                    case VTPAIRS:
                        Log.i(TAG, "VTPair inserted/updated: " + object.getId());
                        break;
                    case USER_EVENTS:
                        Log.i(TAG, "UserEvents inserted/updated: " + object.getId());
                        break;
                }

                if (cursor != null) cursor.close();
                object.cursor.close();
                object.cursor = null;
            }

        }

        if (receiver != null) {
            /*
             * If a handler was included,
             * notify the caller and inform them that the syncing has completed for this object type.
             */
            receiver.send(type, null);
        }
    }
}

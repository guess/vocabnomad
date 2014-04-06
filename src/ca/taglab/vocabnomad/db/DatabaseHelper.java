package ca.taglab.vocabnomad.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TAG = "DatabaseHelper";

    private static DatabaseHelper mInstance = null;

    // The Android's default system path of your application database
    private static final String DB_PATH = "/data/data/ca.taglab.vocabnomad/databases/";

    // VocabNomad database name
    private static final String DB_NAME = "VocabNomad.sqlite";

    // VocabNomad database version
    private static final int DB_VERSION = 1;

    private SQLiteDatabase database;
    private Context context;


    /**
     * Get an instance of the Database helper to access the local SQLite database.
     *
     * @param c Activity or application Context
     * @return an instance of the database helper
     */
    public static DatabaseHelper getInstance(Context c) {
        if (mInstance == null) {
			/* Using the application context ensures that you don't leak Activity's context. */
            mInstance = new DatabaseHelper(c.getApplicationContext());
        }
        return mInstance;
    }


    /**
     * Constructor takes and keeps a reference of the passed 'context' in order
     * to access the application assets and resources.
     *
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }


    /**
     * Create an empty database on the system and rewrite it with the
     * existing VocabNomad database.
     */
    private void create() throws IOException {
        boolean dbExists = dbExists();

        if (!dbExists) {

            // Create an empty database for the application
            this.getReadableDatabase();
            Log.i(TAG, "VocabNomad database created");

            // Copy the existing VocabNomad database to default location
            this.copy();
        }

    }


    /**
     * Check if the database already exists.
     * This is to avoid re-copying the file each time the application is opened.
     *
     * @return True if database exists, False otherwise.
     */
    public boolean dbExists() {
        File file = new File(DB_PATH + DB_NAME);
        return file.exists();
    }


    /**
     * Copy the database from the local assets folder to the empty database
     * in the system folder, from where it can be accessed and handled.
     */
    private void copy() throws IOException {
        int length;
        byte[] buffer = new byte[1024];

        // Open database from the local assets folder
        InputStream input = context.getAssets().open(DB_NAME);

        // Open the empty database as the output stream
        OutputStream output = new FileOutputStream(DB_PATH + DB_NAME);

        // Transfer the database
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        // Close the streams
        output.flush();
        output.close();
        input.close();
    }


    /**
     * Open the database for read and write access.
     */
    public void open() throws SQLException, IOException {
        int flags = SQLiteDatabase.OPEN_READWRITE;
        String path = DB_PATH + DB_NAME;

        // Create the database if it doesn't exist
        create();

        // Open the database
        database = SQLiteDatabase.openDatabase(path, null, flags);
        Log.i(TAG, "VocabNomad database opened");

        // Create the voice clip table
        try {
            Contract.VoiceClip.onCreate(database);
        } catch (Exception e) {
            // The table already exists. Do nothing.
        }
    }


    public void createGoals() {
        try {
            Contract.Goals.onCreate(database);
        } catch (Exception e) {
            // The table already exists. Do nothing.
        }
    }

    public void deleteGoals() {
        try {
            Contract.Goals.onDestroy(database);
        } catch (Exception e) {
            // The table already exists. Do nothing.
        }
    }


    /**
     * Execute a query and return a cursor with results.
     */
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs,
                        String groupBy, String having, String orderBy) {
        return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }


    /**
     * Execute the provided SQL and return a cursor over the result set.
     */
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return database.rawQuery(sql, selectionArgs);
    }


    /**
     * Update the table with the provided data.
     */
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return database.update(table, values, whereClause, whereArgs);
    }


    /**
     * Insert data into the table with the provided values.
     *
     * @return ID of newly added row.
     */
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return database.insert(table, nullColumnHack, values);
    }


    /**
     * Deleted data from the provided table.
     * Passing null into the whereClause will remove all rows.
     * Passing '1' into the whereClause will remove all rows and return a count.
     *
     * @return Number of rows affected, 0 otherwise.
     */
    public int delete(String table, String whereClause, String[] whereArgs) {
        return database.delete(table, whereClause, whereArgs);
    }


    /**
     * Close the database.
     */
    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
            Log.i(TAG, "VocabNomad database closed");
        }

        mInstance = null;

        super.close();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // No need to do anything
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No need to do anything
    }
}

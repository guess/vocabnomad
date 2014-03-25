package ca.taglab.vocabnomad.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public abstract class DAO {

    private static String TAG = "DAO";


    /**
     * Column name of the primary key for this object
     */
    public abstract String getIdColumnName();


    /**
     * A list of column names for the table of this object
     */
    public abstract String[] getProjection();


    /**
     * A list of column names that cannot be empty in the table
     */
    public abstract String[] getConstraints();


    /**
     * Get the URI that represents this object.
     * @return  object's URI
     */
    public abstract Uri getUri();





    //////////////////////////////////////////////////////////////////////////////
    //							    CONSTRUCTORS								//
    //////////////////////////////////////////////////////////////////////////////


    /**
     * Default constructor for this database object.
     */
    public DAO() {
        values = new ContentValues();
    }


    /**
     * Constructor for this object.
     * Set the values of the object using a cursor.
     * @param cursor    Cursor from the database
     */
    public DAO(Cursor cursor) {
        values = new ContentValues();
        setCursor(cursor);
    }


    /**
     * Constructor for this object.
     * Set the values of this object using a JSON object.
     * @param object    JSON object from the server
     */
    public DAO(JSONObject object) {
        values = new ContentValues();
        setJSONObject(object);
    }





    //////////////////////////////////////////////////////////////////////////////
    //							    UPDATING VALUES								//
    //////////////////////////////////////////////////////////////////////////////


    /**
     * Content values that holds all updated information of the object.
     */
    public ContentValues values;


    /**
     * Set the value of the object at a specific column.
     * @param col       Column name
     * @param value     The value to be set
     */
    public void set(String col, Object value) {

        if (value instanceof String) {
            values.put(col, (String) value);
        }

        else if (value instanceof Integer) {
            values.put(col, (Integer) value);
        }

        else if (value instanceof Long) {
            values.put(col, (Long) value);
        }

        else if (value instanceof Boolean) {
            values.put(col, (Boolean) value);
        }

        else if (value instanceof Double) {
            values.put(col, (Double) value);
        }

        else if (value instanceof Float) {
            values.put(col, (Float) value);
        }

    }


    /**
     * Set the object's unique ID set by the database.
     * @param id    Device ID
     */
    public void setId(long id) {
        if (id > 0) {
            set(getIdColumnName(), id);
        }
    }


    /**
     * Get the content values for the object representing the changes not yet in the database.
     * @return  content values
     */
    public ContentValues getValues() {
        if (cursor != null) {
            /*
             * The object has already exists in the database.
             * Do not need to check table constraints.
             */
            return values;
        }

        try {

            checkConstraints(getConstraints());
            return values;

        } catch(Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }


    /**
     * Check to see if the content values satisfy the table constraints.
     * @param columns       Name of columns that must be specified
     * @throws Exception    If one or more of the constraints are not met
     */
    public void checkConstraints(final String[] columns) throws Exception {
        for (String col : columns) {
            if (!values.containsKey(col)) {
                throw new Exception("The " + col + " cannot be empty");
            }
        }
    }





    //////////////////////////////////////////////////////////////////////////////
    //							    GETTING VALUES								//
    //////////////////////////////////////////////////////////////////////////////


    /**
     * Cursor that holds all information from the database.
     */
    public Cursor cursor;


    /**
     * Set the cursor for this object.
     * @param cursor    Cursor from the database
     */
    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }


    /**
     * Get the value of the object at a specific column.
     * @param col           Column name
     * @param defaultVal    Default value that is returned if no value exists
     * @return              Value located at the indicated column, or defaultVal if the value does not exist
     */
    public Object get(String col, Object defaultVal) {
        int index = getCursorIndex(col);

        if (index >= 0) {

            switch(cursor.getType(index)) {

                case Cursor.FIELD_TYPE_STRING:
                    return cursor.getString(index);

                case Cursor.FIELD_TYPE_INTEGER:
                    return cursor.getInt(index);

                case Cursor.FIELD_TYPE_FLOAT:
                    return cursor.getFloat(index);

                default:
                    // The value is null and does not exist
                    break;
            }
        }

        return defaultVal;
    }


    /**
     * Get the cursor's index of a specific column.
     * @param col   Column name
     * @return      The index of the column, or -1 if it does not exist
     */
    private int getCursorIndex(String col) {
        int index = -1;

        if (cursor != null) {
            index = cursor.getColumnIndex(col);
        }

        return index;
    }


    /**
     * Get the object's unique ID from the database.
     * @return  Device ID
     */
    public long getId() {
        return ((Integer) get(BaseColumns._ID, 0)).longValue();
    }





    //////////////////////////////////////////////////////////////////////////////
    //						        SERVER HELPERS								//
    //////////////////////////////////////////////////////////////////////////////


    /**
     * Set the values for this object using a JSON object from the server.
     * @param object    JSON object
     */
    public void setJSONObject(JSONObject object) {
        Object value;
        String col;
        Iterator keys = object.keys();

        while (keys.hasNext()) {
            col = (String) keys.next();
            value = object.opt(col);

            if (value instanceof String) {
                values.put(col, (String) value);
            }

            else if (value instanceof Integer) {
                values.put(col, (Integer) value);
            }

            else if (value instanceof Long) {
                values.put(col, (Long) value);
            }

            else if (value instanceof Boolean) {
                values.put(col, (Boolean) value);
            }

            else if (value instanceof Double) {
                values.put(col, (Double) value);
            }
        }

    }


    /**
     * Get the JSON object representation of this object.
     * @return  JSON object
     */
    public JSONObject getJSONObject() {
        JSONObject object = new JSONObject();

        if (cursor == null) {
            Log.e(TAG, "Tried to get a JSON object without committing to db first");
            return object;
        }

        try {
            for (String col : cursor.getColumnNames()) {

                int index = cursor.getColumnIndex(col);

                switch(cursor.getType(index)) {

                    case Cursor.FIELD_TYPE_STRING:
                        object.put(col, cursor.getString(index));
                        break;

                    case Cursor.FIELD_TYPE_INTEGER:
                        if (col.equals("_id")) {
                            object.put(getIdColumnName(), cursor.getLong(index));
                        } else {
                            object.put(col, cursor.getInt(index));
                        }
                        break;

                    case Cursor.FIELD_TYPE_FLOAT:
                        object.put(col, cursor.getFloat(index));
                        break;

                    default:
                        Log.e(TAG, "Value at column \'" + col + "\' was not accounted for");
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }





    //////////////////////////////////////////////////////////////////////////////
    //						    DATABASE HELPERS								//
    //////////////////////////////////////////////////////////////////////////////


    /**
     * Insert the object into the database.
     * If the object has already been inserted, update it.
     * @param resolver  Content resolver
     * @return true if the object was committed, false otherwise
     * @throws Exception    when the object's values do not satisfy the table constraints
     */
    public Uri commit(ContentResolver resolver) throws Exception {
        int affected = 0;
        Uri uri;

        try {

            /* Insert the object into the database */
            uri = resolver.insert(getUri(), getValues());
            affected = 1;
            Log.i("DAO", "Inserted: " + getValues());

        } catch(SQLException e) {

            /* Object is already in the database so update it */
            Log.i(TAG, "UPDATE URI=" + getUri());
            affected = resolver.update(getUri(), getValues(), null, null);
            uri = getUri();

            if (affected > 0) {
                Log.i("DAO", "Updated: " + getValues());
            }

        }

        if (affected > 0) {
            /*
             * The object successfully updated into the database.
             * Reset the content values.
             */
            values = null;
            values = new ContentValues();
        }

        return uri;
    }


    /**
     * Set the object's attributes using the information that is stored in the database.
     * @param uri           URI for the object
     * @param resolver      Content resolver to access the database
     * @return              True if the object was retrieved from the database, False otherwise
     */
    public boolean refresh(Uri uri, ContentResolver resolver) {
        Cursor cursor = resolver.query(uri, getProjection(), null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            setCursor(cursor);
            return true;
        }

        return false;
    }


    /**
     * Delete the object from the database.
     * @param resolver  Content resolver
     * @return true if the object was deleted, false otherwise
     */
    public boolean delete(ContentResolver resolver) {
        int affected = resolver.delete(getUri(), null, null);

        if (affected > 0) {
            cursor = null;
            return true;
        }

        return false;
    }

}

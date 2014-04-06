package ca.taglab.vocabnomad.types;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import java.util.Calendar;
import java.util.Date;

import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;


public class VocabLevel {

    /**
     * Add vocabulary within a set to the vocabulary levels database.
     * @param context   Activity or application context.
     * @param tag      The name of a tag
     */
    public static void addVocabByTag(Context context, String tag) {
        Cursor vocabulary = VocabLevel.getVocabByTag(context, tag);
        if (vocabulary != null) {
            if (vocabulary.moveToFirst()) {
                while (!vocabulary.isAfterLast()) {
                    VocabLevel.addVocab(context,
                            vocabulary.getLong(vocabulary.getColumnIndex(Contract.View.WORD_ID)));
                    vocabulary.moveToNext();
                }
            }
            vocabulary.close();
        }
    }

    /**
     * Add a vocabulary entry to the VocabLevel table
     * @param context   Activity or application context.
     * @param id        Vocabulary device ID
     */
    public static void addVocab(Context context, long id) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        ContentValues values = new ContentValues();
        values.put(Contract.VocabLevel.WORD_ID, id);
        values.put(Contract.VocabLevel.USER_ID, UserManager.getUserId());

        try {
            db.insert(Contract.VocabLevel.TABLE, null, values);
        } catch (SQLiteConstraintException e) {
            // Do nothing, the vocabulary entry already exists
            // TODO: Check to see if the forget date has passed?
        }
    }

    /**
     * Level up a vocabulary entry (i.e., increment interval length, forget date, and level).
     * @param context   Activity or application context.
     * @param id        Vocabulary device ID
     */
    public static void levelUp(Context context, long id) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        int level, interval;

        // Get the current statistics for the vocabulary entry
        Cursor cursor = VocabLevel.getVocabLevel(context, id);
        if (!cursor.moveToFirst()) return;
        ContentValues values = new ContentValues();

        // Increment the level
        level = cursor.getInt(cursor.getColumnIndex(Contract.VocabLevel.LEVEL)) + 1;
        values.put(Contract.VocabLevel.LEVEL, level);

        // Increase the interval length based on the spaced repetition algorithm
        switch (level) {
            case 1:
                interval = 1;
                break;
            case 2:
                interval = 6;
                break;
            default:
                float ef = cursor.getFloat(cursor.getColumnIndex(Contract.VocabLevel.EF));
                long old_interval = cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.INTERVAL_LENGTH));
                interval = Math.round(old_interval * ef);
        }
        cursor.close();
        values.put(Contract.VocabLevel.INTERVAL_LENGTH, interval);

        // Increase the forget date based on the new interval length
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, interval);
        values.put(Contract.VocabLevel.FORGET_DATE, c.getTimeInMillis());

        db.update(Contract.VocabLevel.TABLE, values, Contract.VocabLevel.WORD_ID + "=?",
                new String[]{Long.toString(id)});
    }


    /**
     * Get the VocabLevel statistics for a vocabulary entry
     * @param context   Activity or application context.
     * @param id        Vocabulary device ID
     * @return the VocabLevel stats for the vocabulary entry with the specified device 'id'.
     */
    public static Cursor getVocabLevel(Context context, long id) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        return db.query(
                Contract.VocabLevel.TABLE,
                Contract.VocabLevel.PROJECTION,
                Contract.VocabLevel.WORD_ID + "=?",
                new String[] { Long.toString(id) },
                null, null, null
        );
    }


    /**
     * Get the vocabulary IDs that are at least at 'level'
     * @param context   Activity or application context.
     * @param level     Minimum level of the vocabulary entries
     * @param tag       The name of a tag
     * @return a cursor containing the device IDs of vocabulary at least at 'level'
     */
    public static Cursor getVocabAtMinLevel(Context context, int level, String tag) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        String vocab_level =
                "SELECT " + Contract.VocabLevel.WORD_ID + " "
                + "FROM " + Contract.VocabLevel.TABLE + " "
                + "WHERE " + Contract.VocabLevel.LEVEL + ">=?";

        String vocab_by_tag =
                "SELECT " + Contract.View.WORD_ID + " "
                + "FROM " + Contract.View.TABLE + " "
                + "WHERE " + Contract.View.NAME + "=?";

        String join =
                "SELECT " + Contract.VocabLevel.WORD_ID + " "
                + "FROM (" + vocab_level + ") AS " + Contract.VocabLevel.TABLE + " "
                + "INNER JOIN (" + vocab_by_tag + ") AS " + Contract.View.TABLE + " "
                + "ON " + Contract.VocabLevel.TABLE + "." + Contract.VocabLevel.WORD_ID + "="
                + Contract.View.TABLE + "." + Contract.View.WORD_ID;

        return db.rawQuery(join, new String[]{Integer.toString(level), tag});
    }


    /**
     * Check if the vocabulary entry has passed the forget date.
     * @param context   Activity or application context.
     * @param id        Vocabulary device ID
     * @return True if the vocabulary entry has passed the forget date, False otherwise.
     */
    public static boolean hasPassedForgetDate(Context context, long id) {
        boolean passed = false;

        Cursor cursor = VocabLevel.getVocabLevel(context, id);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                long forget_date = cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.FORGET_DATE));
                passed = new Date().getTime() > forget_date;
            }
            cursor.close();
        }

        return passed;
    }


    /**
     * Return a cursor containing the vocabulary associated with a 'tag'.
     * @param context   Activity or application context.
     * @param tag       The name of a tag
     * @return a cursor containing the vocabulary associated with a 'tag'.
     */
    private static Cursor getVocabByTag(Context context, String tag) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        return db.query(
                Contract.View.TABLE,
                new String[] { Contract.View.WORD_ID },
                Contract.View.NAME + "=?",
                new String[] { tag },
                null, null, null
        );
    }

}

package ca.taglab.vocabnomad.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.types.VocabLevel;

public class VocabLevelTest extends AndroidTestCase {
    private DatabaseHelper db;

    @Override
    protected void setUp() throws Exception {
        db = DatabaseHelper.getInstance(getContext());
        db.open();
        db.deleteVocabLevelTable();
        db.createVocabLevelTable();

        UserManager.login(getContext());
    }

    public void testAddVocabByTag() throws Exception {
        VocabLevel.addVocabByTag(getContext(), "beer");

        Cursor cursor = db.query(
                Contract.VocabLevel.TABLE,
                Contract.VocabLevel.PROJECTION,
                null, null, null, null, null
        );

        assertNotNull(cursor);
        assertTrue(cursor.getCount() > 1);

        cursor.close();
    }

    public void testAddVocab() throws Exception {
        VocabLevel.addVocab(getContext(), getVocabId("church"));

        Cursor cursor = db.query(
                Contract.VocabLevel.TABLE,
                Contract.VocabLevel.PROJECTION,
                null, null, null, null, null
        );

        assertNotNull(cursor);
        cursor.moveToFirst();

        assertEquals(getVocabId("church"), cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.WORD_ID)));
        assertEquals(0, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.LEVEL)));
        assertEquals(0, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.FORGET_DATE)));
        assertEquals(0, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.INTERVAL_LENGTH)));
        assertEquals((float) 1.8, cursor.getFloat(cursor.getColumnIndex(Contract.VocabLevel.EF)));
        assertEquals(UserManager.getUserId(), cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.USER_ID)));

        cursor.close();
    }

    public void testAddExistingVocab() throws Exception {
        // Add a vocabulary entry
        VocabLevel.addVocab(getContext(), getVocabId("church"));

        // Update the entry
        ContentValues values = new ContentValues();
        values.put(Contract.VocabLevel.LEVEL, 2);
        values.put(Contract.VocabLevel.FORGET_DATE, 10);
        values.put(Contract.VocabLevel.INTERVAL_LENGTH, 2);
        values.put(Contract.VocabLevel.EF, 2.5);

        db.update(Contract.VocabLevel.TABLE, values, Contract.VocabLevel.WORD_ID + "=?",
                new String[] { Long.toString(getVocabId("church")) });

        // Try to add the entry again
        VocabLevel.addVocab(getContext(), getVocabId("church"));

        // The fields should not have changed
        Cursor cursor = db.query(
                Contract.VocabLevel.TABLE,
                Contract.VocabLevel.PROJECTION,
                null, null, null, null, null
        );

        assertNotNull(cursor);
        cursor.moveToFirst();

        assertEquals(getVocabId("church"), cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.WORD_ID)));
        assertEquals(2, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.LEVEL)));
        assertEquals(10, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.FORGET_DATE)));
        assertEquals(2, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.INTERVAL_LENGTH)));
        assertEquals((float) 2.5, cursor.getFloat(cursor.getColumnIndex(Contract.VocabLevel.EF)));
        assertEquals(UserManager.getUserId(), cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.USER_ID)));

        cursor.close();
    }

    public void testLevelUp() throws Exception {
        Cursor cursor;
        long date;

        VocabLevel.addVocab(getContext(), getVocabId("church"));

        // Make sure that the values are default
        cursor = VocabLevel.getVocabLevel(getContext(), getVocabId("church"));
        assertNotNull(cursor);
        cursor.moveToFirst();
        assertEquals(0, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.LEVEL)));
        assertEquals(0, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.INTERVAL_LENGTH)));
        assertEquals(0, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.FORGET_DATE)));
        cursor.close();


        // Increment to level 1
        VocabLevel.levelUp(getContext(), getVocabId("church"));
        cursor = VocabLevel.getVocabLevel(getContext(), getVocabId("church"));
        assertNotNull(cursor);
        cursor.moveToFirst();
        assertEquals(1, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.LEVEL)));
        assertEquals(1, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.INTERVAL_LENGTH)));
        date = cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.FORGET_DATE));
        assertNotSame(0, date);
        cursor.close();


        // Increment to level 2
        VocabLevel.levelUp(getContext(), getVocabId("church"));
        cursor = VocabLevel.getVocabLevel(getContext(), getVocabId("church"));
        assertNotNull(cursor);
        cursor.moveToFirst();
        assertEquals(2, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.LEVEL)));
        assertEquals(6, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.INTERVAL_LENGTH)));
        assertNotSame(date, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.FORGET_DATE)));
        date = cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.FORGET_DATE));
        cursor.close();

        // Increment to level 3
        VocabLevel.levelUp(getContext(), getVocabId("church"));
        cursor = VocabLevel.getVocabLevel(getContext(), getVocabId("church"));
        assertNotNull(cursor);
        cursor.moveToFirst();
        assertEquals(3, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.LEVEL)));
        assertEquals(11, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.INTERVAL_LENGTH)));
        assertNotSame(date, cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.FORGET_DATE)));
        cursor.close();
    }

    public void testGetVocabAtMinLevel() throws Exception {
        Cursor cursor;

        // Add vocabulary to the VocabLevel table
        VocabLevel.addVocabByTag(getContext(), "places");

        // Make sure that there are no entries with at level 1
        cursor = VocabLevel.getVocabAtMinLevel(getContext(), 1, "places");
        assertNotNull(cursor);
        assertFalse(cursor.moveToFirst());
        cursor.close();

        // Level up a vocabulary entry to level 1
        VocabLevel.levelUp(getContext(), getVocabId("church"));

        cursor = VocabLevel.getVocabAtMinLevel(getContext(), 1, "places");
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(getVocabId("church"), cursor.getLong(cursor.getColumnIndex(Contract.VocabLevel.WORD_ID)));
        cursor.close();
    }

    public void testHasPassedForgetDate() throws Exception {
        // Add a vocabulary entry to the VocabLevel table
        VocabLevel.addVocab(getContext(), getVocabId("church"));
        assertTrue(VocabLevel.hasPassedForgetDate(getContext(), getVocabId("church")));

        // Level up the vocabulary entry
        VocabLevel.levelUp(getContext(), getVocabId("church"));
        assertFalse(VocabLevel.hasPassedForgetDate(getContext(), getVocabId("church")));
    }


    /**
     * Get the device ID for a vocabulary entry.
     * @param vocab The name of the vocabulary entry
     * @return the device ID for the vocabulary entry.
     */
    private long getVocabId(String vocab) {
        long id = 0;
        Cursor cursor = getContext().getContentResolver().query(
                Contract.Word.getUri(),
                Contract.Word.PROJECTION,
                Contract.Word.ENTRY + "=?",
                new String[] { vocab },
                null, null
        );

        if (cursor != null) {
            cursor.moveToFirst();
            id = cursor.getLong(cursor.getColumnIndex(Contract.Word._ID));
            cursor.close();
        }

        return id;
    }

}

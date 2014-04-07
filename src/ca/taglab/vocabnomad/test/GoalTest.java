package ca.taglab.vocabnomad.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.types.Goal;


public class GoalTest extends AndroidTestCase {
    private DatabaseHelper db;

    // Make sure these tags exist within the system or the tests won't work!
    private static final String TAG_NAME = "beer";
    private static final String TAG_NAME_2 = "alcoholic";
    private static final String TAG_NAME_3 = "non-alcoholic";

    @Override
    protected void setUp() throws Exception {
        db = DatabaseHelper.getInstance(getContext());
        db.open();
        db.deleteGoalTable();
        db.createGoalTable();

        UserManager.login(getContext());
    }

    public void testAddGoal() throws Exception {

        // The method to test:
        Goal.addGoal(getContext(), getTagId(TAG_NAME), TAG_NAME);

        // Check to see if the goal was actually added
        Cursor cursor = Goal.getGoal(getContext(), TAG_NAME);

        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());

        cursor.moveToFirst();
        assertEquals(TAG_NAME, cursor.getString(cursor.getColumnIndex(Contract.Goals.GOAL_NAME)));
        assertEquals(getTagId(TAG_NAME), cursor.getLong(cursor.getColumnIndex(Contract.Goals.GOAL_ID)));
        assertEquals(1, cursor.getLong(cursor.getColumnIndex(Contract.Goals.ACTIVE)));
        assertEquals(0, cursor.getLong(cursor.getColumnIndex(Contract.Goals.DELETED)));
        assertEquals(0, cursor.getLong(cursor.getColumnIndex(Contract.Goals.COMPLETED)));
        assertEquals(UserManager.getUserId(), cursor.getLong(cursor.getColumnIndex(Contract.Goals.USER_ID)));
        assertEquals(0, cursor.getLong(cursor.getColumnIndex(Contract.Goals.PROGRESS)));
        assertNotSame(0, cursor.getLong(cursor.getColumnIndex(Contract.Goals.TOTAL)));

        // Check to see if the goal is active
        assertTrue(Goal.isActiveGoal(getContext(), TAG_NAME));

        cursor.close();
    }

    public void testDeleteGoal() throws Exception {
        Goal.addGoal(getContext(), getTagId(TAG_NAME), TAG_NAME);
        assertTrue(Goal.isActiveGoal(getContext(), TAG_NAME));

        Goal.deleteGoal(getContext(), TAG_NAME);
        assertFalse(Goal.isActiveGoal(getContext(), TAG_NAME));

        Cursor cursor = Goal.getGoal(getContext(), TAG_NAME);
        assertNotNull(cursor);

        cursor.moveToFirst();
        assertEquals(1, cursor.getLong(cursor.getColumnIndex(Contract.Goals.DELETED)));
        assertEquals(0, cursor.getLong(cursor.getColumnIndex(Contract.Goals.ACTIVE)));

        cursor.close();
    }

    public void testUpdateGoal() throws Exception {
        Goal.addGoal(getContext(), getTagId(TAG_NAME), TAG_NAME);
        assertTrue(Goal.isActiveGoal(getContext(), TAG_NAME));

        // Update the goal to be level 2
        ContentValues values = new ContentValues();
        values.put(Contract.Goals.LEVEL, 2);
        db.update(
                Contract.Goals.TABLE,
                values,
                Contract.Goals.GOAL_NAME + "=?",
                new String[] { TAG_NAME }
        );

        // Delete the goal
        Goal.deleteGoal(getContext(), TAG_NAME);
        assertFalse(Goal.isActiveGoal(getContext(), TAG_NAME));

        // Re-add the goal
        Goal.addGoal(getContext(), getTagId(TAG_NAME), TAG_NAME);
        assertTrue(Goal.isActiveGoal(getContext(), TAG_NAME));

        // Make sure that the goal was updated and not added
        Cursor cursor = Goal.getGoal(getContext(), TAG_NAME);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());

        // Ensure that the level is still the same
        cursor.moveToFirst();
        assertEquals(2, cursor.getLong(cursor.getColumnIndex(Contract.Goals.LEVEL)));

        cursor.close();
    }

    public void testGetActiveGoals() throws Exception {
        // Add a bunch of goals
        Goal.addGoal(getContext(), getTagId(TAG_NAME), TAG_NAME);
        Goal.addGoal(getContext(), getTagId(TAG_NAME_2), TAG_NAME_2);
        Goal.addGoal(getContext(), getTagId(TAG_NAME_3), TAG_NAME_3);

        // Delete one
        Goal.deleteGoal(getContext(), TAG_NAME_2);

        Cursor cursor = Goal.getActiveGoals(getContext());
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());

        cursor.moveToFirst();
        assertEquals(TAG_NAME, cursor.getString(cursor.getColumnIndex(Contract.Goals.GOAL_NAME)));

        cursor.moveToNext();
        assertEquals(TAG_NAME_3, cursor.getString(cursor.getColumnIndex(Contract.Goals.GOAL_NAME)));

        cursor.close();
    }

    public void testGetCompletedGoals() throws Exception {
        // Add a couple of goals in the system
        Goal.addGoal(getContext(), getTagId(TAG_NAME), TAG_NAME);
        Goal.addGoal(getContext(), getTagId(TAG_NAME_2), TAG_NAME_2);

        // Complete one of the goals
        Goal.completeGoal(getContext(), TAG_NAME);

        Cursor cursor = Goal.getCompletedGoals(getContext(), 0);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());

        cursor.moveToFirst();
        assertEquals(TAG_NAME, cursor.getString(cursor.getColumnIndex(Contract.Goals.GOAL_NAME)));

        cursor.close();
    }

    public void testIsAtGoalLimit() throws Exception {
        // Should not be at the limit yet
        assertFalse(Goal.isAtGoalLimit(getContext(), 2));

        // Add a couple of goals
        Goal.addGoal(getContext(), getTagId(TAG_NAME), TAG_NAME);
        Goal.addGoal(getContext(), getTagId(TAG_NAME_2), TAG_NAME_2);

        // Should be at the limit now
        assertTrue(Goal.isAtGoalLimit(getContext(), 2));

        // Add another goal. Should still be at the limit
        Goal.addGoal(getContext(), getTagId(TAG_NAME_3), TAG_NAME_3);
        assertTrue(Goal.isAtGoalLimit(getContext(), 2));
    }

    public void testUpdateProgress() throws Exception {
        // TODO: Implement the VocabLevel database first
    }

    private long getTagId(String name) {
        Cursor cursor = db.query(
                Contract.Tag.TABLE,
                Contract.Tag.PROJECTION,
                Contract.Tag.NAME + "=?",
                new String[] { name },
                null, null, null
        );

        long id = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndex(Contract.Tag._ID));
            }
            cursor.close();
        }

        return id;
    }

}

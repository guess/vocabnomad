package ca.taglab.vocabnomad.types;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;

/**
 *  A convenience class to access goals within the system.
 *      Goals being worked on:          active=1, deleted=0
 *      Completed goals:                active=0, deleted=0, completed > 0
 *      Completed but expired goals:    active=0, deleted=0, completed=0
 */
public class Goal {

    /**
     * Get the goals that the user is currently working towards.
     * @param context   Activity or application context.
     * @return a cursor containing the set of the user's goals.
     */
    public static Cursor getActiveGoals(Context context) {
        String selection = "";
        long user = UserManager.getUserId();
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        // Add constraints
        selection = ADD_CONSTRAINT(selection, Contract.Goals.USER_ID, user);
        selection = ADD_CONSTRAINT(selection, Contract.Goals.DELETED, 0);
        selection = ADD_CONSTRAINT(selection, Contract.Goals.ACTIVE, 1);

        return db.query(Contract.Goals.TABLE, Contract.Goals.PROJECTION, selection,
                null, null, null, null);
    }

    /**
     * Get the goals that the user has completed where each goal has at least 'expiration' %
     * of items within the set that are not yet expired.
     *
     * @param context       Activity or application context.
     * @param expiration    Expiration threshold x, where 0 <= x <= 1
     *                      and database values are (# in goal set) / (# of expired items).
     *
     * @return a cursor containing the set of the user's goals.
     */
    public static Cursor getCompletedGoals(Context context, float expiration) {
        String selection;
        long user = UserManager.getUserId();
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        // Add constraints
        selection = Contract.Goals.COMPLETED + ">=" + expiration;
        selection = ADD_CONSTRAINT(selection, Contract.Goals.USER_ID, user);
        selection = ADD_CONSTRAINT(selection, Contract.Goals.DELETED, 0);
        selection = ADD_CONSTRAINT(selection, Contract.Goals.ACTIVE, 0);

        return db.query(Contract.Goals.TABLE, Contract.Goals.PROJECTION, selection,
                null, null, null, null);
    }


    /**
     * Add a goal to the active set.
     * @param context   Activity or application context.
     * @param tagId     Device ID of a tag
     * @param name      The name of a tag
     */
    public static void addGoal(Context context, long tagId, String name) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        if (Goal.isActiveGoal(context, name)) {
            // The goal already exists and is active, do nothing
            return;
        }

        /* Add vocabulary to the vocabulary levels table */
        VocabLevel.addVocabByTag(context, name);

        /*  Generate the values to add to the database.
            For a new goal, the database will set level = 1. Otherwise keep the current level. */
        ContentValues values = new ContentValues();
        values.put(Contract.Goals.GOAL_ID, tagId);
        values.put(Contract.Goals.GOAL_NAME, name);
        values.put(Contract.Goals.ACTIVE, 1);
        values.put(Contract.Goals.DELETED, 0);
        values.put(Contract.Goals.COMPLETED, 0);
        values.put(Contract.Goals.USER_ID, UserManager.getUserId());
        values.put(Contract.Goals.TOTAL, Goal.numItems(context, name));

        // Try to insert the goal
        long id = db.insert(Contract.Goals.TABLE, null, values);
        if (id <= 0) {
            // Goal already exists, try updating the existing entry
            String selection = "";
            selection = ADD_CONSTRAINT(selection, Contract.Goals.USER_ID, UserManager.getUserId());
            selection = ADD_CONSTRAINT(selection, Contract.Goals.GOAL_ID, tagId);
            db.update(Contract.Goals.TABLE, values, selection, null);
        }

        // TODO: Should updating the progress go here? Or should it only go before drawing the view?
        // Update the current progress of the goal
        Goal.updateProgress(context);
    }


    /**
     * Delete the 'goal' from the system (but not actually, just set the deleted column to 1).
     * @param context   Activity or application context.
     * @param goal      The name of the goal
     */
    public static void deleteGoal(Context context, String goal) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        // Create the values to update the database entry
        ContentValues values = new ContentValues();
        values.put(Contract.Goals.DELETED, 1);
        values.put(Contract.Goals.ACTIVE, 0);

        // Update the database
        db.update(Contract.Goals.TABLE, values, Contract.Goals.GOAL_NAME + "=?", new String[] { goal });
    }


    /**
     * Complete the specified goal. Increment the level.
     * @param context   Activity or application context.
     * @param goal      The name of the goal
     */
    public static void completeGoal(Context context, String goal) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        // Update the values of the goal in the database
        ContentValues values = new ContentValues();
        values.put(Contract.Goals.ACTIVE, 0);
        values.put(Contract.Goals.PROGRESS, 0);
        values.put(Contract.Goals.LEVEL, Goal.getGoalLevel(context, goal) + 1);

        db.update(Contract.Goals.TABLE, values, Contract.Goals.GOAL_NAME + "=?", new String[] { goal });
    }


    /**
     * Get the level of the specified 'goal'.
     * @param context   Activity or application context.
     * @param goal      The name of the goal
     * @return the level of the specified goal.
     */
    public static long getGoalLevel(Context context, String goal) {
        long level = 0;

        Cursor cursor = Goal.getGoal(context, goal);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                level = cursor.getLong(cursor.getColumnIndex(Contract.Goals.LEVEL));
            }
            cursor.close();
        }

        return level;
    }


    /**
     * Get a cursor containing the specified 'goal'.
     * @param context   Activity or application context.
     * @param goal      The name of the goal
     * @return a cursor containing the specified 'goal'.
     */
    public static Cursor getGoal(Context context, String goal) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        return db.query(
                Contract.Goals.TABLE,
                Contract.Goals.PROJECTION,
                Contract.Goals.GOAL_NAME + "=?",
                new String[] { goal },
                null, null, null
        );
    }


    /**
     * Update the progress of all active goals.
     * The progress = total # of items that have a level >= goal level.
     * @param context   Activity or application context.
     */
    public static void updateProgress(Context context) {
        // Get the active goals
        Cursor active = Goal.getActiveGoals(context);

        if (active != null) {
            if (active.moveToFirst()) {
                while (!active.isAfterLast()) {
                    String goal = active.getString(active.getColumnIndex(Contract.Goals.GOAL_NAME));
                    int level = active.getInt(active.getColumnIndex(Contract.Goals.LEVEL));
                    Goal.updateProgress(context, goal, level);
                    active.moveToNext();
                }
            }
            active.close();
        }
    }


    /**
     * Update the progress for the 'goal' with the 'level'.
     * @param context   Activity or application context.
     * @param goal      The name of the goal
     * @param level     The level of the goal
     */
    private static void updateProgress(Context context, String goal, int level) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        Cursor cursor = VocabLevel.getVocabAtMinLevel(context, level, goal);

        long progress = 0;
        if (cursor != null) {
            // The progress is the number of vocabulary entries returned
            progress = cursor.getCount();
            cursor.close();
        }

        // Update the progress column in the goal table
        ContentValues values = new ContentValues();
        values.put(Contract.Goals.PROGRESS, progress);
        db.update(Contract.Goals.TABLE, values, Contract.Goals.GOAL_NAME + "=?", new String[] { goal });
    }


    /**
     * Return the number of items contained within the tag set 'name'.
     * TODO: This doesn't belong in this class
     * @param context       Activity or application context.
     * @param name          The name of a tag
     * @return the number of items contained within the set.
     */
    private static long numItems(Context context, String name) {
        int count = 0;
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        Cursor cursor = db.query(Contract.View.TABLE, new String[] { Contract.View.WORD_ID },
                Contract.View.NAME + "=?", new String[] { name }, null, null, null);

        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }


    /**
     * Determine if this goal is active (i.e., currently being completed) or not.
     * @param context   Activity or application context.
     * @param name      The name of a goal
     * @return  True if the user is currently completing this goal. False otherwise.
     */
    public static boolean isActiveGoal(Context context, String name) {
        boolean active = false;
        Cursor cursor = Goal.getActiveGoals(context);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String goal = cursor.getString(cursor.getColumnIndex(Contract.Goals.GOAL_NAME));
                    if (name.equalsIgnoreCase(goal)) active = true;
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }

        return active;
    }


    /**
     * Check if the user has exceeded their limit of active goals
     * @param context   Activity or application context.
     * @param limit     Limit of active goals enforced
     * @return true if they have exceeded the limit, false otherwise.
     */
    public static boolean isAtGoalLimit(Context context, int limit) {
        boolean exceededLimit = false;
        Cursor activeGoals = Goal.getActiveGoals(context);

        if (activeGoals != null) {
            if (activeGoals.getCount() >= limit) exceededLimit = true;
            activeGoals.close();
        }

        return exceededLimit;
    }


    /**
     * Add an equality constraint to the where clause.
     * @param where	Existing where clause
     * @param col	Column name
     * @param val	Value
     * @return		The new where clause
     */
    private static String ADD_CONSTRAINT(String where, String col, Object val) {
        return col + "=" + val + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
    }

}

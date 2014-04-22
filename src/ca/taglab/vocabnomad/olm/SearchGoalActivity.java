package ca.taglab.vocabnomad.olm;


import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.types.Goal;

public class SearchGoalActivity extends ListActivity {
    public static final String ADD_GOAL = "add_goal";
    private boolean isAddingGoal = false;

    private EditText mSearchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.olm_add_goal);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isAddingGoal = bundle.getBoolean(ADD_GOAL, false);
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.search_bg));
            if (!isAddingGoal) actionBar.setTitle("Search Statistics");
            Resources resources = Resources.getSystem();
            if (resources != null) {
                int titleId = resources.getIdentifier("action_bar_title", "id", "android");
                TextView title = (TextView)findViewById(titleId);
                title.setTextColor(Color.parseColor("#99FFFFFF"));
            }
        }

        /* Create the list */
        ListAdapter adapter = new GoalAdapter(
                this,
                R.layout.card_item,
                null,
                new String[] { Contract.Tag.NAME },
                new int[] { R.id.title },
                0
        );
        setListAdapter(adapter);

        /* Filter the list based on the search query */
        mSearchBox = (EditText) findViewById(R.id.goal);
        if (!isAddingGoal) mSearchBox.setHint("Search topic");
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                new SearchGoals().execute(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        /* Go to the detailed tag view when clicking on a list item */
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                TextView item = (TextView) view.findViewById(R.id.title);
                if (item != null && item.getText() != null) {
                    String name = item.getText().toString();
                    if (isAddingGoal) {
                        Goal.addGoal(SearchGoalActivity.this, id, name);
                        finish();
                    } else {
                        Intent intent = new Intent(SearchGoalActivity.this, TagDetailsActivity.class);
                        intent.putExtra(TagDetailsActivity.TAG_NAME, name);
                        startActivity(intent);
                    }
                }
            }
        });

        /* Initially fill the list with nothing but macro-skills */
        setMacroSkills();
        new SearchGoals().execute("");
    }


    /**
     * Draw the macro skill buttons that appear when there is no search query.
     */
    private void setMacroSkills() {
        ViewGroup group;

        group = (ViewGroup) findViewById(R.id.read);
        ((ImageView) group.findViewById(R.id.image)).setImageResource(R.drawable.read);
        ((TextView) group.findViewById(R.id.title)).setText(getString(R.string.reading));
        if (!isAddingGoal) group.setVisibility(View.INVISIBLE);

        group = (ViewGroup) findViewById(R.id.write);
        ((ImageView) group.findViewById(R.id.image)).setImageResource(R.drawable.write);
        ((TextView) group.findViewById(R.id.title)).setText(getString(R.string.writing));
        if (!isAddingGoal) group.setVisibility(View.INVISIBLE);

        group = (ViewGroup) findViewById(R.id.listen);
        ((ImageView) group.findViewById(R.id.image)).setImageResource(R.drawable.listen);
        ((TextView) group.findViewById(R.id.title)).setText(getString(R.string.listening));
        if (!isAddingGoal) group.setVisibility(View.INVISIBLE);

        group = (ViewGroup) findViewById(R.id.speak);
        ((ImageView) group.findViewById(R.id.image)).setImageResource(R.drawable.speak);
        ((TextView) group.findViewById(R.id.title)).setText(getString(R.string.speaking));
        if (!isAddingGoal) group.setVisibility(View.INVISIBLE);
    }


    /*
     *  If there is text in the search box, use the back button to clear it.
     *  Otherwise, use the back button to go back.
     */
    @Override
    public void onBackPressed() {
        if (mSearchBox.getText() != null && !mSearchBox.getText().toString().isEmpty()) {
            mSearchBox.setText("");
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Filter list to show tags matching the search query.
     * If the search query is empty, show the macro skills.
     */
    class SearchGoals extends AsyncTask<String, Void, Cursor> {
        @Override
        protected Cursor doInBackground(String... queries) {
            String query = queries[0];
            if (TextUtils.isEmpty(query)) return null;
            else return getContentResolver().query(
                    Contract.Tag.getUri(),
                    Contract.Tag.PROJECTION,
                    Contract.Tag.NAME + " LIKE \'%" + query + "%\'",
                    null, null
            );
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null) {
                getListView().setVisibility(View.VISIBLE);
                ((CursorAdapter) getListAdapter()).changeCursor(cursor);
            } else {
                // Show the macro skills
                getListView().setVisibility(View.GONE);
            }
        }
    }


    /**
     * Adapter to display the different goals.
     */
    class GoalAdapter extends SimpleCursorAdapter {

        public GoalAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((ImageView) view.findViewById(R.id.image)).setImageResource(R.drawable.tag_normal);
            view.findViewById(R.id.header).setBackgroundResource(R.drawable.white_clickable);
            super.bindView(view, context, cursor);
        }
    }
}

package ca.taglab.vocabnomad.auth;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.rest.RestService;
import ca.taglab.vocabnomad.types.Language;

public class LanguageActivity extends Activity {

    Spinner mLanguageSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.languages);
        mLanguageSelector = (Spinner) findViewById(R.id.language);

        getActionBar().hide();

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestService service = new RestService(
                        new Handler(),
                        getApplicationContext(),
                        Contract.URL
                                + "/userID=" + UserManager.getUserId() + "?langID=" + UserManager.getMotherTongue(),
                        RestService.PUT
                );
                service.addHeader("Content-Type", "application/json");
                service.execute();

                setResult(RESULT_OK);
                finish();
            }
        });

        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(
                getApplicationContext(),
                R.layout.language_item,
                getContentResolver().query(
                        Contract.Language.CONTENT_URI,
                        Contract.Language.PROJECTION,
                        null, null,
                        Contract.Language.LANGUAGE
                ),
                new String[] { Contract.Language.LANGUAGE },
                new int[] { android.R.id.text1 },
                0

        );

        mAdapter.setDropDownViewResource(R.layout.language_item);

        mLanguageSelector.setAdapter(mAdapter);

        mLanguageSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                findViewById(R.id.submit).setVisibility(View.VISIBLE);
                setMotherTongue(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                findViewById(R.id.submit).setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onBackPressed() {
        setMotherTongue(0);
        setResult(RESULT_CANCELED);
        finish();
    }


    /**
     * Set the user's mother tongue in the database and refresh the user.
     * @param id    Mother tongue ID
     */
    private void setMotherTongue(long id) {
        ContentValues values = new ContentValues();
        values.put(Contract.User.MOTHER_TONGUE, id);
        getContentResolver().update(
                ContentUris.withAppendedId(Contract.User.CONTENT_URI, UserManager.getUserId()),
                values,
                null,
                null
        );

        // Refresh the user
        UserManager.login(getApplicationContext());
    }
}

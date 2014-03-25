package ca.taglab.vocabnomad;

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

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                findViewById(R.id.submit).setVisibility(View.GONE);
            }
        });

    }
}

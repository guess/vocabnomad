package ca.taglab.vocabnomad;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ca.taglab.vocabnomad.adapter.VocabAdapter;
import ca.taglab.vocabnomad.auth.LanguageActivity;
import ca.taglab.vocabnomad.auth.LoginActivity;
import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.db.UserEvents;
import ca.taglab.vocabnomad.rest.DataSyncRestService;
import ca.taglab.vocabnomad.rest.RestService;

public class VocabActivity extends ListActivity {
    public static final String TAG = "VocabActivity";

    public static final int USER_LOGIN = 1;
    public static final int LANGUAGE = 2;
    public static final int SEARCH = 3;
    public static final int VIEW_WORD = 4;

    private ViewGroup mProgress;

    private String mFilter;

    private QueryHandler mQueryHandler;
    private CursorAdapter mAdapter;

    /**
     * Handler that is called whenever a sync has finished.
     */
    Handler mSyncFinished = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case DataSyncRestService.VOCAB:
                    Log.d(TAG, "Words finished syncing");
                    break;
                case DataSyncRestService.TAGS:
                    Log.d(TAG, "Tags finished syncing");
                    break;
                case DataSyncRestService.VTPAIRS:
                    Log.d(TAG, "VTPairs finished syncing");
                    break;
            }

            if (msg.what == DataSyncRestService.VTPAIRS) {
                UserEvents.log(
                        Contract.UserEvents.DATA_SYNC,
                        0, 0, 0, 0, null
                );

                resetVocabList();

                hideProgressBar();
            }
        }
    };


    /**
     * Handler that is called whenever a JIT search has finished.
     */
    Handler jitFinishedHandler = new Handler() {

        public void handleMessage(Message msg) {
            // Perform a sync operation to get the new vocabulary
            syncVocab();
        }
    };


    private static final class QueryHandler extends AsyncQueryHandler {
        public static final int TOKEN_WORD = 1;
        public static final int TOKEN_RESET = 2;
        private CursorAdapter mAdapter;
        private Context mContext;

        public QueryHandler(Context context, CursorAdapter adapter) {
            super(context.getContentResolver());
            this.mAdapter = adapter;
            this.mContext = context;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, final Cursor cursor) {
            switch (token) {
                case TOKEN_WORD:
                    mAdapter.swapCursor(cursor);
                    mAdapter.notifyDataSetChanged();
                    break;
                case TOKEN_RESET:
                    mAdapter.swapCursor(cursor);
                    mAdapter.notifyDataSetChanged();
                    ((ListActivity) mContext).getListView().setSelection(0);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vocab_list);

        mProgress = (ViewGroup) findViewById(R.id.progress);
        final Context context = this;
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFilter();
            }
        });

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.search_bg));
            actionBar.setIcon(R.drawable.launch_icon);
            actionBar.setTitle("");
            actionBar.hide();
        }

        // Initialize the databases
        init();

        // Populate the vocabulary list
        mAdapter = new VocabAdapter(this, null, 0, filterHandler);
        mQueryHandler = new QueryHandler(this, mAdapter);
        setListAdapter(mAdapter);
        resetVocabList();
    }

    /**
     * Load the cursor for the vocabulary list in the background.
     */
    private void resetVocabList() {
        mQueryHandler.startQuery(QueryHandler.TOKEN_WORD, null, Contract.Word.getUri(),
                Contract.View.WORD, mFilter, null, Contract.View.ENTRY + " COLLATE NOCASE");
    }

    private void resetVocabList(boolean resetPosition) {
        if (resetPosition) {
            mQueryHandler.startQuery(QueryHandler.TOKEN_RESET, null, Contract.Word.getUri(),
                    Contract.View.WORD, mFilter, null, Contract.View.ENTRY + " COLLATE NOCASE");
        } else {
            resetVocabList();
        }
    }

    Handler filterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            addToFilter((String) msg.obj);
        }
    };

    private void addToFilter(String tag) {
        LinearLayout filter = (LinearLayout) findViewById(R.id.filter);
        findViewById(R.id.bar).setVisibility(View.VISIBLE);

        final TextView newView = (TextView) LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.tag_search, filter, false);
        newView.setText(tag);
        filter.addView(newView, 0);

        if (!TextUtils.isEmpty(mFilter)) {
            mFilter += " OR ";
        } else {
            mFilter = "";
        }

        mFilter += Contract.View.NAME + "=\'" + tag + "\'";

        resetVocabList(true);
    }


    private void clearFilter() {
        LinearLayout filter = (LinearLayout) findViewById(R.id.filter);
        filter.removeAllViewsInLayout();
        findViewById(R.id.bar).setVisibility(View.GONE);

        invalidateOptionsMenu();

        mFilter = null;
        resetVocabList(true);

        hideProgressBar();
    }


    /**
     * Execute a JIT search to find related words to the tag 'name'.
     * @param name  Tag name that the JIT search will query.
     */
    private void jitSearch(String name) {
        Log.i(TAG, "Executing JIT: " + name);
        showProgressBar();

        RestService service = new RestService(
                jitFinishedHandler,
                getApplicationContext(),
                Contract.JIT_URL
                        + "/userid=" + UserManager.getUserId()
                        + "/word=" + name,
                RestService.POST
        );
        service.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vocab_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_add:
                startActivity(new Intent(this, EditWordActivity.class));
                return true;

            case R.id.menu_search:
                startActivityForResult(new Intent(this, VocabSearch.class), SEARCH);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserEvents.log(Contract.UserEvents.VIEW_VOC, 0, 0, 0, 0, null);
        resetVocabList();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(this, ViewWordActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, VIEW_WORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case USER_LOGIN:
                if (resultCode == RESULT_CANCELED) finish();
                Toast.makeText(
                        getApplicationContext(),
                        "Welcome to VocabNomad " + UserManager.getUsername(),
                        Toast.LENGTH_LONG
                ).show();
                syncVocab();
                break;

            case LANGUAGE:
                if (resultCode == RESULT_CANCELED) finish();
                break;

            case VIEW_WORD:
            case SEARCH:
                if (resultCode == RESULT_OK) {
                    String tag = data.getStringExtra("tag");
                    if (data.getBooleanExtra("jit", false)) {
                        jitSearch(tag);
                    }
                    addToFilter(tag);
                }
                break;

        }
    }

    /**
     * Synchronize the vocabulary list with the data on the server.
     */
    private void syncVocab() {
        //if (isNetworkOnline()) {
        if (haveNetworkConnection()) {
            showProgressBar();
            new DataSyncRestService.Refresh(getApplicationContext(), DataSyncRestService.VOCAB)
                    .run(mSyncFinished);
        } else {
            hideProgressBar();
        }
    }

    /**
     * Initialize the backend of the application.
     */
    private void init() {
        try {
            DatabaseHelper.getInstance(getApplicationContext()).open();
        } catch(Exception e) {
            Log.e(TAG, "An error occurred when opening the VocabNomad database");
        }

        // Initialize the user events
        UserEvents.init(getApplicationContext());

        // Check the if the user is logged in
        UserManager.login(getApplicationContext());
        checkUserLogin();
    }

    /**
     * Show the progress bar.
     * Used when loading has been initialized.
     */
    private void showProgressBar() {
        mProgress.setVisibility(View.VISIBLE);
        getListView().setVisibility(View.GONE);
    }

    /**
     * Hide the progress bar.
     * Used when loading has completed.
     */
    private void hideProgressBar() {
        getActionBar().show();
        mProgress.setBackground(null);
        mProgress.setVisibility(View.GONE);
        getListView().setVisibility(View.VISIBLE);
    }


    /**
     * Check if the user has a mother tongue.
     * If not, open the language activity.
     */
    private void checkMotherTongue() {
        if (UserManager.getMotherTongue() <= 0) {
            // The user does not have a mother tongue
            startActivityForResult(new Intent(this, LanguageActivity.class), LANGUAGE);
        }
    }

    /**
     * Check if there is a user currently logged in.
     * If not, open the login activity.
     */
    private void checkUserLogin() {
        if (!UserManager.isLoggedIn()) {
            startActivityForResult(new Intent(this, LoginActivity.class), USER_LOGIN);
        } else {
            checkMotherTongue();
            syncVocab();
            if (getActionBar() != null) getActionBar().setTitle(UserManager.getUsername());
        }
    }


    /**
     * Check the internet connection.
     * @return  True if the network is connected, False otherwise.
     */
    private boolean isNetworkOnline() {
        boolean status = false;

        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getNetworkInfo(0);
            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                networkInfo = cm.getNetworkInfo(1);
                if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    status = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Log.i(TAG, "The user is connected to the internet: " + status);

        return status;
    }


    /**
     * Check network connection on both mobile and wifi
     * @return  True if there is a network connection, False otherwise.
     */
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();

        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }

        return haveConnectedWifi || haveConnectedMobile;
    }
}

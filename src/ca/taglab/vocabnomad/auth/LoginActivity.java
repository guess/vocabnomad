package ca.taglab.vocabnomad.auth;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.rest.DataSyncRestService;
import ca.taglab.vocabnomad.rest.RestService;
import ca.taglab.vocabnomad.types.User;
import org.json.JSONObject;

public class LoginActivity extends Activity {
    public static final String TAG = "LoginActivity";

    private EditText usernameEditText;
    private EditText passwordEditText;

    private String password;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkUserLogin();

        setContentView(R.layout.login);
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
    }


    /**
     * User clicked on the login button.
     * @param button    Login button
     */
    public void login(View button) {
        password = passwordEditText.getText().toString().trim();

        RestService service = new RestService(
                handler,
                getApplicationContext(),
                Contract.User.URL + usernameEditText.getText().toString().trim(),
                RestService.GET
        );

        service.addHeader("Content-Type", "application/json");
        service.execute();
    }



    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


    /**
     * Handler that gets the user JSON string from the server.
     */
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message message) {
            new UserLogin().execute((String) message.obj);
        }
    };


    private void checkUserLogin() {
        if (UserManager.isLoggedIn()) {
            new DataSyncRestService.Refresh(getApplicationContext(), DataSyncRestService.LANGUAGES).run();
            //new DataSyncRestService.Refresh(getApplicationContext(), DataSyncRestService.VOCAB).run();
            setResult(RESULT_OK);
            finish();
        }
    }


    /**
     * Check the given login credentials.
     * Log the user in if they are correct.
     */
    class UserLogin extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                JSONObject object = new JSONObject(params[0]);

                if (object.getString("pwd").equals(password)) {
                    UserManager.addUser(getApplicationContext(), new User(object));
                } else {
                    Log.i(TAG, "Password is incorrect: " + object.getString("pwd"));
                }

            } catch (Exception e) {
                Log.i(TAG, "Username does not exist");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            checkUserLogin();

            if (!UserManager.isLoggedIn()) {
                Toast.makeText(
                        getApplicationContext(),
                        "Username or password is incorrect",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

    }


}



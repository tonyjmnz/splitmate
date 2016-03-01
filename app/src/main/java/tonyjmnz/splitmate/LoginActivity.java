package tonyjmnz.splitmate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements
        LoginFragment.LoginFragmentInteractions, SignUpFragment.SignUpFragmentInteractions{
    ApiWrapper api;
    View fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isLoggedIn()) {
            loadAppActivity();
            return;
        }

        setContentView(R.layout.activity_login);
        api = new ApiWrapper(getApplicationContext());

        fragmentContainer = findViewById(R.id.fragmentContainer);
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (fragmentContainer != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            Fragment loginFragment = new LoginFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, loginFragment).commit();
        }

    }
    private boolean isLoggedIn() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int userId = sharedPref.getInt(getString(R.string.user_id), 0);
        return userId != 0;
    }

    //load app activity and save user Id in sharedPreferences if the user is new
    private void saveUser(int userId) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.user_id), userId);
        editor.commit();
    }

    private void loadAppActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLogin(String email, String password) {
        api.login(email, password,
            new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Utils.responseToast(response, getApplicationContext());
                    try {
                        int userId = Integer.parseInt(response.get("userId").toString());
                        saveUser(userId);
                        if (response.get("status").toString().equals("success")) {
                            loadAppActivity();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub

                }
            }, false);
    }

    @Override
    public void onSignUp() {
        // Create a new Fragment to be placed in the activity layout
        Fragment signUpFragment = new SignUpFragment();

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, signUpFragment).addToBackStack("signup").commit();
    }

    @Override
    public void onSignUp(String email, String password) {
        api.signUp(email, password,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.responseToast(response, getApplicationContext());
                        try {
                            int userId = Integer.parseInt(response.get("userId").toString());
                            if (response.get("status").toString().equals("success")) {
                                saveUser(userId);
                                loadAppActivity();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
    }
}

package com.example.statsforspotify;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Request;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Login extends AppCompatActivity {
    private final String TAG = "LOGIN";
    public static final int AUTH_CODE_REQUEST_CODE = 1;
    public static final String CLIENT_ID = "e2a579852ac54def8988cb84312f9976";
    public static final String REDIRECT_URI = "statsforspotify://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode;
    private Call mCall;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    boolean changeBackground = false;
    final int[] arrLogin = {R.drawable.login_bg_light_00, R.drawable.login_bg_light_01, R.drawable.login_bg_light_02,
            R.drawable.login_bg_light_03, R.drawable.login_bg_light_04, R.drawable.login_bg_light_05,
            R.drawable.login_bg_light_06, R.drawable.login_bg_light_07, R.drawable.login_bg_light_08,
            R.drawable.login_bg_light_09, R.drawable.login_bg_light_10, R.drawable.login_bg_light_11,
            R.drawable.login_bg_light_12, R.drawable.login_bg_light_13, R.drawable.login_bg_light_14,
            R.drawable.login_bg_light_15, R.drawable.login_bg_light_16, R.drawable.login_bg_light_17};

    int arrLoginIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginWithSpotifyButton = findViewById(R.id.login_with_spotify);

        loginWithSpotifyButton.setOnClickListener((v) -> {
            Log.d(TAG, "onCreate: loginWithSpotifyButton clicked");
            getCode();
        });

        ImageView loginView = (ImageView) findViewById(R.id.login_view);
        changeLoginBackground(loginView, loginWithSpotifyButton);
        loginView.setOnClickListener((v) -> {
            changeBackground = !changeBackground;
        });

        ImageButton question = findViewById(R.id.login_question);
        question.setOnClickListener((v) -> {
            Snackbar.make(v, R.string.login_question_icon, Snackbar.LENGTH_LONG).
                    setAction("Action", null).show();
        });
    }

    /**
     * Get code from Spotify
     * This method will open the Spotify login activity and get the code
     * What is code?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        AuthorizationClient.openLoginActivity(Login.this, AUTH_CODE_REQUEST_CODE, request);
    }

    /**
     * Get authentication request
     *
     * @param type the type of the request
     * @return the authentication request
     */
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[] { "user-read-email", "user-top-read" }) // <--- Change the scope of your requested token here
                .setCampaign("your-campaign-token")
                .build();
    }

    /**
     * Gets the redirect Uri for Spotify
     *
     * @return redirect Uri object
     */
    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    public void getToken() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(Login.this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    /**
     * When the app leaves this activity to momentarily get a token/code, this function
     * fetches the result of that external activity to get the response from Spotify
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        // Check which request code is present (if any)
        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();
            Log.d(TAG, "onActivityResult: WE HAVE TOKEN: " + mAccessToken);
            // setTextAsync(mAccessToken, tokenTextView);
            HashMap<String, String> spotifyAuthData = SpotifyAuthData.getInstance();

            spotifyAuthData.put("token", mAccessToken); // store token for later use on different pages after logging in

            final Request request = new Request.Builder()
                    .url("https://api.spotify.com/v1/me")
                    .addHeader("Authorization", "Bearer " + mAccessToken)
                    .build();

            mCall = mOkHttpClient.newCall(request);

            mCall.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d("HTTP", "Failed to fetch data: " + e);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(Login.this, "Failed to fetch data, watch Logcat for more details",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });


//                    Toast.makeText(Login.this, "Failed to fetch data, watch Logcat for more details",
//                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        final JSONObject jsonObject = new JSONObject(response.body().string());

                        String userId = jsonObject.getString("id");

                        spotifyAuthData.put("spotify_id", userId);


                        Map<String, Object> userObj = new HashMap<>();
                        userObj.put("username", jsonObject.getString("display_name"));
                        userObj.put("email", jsonObject.getString("email"));
                        userObj.put("userId", userId);

                        JSONArray arr = (JSONArray) jsonObject.get("images");
                        // todo: make function to choose correct pfp based on dimensions
                        JSONObject pfpObj = arr.getJSONObject(0);
                        userObj.put("pfp", pfpObj.getString("url"));

                        // TODO: if user_id exists, don't insert; right now this will overate any changes user made to their user object in the settings page
//                        db.collection("userdata").document(userId)
//                                .set(userObj);

                        DocumentReference docRef = db.collection("userdata").document(userId);

// Check if the document exists
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        // Document exists
                                        Log.d(TAG, "Document exists, not doing anything...");
                                        // don't do anything
                                    } else {
                                        // Document does not exist
                                        Log.d(TAG, "User does not yet exist, inserting...");
                                        db.collection("userdata").document(userId).set(userObj);
                                    }
                                } else {
                                    // An error occurred
                                    Log.d(TAG, "Failed with: ", task.getException());
                                }
                            }
                        });



                        Intent intent = new Intent( Login.this, WrappedActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(intent);


                    } catch (JSONException e) {
                        Log.d("JSON", "Failed to parse data: " + e);
                        Toast.makeText(Login.this, "Failed to parse data, watch Logcat for more details",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });


            // TODO: add to firebase like before
            // TODO: store token in stpofiy auth singleton class
            // setTextAsync(mAccessCode, codeTextView);


        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            Log.d(TAG, "onActivityResult: we have code: " + mAccessCode);

            getToken();

        }
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    private void changeLoginBackground(ImageView loginView, Button button) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView text = findViewById(R.id.login_text);
                        ImageButton question = findViewById(R.id.login_question);
                        if (changeBackground) {
                            int nextBackground = (arrLoginIndex + 1) % arrLogin.length;
                            int nextBackgroundID = arrLogin[nextBackground];
                            loginView.setBackgroundResource(nextBackgroundID);
                            arrLoginIndex = nextBackground;

                            text.setVisibility(View.GONE);
                            question.setVisibility(View.GONE);
                        } else {
                            loginView.setBackgroundResource(R.drawable.login_bg_light_base);
                            text.setVisibility(View.VISIBLE);
                            question.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(task, 0, 50);
    }
}

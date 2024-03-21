package com.example.statsforspotify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class Login extends AppCompatActivity {
    private final String TAG = "LOGIN";
    public static final int AUTH_CODE_REQUEST_CODE = 1;
    public static final String CLIENT_ID = "e2a579852ac54def8988cb84312f9976";
    public static final String REDIRECT_URI = "statsforspotify://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;

    private String mAccessToken, mAccessCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginWithSpotifyButton = findViewById(R.id.login_with_spotify);

        loginWithSpotifyButton.setOnClickListener((v) -> {
            Log.d(TAG, "onCreate: loginWithSpotifyButton clicked");
            getCode();
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
                .setScopes(new String[] { "user-read-email" }) // <--- Change the scope of your requested token here
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

        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            Log.d(TAG, "onActivityResult: WE HAVE CODE: " + mAccessCode);

            // TODO: add to firebase like before
            // TODO: store token in stpofiy auth singleton class
            getToken();
            // setTextAsync(mAccessCode, codeTextView);
        }
    }
}

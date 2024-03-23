package com.example.statsforspotify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RelatedArtists extends AppCompatActivity {

    HashMap<String, String> spotifyAuthData = SpotifyAuthData.getInstance();
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private final String TAG = "RELATEDARTISTS";

    Button backButtonObj;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatedartists);

        backButtonObj = findViewById(R.id.backButton);

        backButtonObj.setOnClickListener((v) -> {
            Intent intent = new Intent( RelatedArtists.this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
        });

        String range = "short_term";
        // make an API call to find the user's top artist's ID
        final Request topArtistsRequest = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=" + range + "&limit=10&offset=0")
                .addHeader("Authorization", "Bearer " + spotifyAuthData.get("token"))
                .build();

        Call mCall2;
        mCall2 = mOkHttpClient.newCall(topArtistsRequest);

        mCall2.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    final JSONObject jsonObject;

                    jsonObject = new JSONObject(response.body().string());
                    Log.d(TAG, "onResponse: " + jsonObject.toString());

                    JSONArray items = (JSONArray) jsonObject.get("items");

                    // only looking at the top artist
                    JSONObject artistJSON = items.getJSONObject(0);
//                    String artistImg = artistJSON.getJSONArray("images").getJSONObject(1).getString("url");
//                    String artistName = artistJSON.getString("name");
                    String topArtistId = artistJSON.getString("id");

                    Log.d(TAG, "onResponse: topArtistId => " + topArtistId);

                    final Request topArtistsRequest = new Request.Builder()
                            .url("https://api.spotify.com/v1/artists/" + topArtistId + "/related-artists")
                            .addHeader("Authorization", "Bearer " + spotifyAuthData.get("token"))
                            .build();

                    Call mCall3;
                    mCall3 = mOkHttpClient.newCall(topArtistsRequest);

                    mCall3.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                JSONArray items = jsonObject.getJSONArray("artists");

                                Log.d(TAG, "onResponse: JSON => " + jsonObject.toString());
                                String packageName = getPackageName();

                                for (int i = 0; i < 4; i++) {
                                    JSONObject artistJSON = items.getJSONObject(i);
                                    String artistImg = artistJSON.getJSONArray("images").getJSONObject(1).getString("url");
                                    String artistName = artistJSON.getString("name");

                                    int iForIds = i + 1;

                                    int imgViewResId = getResources().getIdentifier("artist_img_" + iForIds, "id", packageName);

                                    int artistNameId = getResources().getIdentifier("top_artist_name_" + iForIds, "id", packageName);

                                    ImageView artistImgView = findViewById(imgViewResId);
                                    TextView artistNameTextView = findViewById(artistNameId);

                                    new ImageLoadTask(artistImg, artistImgView).execute();
                                    setTextAsync(artistName, artistNameTextView);

                                }

                            } catch (JSONException e) {
                                Log.d(TAG, "onResponse: ERROR => " + e.toString());
//                                throw new RuntimeException(e);
                            }

                        }
                    });




                } catch(JSONException e) {
                    Log.d(TAG, "onResponse (artists): " + e.toString());

                }
            }
        });


    }

    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }
}

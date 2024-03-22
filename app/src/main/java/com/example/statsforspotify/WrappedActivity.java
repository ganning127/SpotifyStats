package com.example.statsforspotify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WrappedActivity extends AppCompatActivity {
    private final String TAG = "WRAPPEDACTIVITY";


    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    ImageView pfpImgView;
    TextView welcomeText;

    ImageView trackImg1;

    TextView trackTitle1;

    TextView artist1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrapped);

        pfpImgView = findViewById(R.id.profile_pic);
        welcomeText = findViewById(R.id.welcome_text);
        trackTitle1 = findViewById(R.id.track_title_1);
        artist1 = findViewById(R.id.artist_name_1);
        trackImg1 = findViewById(R.id.track_img_1);



        // populate all the fields
         HashMap<String, String> spotifyAuthData = SpotifyAuthData.getInstance();

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + spotifyAuthData.get("token"))
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: " + e.toString());

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    final JSONObject jsonObject;
                    jsonObject = new JSONObject(response.body().string());
                    JSONArray arr = (JSONArray) jsonObject.get("images");
                    JSONObject pfpObj = arr.getJSONObject(0);



                    String imgURL = pfpObj.getString("url");

                    new ImageLoadTask(imgURL, pfpImgView).execute();

                    String displayName = (String) jsonObject.get("display_name");

                    // TODO: change this to fetch the name from firebase when we have a settings page
                    setTextAsync("Welcome, " + displayName, welcomeText);


                } catch (JSONException e) {
                    Log.d(TAG, "onResponse: json failed to parse");
                }


            }
        });

        String range = "short_term";

        final Request topTracksRequest = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks?time_range=" + range + "&limit=10&offset=0")
                .addHeader("Authorization", "Bearer " + spotifyAuthData.get("token"))
                .build();

        Call mCall1;
        mCall1 = mOkHttpClient.newCall(topTracksRequest);

        mCall1.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    final JSONObject jsonObject;

                    jsonObject = new JSONObject(response.body().string());
                    Log.d(TAG, "onResponse: " + jsonObject.toString());

                    // track 1
                    JSONArray items = (JSONArray) jsonObject.get("items");
                    String packageName = getPackageName();


                    for (int i = 0; i <= 4; i++) {
                        JSONObject track1 = items.getJSONObject(i);
                        String albumCoverImg1 = track1.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");

                        String track1String = track1.getString("name");

                        String artist1String = track1.getJSONArray("artists").getJSONObject(0).getString("name");


                        Log.d(TAG, "onResponse: trackString: " + track1String);
                        Log.d(TAG, "onResponse: artistString: " + artist1String);
                        Log.d(TAG, "onResponse: album_cover_url: " + albumCoverImg1);

                        int iForIds = i + 1;
                        int trackTitleResId = getResources().getIdentifier("track_title_" + iForIds, "id", packageName);

                        int imgViewResId = getResources().getIdentifier("track_img_" + iForIds, "id", packageName);

                        int artistResId = getResources().getIdentifier("artist_name_" + iForIds, "id", packageName);


                        ImageView albumImgView = findViewById(imgViewResId);
                        TextView trackTitleTextView = findViewById(trackTitleResId);
                        TextView artistTextView = findViewById(artistResId);

                        new ImageLoadTask(albumCoverImg1, albumImgView).execute();
                        setTextAsync(track1String, trackTitleTextView);
                        setTextAsync(artist1String, artistTextView);
                    }


                } catch (JSONException e) {
                    Log.d(TAG, "onResponse (tracks): " + e.toString());
                }

            }
        });


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

                    // track 1
                    JSONArray items = (JSONArray) jsonObject.get("items");
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
                } catch(JSONException e) {
                    Log.d(TAG, "onResponse (artists): " + e.toString());

                }
            }
        });


    }

    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

}
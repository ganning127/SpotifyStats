package com.example.statsforspotify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AllWraps extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String TAG = "ALLWRAPS";

    HashMap<String, String> spotifyAuthData = SpotifyAuthData.getInstance();

    ArrayList<JSONObject> dataToRender = new ArrayList<>();

    BaseAdapter itemsAdapter;

    ListView listView;

    Button backButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allwraps);
        listView = findViewById(R.id.allwraps_listview);
        backButton = findViewById(R.id.wraps_back_button);

        backButton.setOnClickListener((v) -> {
            Intent intent = new Intent(AllWraps.this, ProfileActivity.class);
            startActivity(intent);
        });

        // get a list of all of my wraps
        // Assume db is initialized as FirebaseFirestore.getInstance();
        CollectionReference wrapsCollection = db.collection("wraps");

// Define the field and value you want to search for
        String fieldName = "spotify_id";
        String fieldValue = spotifyAuthData.get("spotify_id");

// Create a query to search for documents where the specified field matches the specified value
        Query query = wrapsCollection.whereEqualTo(fieldName, fieldValue);

// Execute the query
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Document found, you can access its data here
                        dataToRender.add(new JSONObject(document.getData()));
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }

                    Log.d(TAG, "onCreate: final dataToRender: " + dataToRender.toString());
                    itemsAdapter = new AllWrapsAdapter(AllWraps.this, dataToRender);
                    listView.setAdapter(itemsAdapter);
                } else {
                    // An error occurred
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });


    }


}


class AllWrapsAdapter extends BaseAdapter {
    Context mContext;

    ArrayList<JSONObject> items;

    String filterKey;
    public AllWrapsAdapter(Context context,  ArrayList<JSONObject> items) {
        mContext = context;
        this.items = items;
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.listview_wrapped_item, viewGroup, false);
        }

        JSONObject tempObj = (JSONObject) getItem(i);

        TextView tvUsername = view.findViewById(R.id.wrap_username);
        TextView tvCreationDate = view.findViewById(R.id.wrap_date);
        TextView tvTrack1 = view.findViewById(R.id.wrap_track_1);
        TextView tvArtist1 = view.findViewById(R.id.wrap_artist_1);
        TextView tvTrack2 = view.findViewById(R.id.wrap_track_2);
        TextView tvArtist2 = view.findViewById(R.id.wrap_artist_2);
        TextView tvTrack3 = view.findViewById(R.id.wrap_track_3);
        TextView tvArtist3 = view.findViewById(R.id.wrap_artist_3);
        TextView tvTrack4 = view.findViewById(R.id.wrap_track_4);
        TextView tvArtist4 = view.findViewById(R.id.wrap_artist_4);
        TextView tvTrack5 = view.findViewById(R.id.wrap_track_5);


        try {
            String username = tempObj.getString("username");
            String creationDate = tempObj.getString("createdAt");

            creationDate = creationDate + " (" + tempObj.getString("term_length") + ")";
            String track1 = tempObj.getJSONArray("tracks").getJSONObject(0).getString("title");
            String artist1 = tempObj.getJSONArray("artists").getJSONObject(0).getString("name");
            String track2 = tempObj.getJSONArray("tracks").getJSONObject(1).getString("title");
            String artist2 = tempObj.getJSONArray("artists").getJSONObject(1).getString("name");
            String track3 = tempObj.getJSONArray("tracks").getJSONObject(2).getString("title");
            String artist3 = tempObj.getJSONArray("artists").getJSONObject(2).getString("name");
            String track4 = tempObj.getJSONArray("tracks").getJSONObject(3).getString("title");
            String artist4 = tempObj.getJSONArray("artists").getJSONObject(3).getString("name");
            String track5 = tempObj.getJSONArray("tracks").getJSONObject(4).getString("title");

            tvUsername.setText(username);
            tvCreationDate.setText(creationDate);

            tvTrack1.setText(track1);
            tvArtist1.setText(artist1);

            tvTrack2.setText(track2);
            tvArtist2.setText(artist2);

            tvTrack3.setText(track3);
            tvArtist3.setText(artist3);
            tvTrack4.setText(track4);
            tvArtist4.setText(artist4);
            tvTrack5.setText(track5);


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


//        tvItemName.setText(tempObj.item);
//
//        tvCompleted.setOnCheckedChangeListener(null);
//
//        tvCompleted.setChecked(tempObj.completed);
//
//        BaseAdapter ref = this;
//
//        tvCompleted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                System.out.println("hit check");
//                items.get(filterKey).get(i).completed = !items.get(filterKey).get(i).completed;
//                ref.notifyDataSetChanged();
//            }
//        });

        return view;
    }
}

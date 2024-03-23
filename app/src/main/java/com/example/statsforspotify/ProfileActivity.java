package com.example.statsforspotify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    EditText nameInputEditText;
    EditText phoneInputEditText;
    TextView emailTextView;

    TextView userIDEditText;

    Button updateButton;

    HashMap<String, String> spotifyAuthData = SpotifyAuthData.getInstance();
    final String TAG = "PROFILEPAGE";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    HashMap<String, Object> userObj;

    Button backButtonPfp;

    Button deleteAccountButton;

    Button viewAllWrapsButton;

    Button viewRelatedArtistsButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameInputEditText = findViewById(R.id.nameInput);
        phoneInputEditText = findViewById(R.id.phoneInput);
        emailTextView = findViewById(R.id.emailTextView);
        userIDEditText = findViewById(R.id.usernameTextView);
        updateButton = findViewById(R.id.update_button);
        backButtonPfp = findViewById(R.id.back_button_pfp);
        deleteAccountButton = findViewById(R.id.delete_account_button);
        viewAllWrapsButton = findViewById(R.id.view_all_wraps_button);
        viewRelatedArtistsButton = findViewById(R.id.view_related_artists_button);

        viewRelatedArtistsButton.setOnClickListener((v) -> {
            Intent intent = new Intent( ProfileActivity.this, RelatedArtists.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
        });

        backButtonPfp.setOnClickListener((v) -> {

            Intent intent = new Intent( ProfileActivity.this, WrappedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);

        });

        viewAllWrapsButton.setOnClickListener((v) -> {
            Intent intent = new Intent( ProfileActivity.this, AllWraps.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);

        });

        deleteAccountButton.setOnClickListener((v) -> {

            // delete document from firestore DB

            String userId = spotifyAuthData.get("spotify_id");
            db.collection("userdata").document(userId).delete();

            // redirect back to login
            Intent intent = new Intent( ProfileActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);



        });

        updateButton.setOnClickListener((v) -> {
            // make sure name and phone are not empty

            String nameFromUser = nameInputEditText.getText().toString();
            String phoneFromUser = phoneInputEditText.getText().toString();

            if (!nameFromUser.equals("") && !phoneFromUser.equals("")) {
                userObj.put("name", nameFromUser);
                userObj.put("phone", phoneFromUser);

                db.collection("userdata").document(spotifyAuthData.get("spotify_id")).set(userObj); // auto generate id

                Toast.makeText(getApplicationContext(), "Information updated!", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getApplicationContext(), "Fields fields cannot be empty", Toast.LENGTH_LONG).show();
            }
        });

        // find document in firestore with the given userID

        CollectionReference wrapsCollection = db.collection("userdata");

// Define the user ID you want to search for
        String userId = spotifyAuthData.get("spotify_id");

// Create a query to search for documents where the user ID field matches the specified user ID
        Query query = wrapsCollection.whereEqualTo("userId", userId);

// Execute the query
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            // Document found, you can access its data here

                            userObj = (HashMap<String, Object>) document.getData();
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            final JSONObject jsonObject = new JSONObject(document.getData());

                            Log.d(TAG, "JSON => " + jsonObject.toString());

                            // in case name is not set
                            try {
                                String name = jsonObject.getString("name");
                                setEditTextAsync(name, nameInputEditText);
                            } catch (JSONException e) {
                                Log.d(TAG, "onComplete: no name");
                            }

                            // in case phone is not set
                            try {
                                String phone = jsonObject.getString("phone");
                                setEditTextAsync(phone, phoneInputEditText);
                            } catch (JSONException e) {
                                Log.d(TAG, "onComplete: no phone");
                            }

                            try {
                                String email = jsonObject.getString("email");
                                String userIDFromJSON = jsonObject.getString("userId");

                                setTextViewAsync(email, emailTextView);
                                setTextViewAsync(userIDFromJSON, userIDEditText);

                            } catch (JSONException e) {
                                Log.d(TAG, "onComplete: oops we died");
                            }


                        }
                    } else {
                        // No document found
                        Log.d(TAG, "No such document");
                    }
                } else {
                    // An error occurred
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    private void setEditTextAsync(final String text, EditText editText) {
        runOnUiThread(() -> editText.setText(text));
    }

    private void setTextViewAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }

}

package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ShowObject extends AppCompatActivity {

    private ImageView objectPhotoView, profileImageView;
    private TextView emailView, dateView, descriptionView;

    private Button showOnMap;
    //public static String nameUser,profileIUriUser;

    private Double lon,lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_object);


        objectPhotoView = findViewById(R.id.object_image_ob);
        profileImageView = findViewById(R.id.profile_image_ob);
        emailView = findViewById(R.id.email);
        dateView = findViewById(R.id.date);
        descriptionView = findViewById(R.id.description);


        getUsersInformation(savedInstanceState);


        showOnMap = (Button) findViewById(R.id.showOnMap);
        showOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),Maps.class);
                i.putExtra("Longitude",lon);
                i.putExtra("Latitude",lat);
                startActivity(i);
            }
        });

    }


    private void getUsersInformation(Bundle savedInstanceState)
    {
        String newString;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                newString = null;
            } else {
                newString = extras.getString("object_id");
            }
        } else {
            newString = (String) savedInstanceState.getSerializable("object_id");
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference baza = ref.child("my-objects").child(newString).child("imgUri");

        baza.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profImgUri = dataSnapshot.getValue(String.class);
                Uri myUri = Uri.parse(profImgUri);
                Picasso.get().load(myUri).into(objectPhotoView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference uidRef = rootRef.child("my-objects").child(newString);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // String mail = dataSnapshot.child("email").getValue(String.class);
                String date = dataSnapshot.child("date").getValue(String.class);
                String description = dataSnapshot.child("description").getValue(String.class);
                String userID = dataSnapshot.child("UserID").getValue(String.class);
                lat = dataSnapshot.child("latitude").getValue(Double.class);
                lon = dataSnapshot.child("longitude").getValue(Double.class);

                dateView.setText(date);
                descriptionView.setText(description);

                getUserInfo(userID);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log.d(TAG, databaseError.getMessage()); //Don't ignore errors!
            }


        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private void getUserInfo(String uid) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference uidRef = rootRef.child("Users").child(uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String nameUser = dataSnapshot.child("fullName").getValue(String.class);
                String profileUriUser = dataSnapshot.child("profileImageUri").getValue(String.class);

                emailView.setText(nameUser);
                Uri myUri = Uri.parse(profileUriUser);

                Picasso.get().load(myUri).into(profileImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Log.d(TAG, databaseError.getMessage()); //Don't ignore errors!
            }
        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);
    }



}

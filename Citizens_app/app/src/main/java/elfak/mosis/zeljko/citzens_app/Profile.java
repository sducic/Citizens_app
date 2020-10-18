package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
//import com.firebase.ui.auth.AuthUI;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.function.Predicate;


public class Profile extends AppCompatActivity {

    private static final String TAG = "Profile";

    Button btnLog;
    ImageView profileImageView;
    FirebaseAuth fAuth;
    private static Bitmap slika = null;
    TextView email,phoneNumber,fullName, coins;
    public Switch mLocationServiceSwitch;
    public static boolean switchFlag;

    int TAKE_IMAGE_CODE = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile2);


        fAuth=FirebaseAuth.getInstance();
        profileImageView = findViewById(R.id.profile_image);
        email=findViewById(R.id.txt_email);
        phoneNumber=findViewById(R.id.txt_phone);
        fullName=findViewById(R.id.txt_fullName);
        coins = findViewById(R.id.txt_coins);
        mLocationServiceSwitch = (Switch)findViewById(R.id.switch_locationService);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference baza = ref.child("Users").child(userId).child("profileImageUri");

        mLocationServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if(isChecked) {
                    prefs.edit().putBoolean("locked", true).apply();
                    LocationServiceHelper.startLocationService(getApplicationContext());
                    switchFlag=true;
                }
                else {
                    prefs.edit().putBoolean("locked", false).apply();
                    LocationServiceHelper.stopLocationService(getApplicationContext());
                    switchFlag=false;

                }
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean locked = prefs.getBoolean("locked", false);
        if(locked) {
            mLocationServiceSwitch.setChecked(true);
            switchFlag=true;
        }
        else{
            mLocationServiceSwitch.setChecked(false);
            switchFlag=false;
        }

        baza.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profImgUri = dataSnapshot.getValue(String.class);
                Uri myUri = Uri.parse(profImgUri);

                Picasso.get().load(myUri).into(profileImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference uidRef = rootRef.child("Users").child(uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String mail = dataSnapshot.child("email").getValue(String.class);
                String name = dataSnapshot.child("fullName").getValue(String.class);
                String phone = dataSnapshot.child("phoneNumber").getValue(String.class);
                int coin = dataSnapshot.child("coins").getValue(Integer.class);
               email.setText(mail);
               fullName.setText(name);
               phoneNumber.setText(phone);
               coins.setText(String.valueOf(coin));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage()); //Don't ignore errors!
            }


        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);


    }



    public void handleImageClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_IMAGE_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_IMAGE_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    slika = (Bitmap) data.getExtras().get("data");
                    profileImageView.setImageBitmap(slika);
                     handleUpload();
            }
        }
    }

    private void handleUpload() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        slika.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(uid + ".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                         getDownloadUrl(reference);
                        Toast.makeText(Profile.this, " Profile photo updated succesfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ",e.getCause() );
                    }
                });
    }



    private void getDownloadUrl(StorageReference reference) {
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess: " + uri);
                        setUserProfileUrl(uri);

                        String profImgUri;
                        profImgUri=uri.toString();
                        System.out.println("uri");
                        System.out.println(profImgUri);

                        DatabaseReference baza = FirebaseDatabase.getInstance().getReference();
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        baza.child("Users").child(userId).child("profileImageUri").setValue(profImgUri);
                    }
                });
    }

    private void setUserProfileUrl(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Profile.this, "Updated succesfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Profile.this, "Profile image failed...", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

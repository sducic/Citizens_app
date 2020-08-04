package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;


public class RegisterActivity extends AppCompatActivity {

    Button buttonCreate;
    EditText editTextEmail, editTextFullname, editTextPhone, editTextPassword1, editTextPassword2;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    ImageView profileImageView;
    private static Bitmap slika = null;
    public static String profileImageUri="";

    String DISPLAY_NAME = null;
    String PROFILE_IMAGE_URL = null;
    int TAKE_IMAGE_CODE = 10001;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextPassword1=findViewById(R.id.edit_text_password1);
        editTextPassword2=findViewById(R.id.edit_text_password2);
        editTextFullname=findViewById(R.id.edit_text_fullname);
        editTextPhone=findViewById(R.id.edit_text_phone);
        editTextEmail= findViewById(R.id.edit_text_email);
        buttonCreate = findViewById(R.id.button_create);
        fAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.progressBar);
        profileImageView = findViewById(R.id.profile_image);


        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email=editTextEmail.getText().toString().trim();
                final String fullName=editTextFullname.getText().toString().trim();
                final String phoneNumber=editTextPhone.getText().toString().trim();
                String password1=editTextPassword1.getText().toString().trim();
                String password2=editTextPassword2.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {   editTextEmail.setError("Email is required");  return; }

                if(TextUtils.isEmpty(password1))
                {   editTextPassword1.setError("Password is required");    return; }
                if(TextUtils.isEmpty(password2))
                {   editTextPassword2.setError("Repeat password");    return; }

                if(password1.length()<6)
                {  editTextPassword1.setError("Minimum 6 characters"); return;}
                if(password2.length()<6)
                {  editTextPassword2.setError("Minimum 6 characters"); return;}

                if(TextUtils.isEmpty(fullName))
                {   editTextFullname.setError("FullName is required");  return; }

                if(TextUtils.isEmpty(phoneNumber))
                {   editTextPhone.setError("PhoneNumber is required");  return; }

                if(!password1.equals(password2))
                {
                    editTextPassword1.setError("Password don't match");    return;
                }

                if(slika==null)
                {
                    Toast.makeText(RegisterActivity.this,"Please upload profile photo!", Toast.LENGTH_SHORT).show();
                }


                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Toast.makeText(Register.this,"User created",Toast.LENGTH_SHORT).show();
                            // startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            User user = new User(
                                    fullName,
                                    email,
                                    phoneNumber,
                                    profileImageUri
                            );

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        handleUpload();
                                        Toast.makeText(RegisterActivity.this, "User created", Toast.LENGTH_SHORT).show();
                                       // fAuth.signOut();
                                        //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        //new MyAsyncTask().execute();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }

                                }

                            });


                        }

                        else
                        {
                            Toast.makeText(RegisterActivity.this,"Error"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });



            }
        });


    }

    /*private class MyAsyncTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            handleUpload();
            Toast.makeText(RegisterActivity.this, "User created", Toast.LENGTH_SHORT).show();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            fAuth.signOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }*/

    //profile photo!

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
                    //handleUpload();
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
                        Toast.makeText(RegisterActivity.this, " Profile photo updated succesfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ",e.getCause() );
                    }
                });

    }

   /*private void saveUri(String uri)
    {

            FirebaseDatabase.getInstance().getReference("Users")
                   .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                   .child("profileImageUri")
                   .setValue(uri);

        Toast.makeText(RegisterActivity.this, " Save Uri", Toast.LENGTH_SHORT).show();


    }*/



    private void getDownloadUrl(StorageReference reference) {
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess: " + uri);
                        //setUserProfileUrl(uri);
                        String profImgUri;
                        profImgUri=uri.toString();
                        System.out.println("uri");
                        System.out.println(profImgUri);

                        DatabaseReference baza = FirebaseDatabase.getInstance().getReference();
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        baza.child("Users").child(userId).child("profileImageUri").setValue(profImgUri);

                        fAuth.signOut();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    }
                });
    }

   /* private void setUserProfileUrl(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterActivity.this, "Updated succesfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Profile image failed...", Toast.LENGTH_SHORT).show();
                    }
                });
    }*/
}

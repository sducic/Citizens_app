package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class AddObject extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {


    Button addButton;
    boolean editMode = true;
    int position = -1;

    EditText name, description, category, latitude, longitude;
    //slika
    ImageView reportImage;
    private static Bitmap slika = null;
    public static String profileImageUri="";
    int TAKE_IMAGE_CODE = 10001;
    private static final String TAG = "AddObject";
    public static String objectKey;

    String[] country = { "Kategorija1", "Kategorija2", "Kategorija3", "Ostalo"};
    public static String kategorija;


    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_object);

        try{
            Intent listIntent=getIntent();
            Bundle positionBundle=listIntent.getExtras();
            if(positionBundle != null)
                position=positionBundle.getInt("position");
            else
                editMode=false;
        } catch(Exception e) {
            editMode = false;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        EditText latitudeEdit = (EditText)findViewById((R.id.latitude_text));
        EditText longitudeEdit = (EditText)findViewById((R.id.longitude_text));
        reportImage = findViewById(R.id.imageView4);

        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        Spinner spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,country);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);

        if(position<=0)
        {

          //  Object object = MyObjectData.getInstance().getObject(position);


            Toast.makeText(getApplicationContext(),"nestooo", Toast.LENGTH_SHORT).show();

            String st = getIntent().getExtras().getString("Latitude");

            latitudeEdit.setText(st);

            String st1 = getIntent().getExtras().getString("Longitude");
            longitudeEdit.setText(st1);
        }

        longitudeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        longitudeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        addButton = findViewById(R.id.buttonAdd);

         name = (EditText) findViewById(R.id.editTextName);
         description = (EditText) findViewById(R.id.editTextDescription);
         category = (EditText) findViewById(R.id.editTextCategory);     // greska kad obrisem, ne znam zasto
         longitude = (EditText) findViewById(R.id.longitude_text);
         latitude = (EditText) findViewById(R.id.latitude_text);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String nme = name.getText().toString();
                final String desc = description.getText().toString();
                //final String categ = category.getText().toString();
                final String lon = longitude.getText().toString();
                final String lat = latitude.getText().toString();
                final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                String strDate = dateFormat.format(date).toString();
                String imgUri="";

                double lon1 = Double.parseDouble(lon);
                double lat1 = Double.parseDouble(lat);

                Object object = new Object(nme, desc, kategorija, lon1, lat1,userID,strDate,imgUri);
                MyObjectData.getInstance().addNewPlace(object);
                objectKey=object.getKey();
                handleUpload();//upload image
                Toast.makeText(getApplicationContext(), "Added object.", Toast.LENGTH_SHORT).show();

                //add coins
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference uidRef = rootRef.child("Users").child(uid);
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int coins = dataSnapshot.child("coins").getValue(int.class);
                        coins=coins+100;
                        rootRef.child("Users").child(uid).child("coins").setValue(coins);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, databaseError.getMessage()); //Don't ignore errors!
                    }
                };
                uidRef.addListenerForSingleValueEvent(valueEventListener);

                startActivity(new Intent(getApplicationContext(),FeedActivity.class));

            }
        });




        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        category.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        Toast.makeText(getApplicationContext(),country[position] , Toast.LENGTH_LONG).show();
        kategorija=country[position];
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


    @Override
    public void onClick(View v) {


    }

    public void handleImageClick(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
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
                    reportImage.setImageBitmap(slika);
                    //handleUpload();
            }
        }
    }

    private void handleUpload() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        slika.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String uniqueId = UUID.randomUUID().toString();
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("ObjectImages")
                .child(uniqueId);

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(reference);
                        Toast.makeText(AddObject.this, " Photo upload succesfully", Toast.LENGTH_SHORT).show();
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
                        //setUserProfileUrl(uri);
                        String profImgUri;
                        profImgUri=uri.toString();

                        DatabaseReference baza = FirebaseDatabase.getInstance().getReference();
                        baza.child("my-objects").child(objectKey).child("imgUri").setValue(profImgUri);



                    }
                });
    }




}

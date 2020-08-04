package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;

public class AddObject extends AppCompatActivity implements View.OnClickListener {


    Button addButton;
    boolean editMode = true;
    int position = -1;

    EditText name, description, category, latitude, longitude;


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
         category = (EditText) findViewById(R.id.editTextCategory);
         longitude = (EditText) findViewById(R.id.longitude_text);
         latitude = (EditText) findViewById(R.id.latitude_text);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String nme = name.getText().toString();
                final String desc = description.getText().toString();
                final String categ = category.getText().toString();
                final String lon = longitude.getText().toString();
                final String lat = latitude.getText().toString();

                Object object = new Object(nme, desc, categ, lat, lon);
                MyObjectData.getInstance().addNewPlace(object);
                Toast.makeText(getApplicationContext(), "Added object.", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(getApplicationContext(),Maps.class));

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


    @Override
    public void onClick(View v) {


    }





}

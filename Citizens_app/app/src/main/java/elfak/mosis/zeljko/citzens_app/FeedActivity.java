package elfak.mosis.zeljko.citzens_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
//import com.squareup.picasso.Picasso;


public class FeedActivity extends AppCompatActivity {

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabaseReference;


    private String izabranaKategorija;
    private Button btnSearch;
    private EditText searchTxt;
    private Spinner spin;
    private TextView textDate;
    private TextView textDate2;
    private Button pickDate;
    private Button pickDate2;
    private Calendar c;
    private DatePickerDialog dpd;
    private Button findDate;

    private DatePickerDialog.OnDateSetListener fromDate;
    private DatePickerDialog.OnDateSetListener toDate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        textDate = (TextView)findViewById(R.id.textDate_first);
        textDate2 = (TextView)findViewById(R.id.textDate_second);
        pickDate = (Button) findViewById(R.id.pickDate);
        pickDate2 = (Button) findViewById(R.id.pickDate2);
        spin = (Spinner) findViewById(R.id.spinner1);
        btnSearch = (Button) findViewById(R.id.button_Search);
        searchTxt = (EditText) findViewById(R.id.txtSearchByName);

        mUsersList = (RecyclerView) findViewById(R.id.recycleViewPost);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("my-objects");
        mUsersDatabaseReference.keepSynced(true);


        getCategories(); //ucitavanje kategorija u spinner



        ////pretrazivanje po imenu


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchByNaslov();
            }


        });



        ///pick date 1

        pickDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fromDate = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker1, int year, int month, int day) {
                                month = month + 1;
                                String date;
                                if (day < 10) {
                                    date = year + "/" + month + "/0" + day;
                                } else {
                                    date = year + "/" + month + "/" + day;
                                }

                                textDate.setText(date);
                            }
                        };




                        ////////////////
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog1 = new DatePickerDialog(
                                FeedActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                fromDate,
                                year, month, day);
                        //dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog1.show();
                    }
        });


        ////pick date 2
        pickDate2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        toDate = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker2, int year, int month, int day) {
                                month = month + 1;
                                String date;
                                if(day < 10)
                                {
                                    date = year + "/" + month + "/0" + day;
                                }
                                else {
                                    date = year + "/" + month + "/" + day;
                                }

                                textDate2.setText(date);
                            }
                        };




                        //////////////
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog1 = new DatePickerDialog(
                                FeedActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                toDate,
                                year,month,day);
                        dialog1.show();
                    }
                });





        findDate = (Button) findViewById(R.id.findDate);
        findDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchByDate();
            }
        });




    }



    @Override
    protected void onStart() {
        super.onStart();
    }



    public static class UserViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView=(TextView)mView.findViewById(R.id.name);
            userNameView.setText(name);
        }


        public void setDate(String email) {
            TextView userStatusView=(TextView)mView.findViewById(R.id.date);
            userStatusView.setText(email);
        }

        public void setImage(Uri pom) {

            ImageView userImageView = (ImageView) mView.findViewById(R.id.photo);
            Picasso.get().load(pom).into(userImageView);
        }

        public void setCategory(String category)
        {
            TextView problemCategory = (TextView)mView.findViewById(R.id.name);
            problemCategory.setText(category);

        }

    }

    @Override
    protected void onStop() {
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue(ServerValue.TIMESTAMP);

        super.onStop();
    }




    private void SearchByNaslov()
    {
        String naslov = searchTxt.getText().toString().trim();
        FirebaseRecyclerAdapter<Object, UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Object, UserViewHolder>(
                Object.class,
                R.layout.post_card,
                UserViewHolder.class,
                mUsersDatabaseReference.orderByChild("name").equalTo(naslov)

        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, Object object, int position) {


                viewHolder.setName(object.getName());
                viewHolder.setDate(object.getDate());
                //viewHolder.setCategory(object.getCategory());

                String imgUri = object.getImgUri();
                Uri myUri = Uri.parse(imgUri);
                viewHolder.setImage(myUri);

                final String object_id = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent showObject = new Intent(FeedActivity.this, ShowObject.class);
                        showObject.putExtra("object_id", object_id);
                        startActivity(showObject);
                    }
                });
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
        searchTxt.getText().clear();
    }


    private void searchByDate()
    {

            String date = textDate.getText().toString();
            String date2 = textDate2.getText().toString();
        Toast.makeText(getApplicationContext(),date,Toast.LENGTH_SHORT).show();
            FirebaseRecyclerAdapter<Object, UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Object, UserViewHolder>(
                    Object.class,
                    R.layout.post_card,
                    UserViewHolder.class,
                    mUsersDatabaseReference.orderByChild("date").startAt(date).endAt(date2)

            ) {
                @Override
                protected void populateViewHolder(UserViewHolder viewHolder, Object object, int position) {


                    viewHolder.setName(object.getName());
                    viewHolder.setDate(object.getDate());
                    //viewHolder.setCategory(object.getCategory());

                    String imgUri = object.getImgUri();
                    Uri myUri = Uri.parse(imgUri);
                    viewHolder.setImage(myUri);

                    final String object_id = getRef(position).getKey();

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent showObject = new Intent(FeedActivity.this, ShowObject.class);
                            showObject.putExtra("object_id", object_id);
                            startActivity(showObject);
                        }
                    });
                }
            };
            mUsersList.setAdapter(firebaseRecyclerAdapter);
            searchTxt.getText().clear();

    }

    private  void getCategories()
    {
        final List<String> list = new ArrayList<String>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("my-objects");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String category = snapshot.child("category").getValue(String.class);


                    if(!list.contains(category))
                        list.add(category);



                    ArrayAdapter<String> a = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item, list);
                    a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //  Setting the ArrayAdapter data on the Spinner
                    spin.setAdapter(a);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        spin();
    }

    private void spin()
    {
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                izabranaKategorija = (String) spin.getSelectedItem();
                FirebaseRecyclerAdapter<Object, UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Object, UserViewHolder>(
                        Object.class,
                        R.layout.post_card,
                        UserViewHolder.class,
                        mUsersDatabaseReference.orderByChild("category").equalTo(izabranaKategorija)


                ) {
                    @Override
                    protected void populateViewHolder(UserViewHolder viewHolder, Object object, int position) {


                        viewHolder.setName(object.getName());
                        viewHolder.setDate(object.getDate());

                        String imgUri = object.getImgUri();
                        Uri myUri = Uri.parse(imgUri);
                        viewHolder.setImage(myUri);

                        final String object_id = getRef(position).getKey();

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent showObject = new Intent(FeedActivity.this, ShowObject.class);
                                showObject.putExtra("object_id", object_id);
                                startActivity(showObject);
                            }
                        });
                    }
                };
                mUsersList.setAdapter(firebaseRecyclerAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(FeedActivity.this,HomePage.class);
        startActivity(i);
    }
}
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
    Spinner spin;
    String izabranaKategorija;
    Button btnSearch;
    EditText searchTxt;

    TextView textDate;
    TextView textDate2;

    Button pickDate;
    Button pickDate2;
    Calendar c;
    DatePickerDialog dpd;

    Button findDate;

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private DatePickerDialog.OnDateSetListener mDateSetListener2;

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
        getCategories(); //ucitavanje kategorija u spinner

        mUsersList = (RecyclerView) findViewById(R.id.recycleViewPost);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("my-objects");

        mUsersDatabaseReference.keepSynced(true);

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
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ////pretrazivanje po imenu


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchByNaslov();
            }


        });



        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDateTime();
            }
        });

        pickDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDateTime2();
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
/*

        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue("true");

        //-------FIREBASE RECYCLE VIEW ADAPTER-------

        izabranaKategorija = (String)spin.getSelectedItem();
        FirebaseRecyclerAdapter<Object , UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Object, UserViewHolder>(
                Object.class,
                R.layout.post_card,
                UserViewHolder.class,
                mUsersDatabaseReference.orderByChild("category").equalTo(izabranaKategorija)


        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, Object object, int position) {


                    viewHolder.setName(object.getName());
                    viewHolder.setDate(object.getDate());
                    viewHolder.setCategory(object.getCategory());

                    String imgUri = object.getImgUri();
                    Uri myUri = Uri.parse(imgUri);
                    viewHolder.setImage(myUri);


                final String object_id=getRef(position).getKey();



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
        mUsersList.setAdapter(firebaseRecyclerAdapter);*/
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


       /* CardView card_view = (CardView) findViewById(R.id.card_view); // creating a CardView and assigning a value.

        card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do whatever you want to do on click (to launch any fragment or activity you need to put intent here.)
            }
        });*/

    }

    @Override
    protected void onStop() {
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue(ServerValue.TIMESTAMP);

        super.onStop();
    }

    private void pickDateTime() {
        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog1 = new DatePickerDialog(
                        FeedActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                //dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog1.show();
            }
        });


        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
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
    }






    private void pickDateTime2()
    {

        pickDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog1 = new DatePickerDialog(
                        FeedActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                //dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog1.show();
            }
        });


        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
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


}
package elfak.mosis.zeljko.citzens_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
//import com.squareup.picasso.Picasso;


public class FeedActivity extends AppCompatActivity {

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabaseReference;
    Spinner spin;
    String izabranaKategorija;
    Button btnSearch;
    EditText searchTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

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
}
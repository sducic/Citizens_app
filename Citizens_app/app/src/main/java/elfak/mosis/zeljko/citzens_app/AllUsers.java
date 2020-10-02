package elfak.mosis.zeljko.citzens_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
//import com.squareup.picasso.Picasso;


public class AllUsers extends AppCompatActivity {

    private RecyclerView mUsersList;
    private EditText mSearch;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mUsersList=(RecyclerView)findViewById(R.id.recyclerViewUsersList);
        mSearch = (EditText)findViewById(R.id.search_et);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabaseReference.keepSynced(true);

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = mSearch.getText().toString();
                    firebaseSearch(text);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue("true");

        firebaseSearch("");

    }

    private void firebaseSearch(String searchText)
    {
        Query firebaseSearchQuery = mUsersDatabaseReference.orderByChild("fullName").startAt(searchText).endAt(searchText + "\uf8ff");
        FirebaseRecyclerAdapter<User , UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class,
                R.layout.recycle_list_single_user,
                UserViewHolder.class,
                firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, User users, int position) {
                viewHolder.setName(users.getfullName());
                viewHolder.setEmail(users.getEmail());

                String imgUri=users.getProfileImageUri();
                Uri myUri = Uri.parse(imgUri);

                viewHolder.setImage(myUri);

                final String user_id = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(AllUsers.this, UserProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);
                    }
                });



            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView=(TextView)mView.findViewById(R.id.textViewSingleListName);
            userNameView.setText(name);
        }


        public void setEmail(String email) {
            TextView userStatusView=(TextView)mView.findViewById(R.id.textViewSingleListStatus);
            userStatusView.setText(email);
        }

        public void setImage(Uri pom) {

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.circleImageViewUserImage);
            Picasso.get().load(pom).into(userImageView);
        }
    }

    @Override
    protected void onStop() {
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue(ServerValue.TIMESTAMP);

        super.onStop();
    }
}
package elfak.mosis.zeljko.citzens_app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
//import com.squareup.picasso.Picasso;


public class AllUsers extends AppCompatActivity {

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mUsersList=(RecyclerView)findViewById(R.id.recyclerViewUsersList);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabaseReference.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue("true");

        //-------FIREBASE RECYCLE VIEW ADAPTER-------
        FirebaseRecyclerAdapter<User , UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class,
                R.layout.recycle_list_single_user,
                UserViewHolder.class,
                mUsersDatabaseReference
        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, User users, int position) {
                viewHolder.setName(users.getfullName());
                viewHolder.setEmail(users.getEmail());

                String imgUri=users.getProfileImageUri();
                Uri myUri = Uri.parse(imgUri);

                viewHolder.setImage(myUri);



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
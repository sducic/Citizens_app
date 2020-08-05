package elfak.mosis.zeljko.citzens_app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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


public class FeedActivity extends AppCompatActivity {

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mUsersList=(RecyclerView)findViewById(R.id.recycleViewPost);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabaseReference= FirebaseDatabase.getInstance().getReference().child("my-objects");
        mUsersDatabaseReference.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue("true");

        //-------FIREBASE RECYCLE VIEW ADAPTER-------
        FirebaseRecyclerAdapter<Object , UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Object, UserViewHolder>(
                Object.class,
                R.layout.post_card,
                UserViewHolder.class,
                mUsersDatabaseReference
        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, Object object, int position) {
                viewHolder.setName(object.getName());
                viewHolder.setDate(object.getDate());

                String imgUri=object.getImgUri();
                Uri myUri = Uri.parse(imgUri);
                viewHolder.setImage(myUri);

               // viewHolder.setEmail(users.getEmail());
                //viewHolder.setImage(users.getThumbImage(),getApplicationContext());
                //final String user_id=getRef(position).getKey();

               /* viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent=new Intent(Friends.this,Profile.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });*/
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

       /* public void setImage(String thumb_image,Context ctx) {
            CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.circleImageViewUserImage);
            //Log.e("thumb URL is--- ",thumb_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.user_img).into(userImageView);
        }*/
    }

    @Override
    protected void onStop() {
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue(ServerValue.TIMESTAMP);

        super.onStop();
    }
}
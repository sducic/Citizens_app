package elfak.mosis.zeljko.citzens_app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class Coins extends AppCompatActivity {

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabaseReference;
    public int num=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coins);

        mUsersList=(RecyclerView)findViewById(R.id.recyclerViewUsersList2);
        mUsersList.setHasFixedSize(true);
        //mUsersList.setLayoutManager(new LinearLayoutManager(this));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);        //radi reverse liste iz asc u desc
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mUsersList.setLayoutManager(linearLayoutManager);


        mUsersDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabaseReference.keepSynced(true);

        

    }

    @Override
    protected void onStart() {
        super.onStart();

        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue("true");

        //-------FIREBASE RECYCLE VIEW ADAPTER-------
        FirebaseRecyclerAdapter<User , Coins.UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<User, Coins.UserViewHolder>(
                User.class,
                R.layout.recycle_list_single_user_coins,
                Coins.UserViewHolder.class,
                mUsersDatabaseReference.orderByChild("coins")   //sortiranje desc
        ) {
            @Override
            protected void populateViewHolder(Coins.UserViewHolder viewHolder, User users, int position) {
                viewHolder.setName(users.getfullName());
                viewHolder.setCoin(users.getCoins());

                String imgUri=users.getProfileImageUri();
                Uri myUri = Uri.parse(imgUri);

                viewHolder.setImage(myUri);

                viewHolder.setNumber(num);      //redni broj
                num++;




               // viewHolder.setImage(pom);

               /* viewHolder.setImage(users.getThumbImage(),getApplicationContext());
                final String user_id=getRef(position).getKey();


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent=new Intent(Coins.this,Profile.class);
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
            TextView userNameView=(TextView)mView.findViewById(R.id.textViewSingleListName);
            userNameView.setText(name);
        }

        public void setCoin(int coin) {
            TextView userCoinView=(TextView)mView.findViewById(R.id.textViewSingleListCoin);
            userCoinView.setText(String.valueOf(coin));
        }

        public void setNumber(int num)
        {
            TextView userCoinView=(TextView)mView.findViewById(R.id.cifra);
            userCoinView.setText(String.valueOf(num));
        }

        public void setImage(Uri pom) {

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.circleImageViewUserImage);
            Picasso.get().load(pom).into(userImageView);
        }

     /*  public void setImage(String thumb_image, Context ctx) {
            CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.circleImageViewUserImage);
            //Log.e("thumb URL is--- ",thumb_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.user_img).into(userImageView);
        }*/
    }

    @Override
    protected void onStop() {
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue(ServerValue.TIMESTAMP);

        super.onStop();
    }
}

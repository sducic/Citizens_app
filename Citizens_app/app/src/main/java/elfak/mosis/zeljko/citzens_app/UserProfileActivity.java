package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoProvider;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView mProfileImageView;
    private TextView mUsernameTextView, mEmailTextView, mTextNotigf;
    private Button mSendRequestBtn, mDeclineRequestBtn;
    private DatabaseReference mUsersDatabaseRef, mFriendRequestDatabase, mFriendDatabase, mRootRef;
    private ProgressDialog mProgressDialog;
    private String mCurrent_state;
    private FirebaseUser mCurrent_user;
    private String user_token, fullName;
    private double  userLongitude, userLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        final String usid = getIntent().getStringExtra("user_id");
        

        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(usid);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mTextNotigf = (TextView)findViewById(R.id.textNotif);


        mTextNotigf.setVisibility(View.INVISIBLE);

        mProfileImageView = (ImageView)findViewById(R.id.iv_thumbnail);
        mUsernameTextView = (TextView)findViewById(R.id.tv_username);
        mEmailTextView = (TextView)findViewById(R.id.tv_email);
        mSendRequestBtn = (Button)findViewById(R.id.btn_request);
        mDeclineRequestBtn = (Button)findViewById(R.id.btn_decline_request);
        mDeclineRequestBtn.setVisibility(View.INVISIBLE);
        mDeclineRequestBtn.setEnabled(false);

        mCurrent_state = "not_friends";

        if(usid.equals(mCurrent_user.getUid())){
            mSendRequestBtn.setVisibility(View.INVISIBLE);
            mSendRequestBtn.setEnabled(false);
            mDeclineRequestBtn.setVisibility(View.INVISIBLE);
            mDeclineRequestBtn.setEnabled(false);
            mTextNotigf.setVisibility(View.VISIBLE);
            mTextNotigf.setText("This is how your profile looks to others.");

        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait while we download user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mUsersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("fullName").getValue().toString();
                String display_email = dataSnapshot.child("email").getValue().toString();
                String thumbnail = dataSnapshot.child("profileImageUri").getValue().toString();
                user_token = dataSnapshot.child("device_token").getValue().toString();

                mUsernameTextView.setText(display_name);
                mEmailTextView.setText(display_email);

                Uri myUri = Uri.parse(thumbnail);
                mProfileImageView.setImageURI(myUri);

                Picasso.get().load(myUri).placeholder(R.drawable.ic_action_image1).into(mProfileImageView);


                mFriendRequestDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(usid)) {
                            String req_type = dataSnapshot.child(usid).child("request_type").getValue().toString();

                            if(req_type.equals("received")) {
                                mCurrent_state="req_received";
                                mSendRequestBtn.setText("Accept friend request");
                                mDeclineRequestBtn.setVisibility(View.VISIBLE);
                                mDeclineRequestBtn.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mSendRequestBtn.setText("Cancel friend request");
                                mDeclineRequestBtn.setVisibility(View.INVISIBLE);
                                mDeclineRequestBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }

                        else {
                            mFriendDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(usid)) {
                                        mCurrent_state="friends";
                                        mSendRequestBtn.setText("Unfriend this person");
                                        mDeclineRequestBtn.setText("Show friend on map");
                                        mDeclineRequestBtn.setVisibility(View.VISIBLE);
                                        mDeclineRequestBtn.setEnabled(true);
                                    }
                                    else {
                                        mCurrent_state = "not_friends";
                                        mSendRequestBtn.setText("Send friend request");
                                        mDeclineRequestBtn.setVisibility(View.INVISIBLE);
                                        mDeclineRequestBtn.setEnabled(false);
                                    }

                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                            mProgressDialog.dismiss();
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });


        mFriendDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(usid)) {
                    mCurrent_state = "not_friends";
                    mSendRequestBtn.setText("Send friend request");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendRequestBtn.setEnabled(false);

                if(mCurrent_state.equals("not_friends")) {

                    Map requestMap = new HashMap();
                    requestMap.put(mCurrent_user.getUid() + "/" + usid + "/request_type", "sent");
                    requestMap.put(usid + "/" + mCurrent_user.getUid() + "/request_type", "received");

                    mFriendRequestDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError == null) {

                                    Retrofit rf = new Retrofit.Builder()
                                            .baseUrl("https://citizensapp-b9609.web.app/api/")
                                            .addConverterFactory(GsonConverterFactory.create())
                                            .build();
                                    Api api = rf.create(Api.class);


                                    Call<ResponseBody> call = api.sendNotification(user_token, "Friend request", "You've received a new friend request", mCurrent_user.getUid());
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try {
                                                Toast.makeText(UserProfileActivity.this, response.body().string(), Toast.LENGTH_SHORT).show();
                                                mSendRequestBtn.setEnabled(true);
                                                mCurrent_state="req_sent";
                                                mSendRequestBtn.setText("Cancel friend request");
                                                Toast.makeText(UserProfileActivity.this, "Friend request sent successfully", Toast.LENGTH_SHORT).show();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Toast.makeText(UserProfileActivity.this, "Fail sending request", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }                        }
                    });

                }

                if(mCurrent_state.equals("req_sent")) {
                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(usid).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            mFriendRequestDatabase.child(usid).child(mCurrent_user.getUid()).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            mSendRequestBtn.setEnabled(true);
                                                            mCurrent_state="not_friends";
                                                            mSendRequestBtn.setText("Send friend request");
                                                        }
                                                    });
                                        }
                                }
                            });

                }

                if(mCurrent_state.equals("req_received"))
                {
                    final String currDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + usid + "/date", currDate);
                    friendsMap.put("Friends/" + usid + "/" + mCurrent_user.getUid() + "/date", currDate);
                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + usid, null);
                    friendsMap.put("Friend_req/" + usid + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null) {
                                mSendRequestBtn.setEnabled(true);
                                mCurrent_state="friends";
                                mSendRequestBtn.setText("Unfriend this person");
                                mDeclineRequestBtn.setText("Show friend location");

                            }

                            else {
                                String error = databaseError.getMessage();
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                }

                if(mCurrent_state.equals("friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + usid, null);
                    unfriendMap.put("Friends/" + usid + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null) {

                                mCurrent_state="not_friends";
                                mSendRequestBtn.setText("Send friend request");
                                mDeclineRequestBtn.setVisibility(View.INVISIBLE);
                                mDeclineRequestBtn.setEnabled(true);
                            }

                            else {
                                String error = databaseError.getMessage();
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();

                            }
                            mSendRequestBtn.setEnabled(true);

                        }
                    });

                }
            }
        });


        mDeclineRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrent_state.equals("req_received")) {
                   Map requests = new HashMap();
                   requests.put("Friend_req/" + mCurrent_user.getUid() + "/" + usid, null);
                   requests.put("Friend_req/" + usid + "/" + mCurrent_user.getUid(), null);

                   mRootRef.updateChildren(requests, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                           if(databaseError == null) {
                                mCurrent_state = "not_friends";
                                mSendRequestBtn.setText("Send friend request");
                                mDeclineRequestBtn.setVisibility(View.INVISIBLE);
                                mDeclineRequestBtn.setEnabled(false);
                           }

                           else {
                               String error = databaseError.getMessage();
                               Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                           }
                       }
                   });
                }
                if(mCurrent_state.equals("friends")) {

                    Intent ni = new Intent(getApplicationContext(), UserLocationsActivity.class);
                    ni.putExtra("user_id", usid);
                    startActivity(ni);
                }

            }
        });
    }
}

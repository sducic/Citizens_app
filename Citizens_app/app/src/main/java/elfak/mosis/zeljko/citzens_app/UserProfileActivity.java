package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoProvider;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView mProfileImageView;
    private TextView mUsernameTextView, mEmailTextView;
    private Button mSendRequestBtn, mDeclineRequestBtn;
    private DatabaseReference mUsersDatabaseRef, mFriendRequestDatabase, mFriendDatabase;
    private ProgressDialog mProgressDialog;
    private String mCurrent_state;
    private FirebaseUser mCurrent_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        final String usid = getIntent().getStringExtra("user_id");

        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(usid);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImageView = (ImageView)findViewById(R.id.iv_thumbnail);
        mUsernameTextView = (TextView)findViewById(R.id.tv_username);
        mEmailTextView = (TextView)findViewById(R.id.tv_email);
        mSendRequestBtn = (Button)findViewById(R.id.btn_request);
        mDeclineRequestBtn = (Button)findViewById(R.id.btn_decline_request);
        mDeclineRequestBtn.setVisibility(View.INVISIBLE);
        mDeclineRequestBtn.setEnabled(false);

        mCurrent_state = "not_friends";

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

                mUsernameTextView.setText(display_name);
                mEmailTextView.setText(display_email);

                Uri myUri = Uri.parse(thumbnail);
                mProfileImageView.setImageURI(myUri);

                Picasso.get().load(myUri).placeholder(R.drawable.ic_action_image1).into(mProfileImageView);

                mFriendRequestDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(usid)) {
                                        mCurrent_state="friends";
                                        mSendRequestBtn.setText("Unfriend this person");
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


        mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(usid).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                mFriendRequestDatabase.child(usid).child(mCurrent_user.getUid()).child("request_type").setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mSendRequestBtn.setEnabled(true);
                                                mCurrent_state="req_sent";
                                                mSendRequestBtn.setText("Cancel friend request");
                                                Toast.makeText(UserProfileActivity.this, "Friend request sent successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else {
                                Toast.makeText(UserProfileActivity.this, "Fail sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
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
                    mFriendDatabase.child(mCurrent_user.getUid()).child(usid).setValue(currDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        mFriendDatabase.child(usid).child(mCurrent_user.getUid()).setValue(currDate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()) {
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
                                                                                                    mCurrent_state="friends";
                                                                                                    mSendRequestBtn.setText("Unfriend this person");
                                                                                                    mDeclineRequestBtn.setVisibility(View.INVISIBLE);
                                                                                                    mDeclineRequestBtn.setEnabled(false);
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });

                                                            }
                                                    }
                                                });
                                    }
                                }
                            });

                }

                if(mCurrent_state.equals("friends")) {
                    mFriendDatabase.child(mCurrent_user.getUid()).child(usid).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        mFriendDatabase.child(usid).child(mCurrent_user.getUid()).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
                                                            mSendRequestBtn.setEnabled(true);
                                                            mCurrent_state="not_friends";
                                                            mSendRequestBtn.setText("Send request");
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });


        mDeclineRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrent_state.equals("req_received")) {
                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(usid).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        mFriendRequestDatabase.child(usid).child(mCurrent_user.getUid()).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
                                                            Toast.makeText(UserProfileActivity.this, "Friend request declined", Toast.LENGTH_SHORT).show();
                                                            mCurrent_state="not_friends";
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });
    }
}

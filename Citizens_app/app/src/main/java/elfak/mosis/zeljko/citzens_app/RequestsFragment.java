package elfak.mosis.zeljko.citzens_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private DatabaseReference mRequestDatabaseRef, mUsersDatabaseRef;
    private RecyclerView mRequestList;
    private View mMainView;
    private FirebaseAuth mAuth;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestsFragment() {
        // Required empty public constructor
    }


    public static RequestsFragment newInstance(String param1, String param2) {
        RequestsFragment fragment = new RequestsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friend_req, RequestsViewHolder> reqRequestsViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friend_req, RequestsViewHolder>(
                Friend_req.class,
                R.layout.recycle_list_single_user,
                RequestsViewHolder.class,
                mRequestDatabaseRef
        ) {
            @Override
            protected void populateViewHolder(RequestsViewHolder requestsViewHolder, Friend_req friend_req, int i) {

                String list_user_id = getRef(i).getKey();
                mUsersDatabaseRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String usn = dataSnapshot.child("fullName").getValue().toString();
                        String imgUri = dataSnapshot.child("profileImageUri").getValue().toString();
                        String email = dataSnapshot.child("email").getValue().toString();

                        requestsViewHolder.setEmail(email);
                        requestsViewHolder.setImage(imgUri);
                        requestsViewHolder.setName(usn);

                        requestsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent profileIntent = new Intent(getActivity(), UserProfileActivity.class);
                                profileIntent.putExtra("user_id", list_user_id);
                                startActivity(profileIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        };

        mRequestList.setAdapter(reqRequestsViewHolderFirebaseRecyclerAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);
        mRequestList = (RecyclerView)mMainView.findViewById(R.id.recyclerViewRequests);
        mAuth = FirebaseAuth.getInstance();

        mRequestDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mAuth.getUid());
        mRequestDatabaseRef.keepSynced(true);
        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabaseRef.keepSynced(true);

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;


    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView usernameView = (TextView)mView.findViewById(R.id.textViewSingleListName);
            usernameView.setText(name);
        }

        public void setEmail(String email) {
            TextView emailView = (TextView)mView.findViewById(R.id.textViewSingleListStatus);
            emailView.setText(email);

        }
        public void setImage(String uri) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.circleImageViewUserImage);
            Picasso.get().load(uri).into(userImageView);
        }
    }
}

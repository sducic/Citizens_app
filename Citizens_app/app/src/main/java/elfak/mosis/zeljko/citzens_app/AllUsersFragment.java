package elfak.mosis.zeljko.citzens_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class AllUsersFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView mUsersList;
    private EditText mSearch;
    private DatabaseReference mUsersDatabaseReference;
    private FirebaseAuth mAuth;
    private int pos;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public static AllUsersFragment newInstance(String param1, String param2) {
        AllUsersFragment fragment = new AllUsersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public AllUsersFragment() {
        // Required empty public constructor
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

        View v = inflater.inflate(R.layout.fragment_all_users, container, false);
        mUsersList=(RecyclerView)v.findViewById(R.id.recyclerViewUsersList);
        mSearch = (EditText)v.findViewById(R.id.search_et);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();

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

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //mUsersDatabaseReference.child(uid).child("online").setValue("true");

        firebaseSearch("");

    }

    private void firebaseSearch(String searchText)
    {
        Query firebaseSearchQuery = mUsersDatabaseReference.orderByChild("email").startAt(searchText).endAt(searchText + "\uf8ff");
        FirebaseRecyclerAdapter<User , AllUsersFragment.UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<User, AllUsersFragment.UserViewHolder>(
                User.class,
                R.layout.recycle_list_single_user,
                AllUsersFragment.UserViewHolder.class,
                firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(AllUsersFragment.UserViewHolder viewHolder, User users, int position) {
                viewHolder.setName(users.getfullName());
                viewHolder.setEmail(users.getEmail());

                String imgUri=users.getProfileImageUri();
                Uri myUri = Uri.parse(imgUri);

                viewHolder.setImage(myUri);

                final String user_id = getRef(position).getKey();
                if(user_id.equals(mAuth.getUid())){
                    pos = position;
                }

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(getActivity(), UserProfileActivity.class);
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


}

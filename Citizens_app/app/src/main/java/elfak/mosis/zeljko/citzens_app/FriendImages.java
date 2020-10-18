package elfak.mosis.zeljko.citzens_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendImages {

    public static final long ONE_MEGABYTE=1024*1024;
    private StorageReference mUsersImagesStorage = FirebaseStorage.getInstance().getReference().child("profileImages");
    private DatabaseReference mFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private ArrayList<String> friendsIds = new ArrayList<String>();
    public HashMap<String, Bitmap> usersImages = new HashMap<String, Bitmap>();

    public void loadUserImages() {
        mFriendsDatabaseRef.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    friendsIds.add(ds.getKey());
                }

                for(String usid : friendsIds) {
                    mUsersImagesStorage.child(usid + ".jpeg").getBytes(5 * ONE_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                        @Override
                        public void onComplete(@NonNull Task<byte[]> task) {
                            byte data[] = task.getResult();
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Bitmap scaledBmp = bmp.createScaledBitmap(bmp, 100,100,false);
                            usersImages.put(usid, scaledBmp);

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }





}

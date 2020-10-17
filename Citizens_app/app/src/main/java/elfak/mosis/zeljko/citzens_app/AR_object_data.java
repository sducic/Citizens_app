package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AR_object_data {

    private ArrayList<AR_object> AR_objects;
    private HashMap<String,Integer> ar_objectsKeyIndexMapping;
    private DatabaseReference database;
    private static final String FIREBASE_CHILD = "ar_objects";
    ListUpdatedEventListener updatedListener;

    private static AR_object_data sInstance = null;



    private AR_object_data() {
        AR_objects = new ArrayList<AR_object>();
        ar_objectsKeyIndexMapping = new HashMap<String, Integer>();
        database = FirebaseDatabase.getInstance().getReference();
        database.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
        database.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);
    }

    ValueEventListener parentEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if(updatedListener != null)
                updatedListener.onListUpdated();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String arObjectKey = dataSnapshot.getKey();
            if(!ar_objectsKeyIndexMapping.containsKey(arObjectKey)){
                AR_object ar_object = dataSnapshot.getValue(AR_object.class);
                ar_object.key = arObjectKey;
                AR_objects.add(ar_object);
                ar_objectsKeyIndexMapping.put(arObjectKey, AR_objects.size()-1);
                if(updatedListener!= null)
                    updatedListener.onListUpdated();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            String arObjectKey = dataSnapshot.getKey();
            AR_object ar_object = dataSnapshot.getValue(AR_object.class);
            ar_object.key = arObjectKey;
            if(ar_objectsKeyIndexMapping.containsKey(arObjectKey)) {
                int index = ar_objectsKeyIndexMapping.get(arObjectKey);
                AR_objects.set(index,ar_object);
            } else {
                AR_objects.add(ar_object);
                ar_objectsKeyIndexMapping.put(arObjectKey, AR_objects.size()-1);
            }
            if(updatedListener != null)
                updatedListener.onListUpdated();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            String myObjectKey = dataSnapshot.getKey();
            if(ar_objectsKeyIndexMapping.containsKey(myObjectKey)){
                int index = ar_objectsKeyIndexMapping.get(myObjectKey);
                AR_objects.remove(index);
                recreateKeyIndexMapping();
                if(updatedListener != null)
                    updatedListener.onListUpdated();
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };



    public void addnewArObject(AR_object ar_object)
    {
        String key = database.push().getKey();
        AR_objects.add(ar_object);
        ar_objectsKeyIndexMapping.put(key, AR_objects.size()-1);
        database.child(FIREBASE_CHILD).child(key).setValue(ar_object);
        ar_object.key = key;
    }

    public void deleteARObject(int index)
    {
        database.child(FIREBASE_CHILD).child(AR_objects.get(index).key).removeValue();
        AR_objects.remove(index);
        recreateKeyIndexMapping();
    }

    public void updatePlace(int index, double lng, double lat) {
        AR_object ar_object = AR_objects.get(index);
        ar_object.latitude = lat;
        ar_object.longitude = lng;
        database.child(FIREBASE_CHILD).child(ar_object.key).setValue(ar_object);
    }

    public void recreateKeyIndexMapping() {
        ar_objectsKeyIndexMapping.clear();
        for(int i = 0; i< AR_objects.size(); i++) {
            ar_objectsKeyIndexMapping.put(AR_objects.get(i).key,i);
        }
    }




    public interface ListUpdatedEventListener {
        void onListUpdated();
    }

    public void setEventListener(ListUpdatedEventListener listener) {
        updatedListener = listener;
    }

    private static class SingletonHolder{
        public static final AR_object_data instance = new AR_object_data();
    }
    public static AR_object_data getInstance(){
        return AR_object_data.SingletonHolder.instance;
    }


}

package elfak.mosis.zeljko.citzens_app;

import android.widget.Toast;

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

public class MyObjectData {

    private ArrayList<Object> myObjects;
    private HashMap<String,Integer> myObjectsKeyIndexMapping;
    private DatabaseReference database;
    private static final String FIREBASE_CHILD = "my-objects";
    ListUpdatedEventListener  updateListener;

    private MyObjectData(){
        myObjects = new ArrayList<Object>();
        myObjectsKeyIndexMapping = new HashMap<String,Integer>();
        database = FirebaseDatabase.getInstance().getReference();
        database.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
        database.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);
    }

    ValueEventListener parentEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(updateListener != null)
                updateListener.onListUpdated();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String myObjectKey = dataSnapshot.getKey();
            if(!myObjectsKeyIndexMapping.containsKey(myObjectKey))
            {
                Object object = dataSnapshot.getValue(Object.class);
                object.key = myObjectKey;
                myObjects.add(object);
                myObjectsKeyIndexMapping.put(myObjectKey,myObjects.size()-1);
                if(updateListener != null)
                    updateListener.onListUpdated();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            String myObjectKey = dataSnapshot.getKey();
            Object object = dataSnapshot.getValue(Object.class);
            object.key = myObjectKey;
            if(myObjectsKeyIndexMapping.containsKey(myObjectKey))
            {
                int index = myObjectsKeyIndexMapping.get(myObjectKey);
                myObjects.set(index,object);
            } else {
                myObjects.add(object);
                myObjectsKeyIndexMapping.put(myObjectKey,myObjects.size() - 1);
            }

            if(updateListener != null)
                updateListener.onListUpdated();

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            String myObjectKey = dataSnapshot.getKey();
            if(myObjectsKeyIndexMapping.containsKey(myObjectKey))
            {
                int index = myObjectsKeyIndexMapping.get(myObjectKey);
                myObjects.remove(index);
                recreateKeyIndexMapping();
            }

            if(updateListener != null)
                updateListener.onListUpdated();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    private static class SingletonHolder{
        public static final MyObjectData instance = new MyObjectData();
    }
    public static MyObjectData getInstance(){
        return SingletonHolder.instance;
    }


    public ArrayList<Object> getMyObjects() {
        return myObjects;
    }
    public void addNewPlace(Object object) {
        String key = database.push().getKey();
        myObjects.add(object);
        myObjectsKeyIndexMapping.put(key,myObjects.size() - 1);
        database.child(FIREBASE_CHILD).child(key).setValue(object);
        object.key = key;
    }

    public Object getObject(int index)
    {
        return myObjects.get(index);
    }
    public void deleteObject(int index) {
        database.child(FIREBASE_CHILD).child(myObjects.get(index).key).removeValue();
        myObjects.remove(index);
        recreateKeyIndexMapping();
    }

    public void updateObject(int index, String nme, String desc, String categ,String lng, String lat)
    {
        Object object = myObjects.get(index);
        object.name = nme;
        object.description = desc;
        object.category = categ;
        object.longitude = lng;
        object.latitude = lat;
        database.child(FIREBASE_CHILD).child(object.key).setValue(object);
    }

    private void recreateKeyIndexMapping() {
        myObjectsKeyIndexMapping.clear();
        for(int i = 0;i<myObjects.size();i++) {
            myObjectsKeyIndexMapping.put(myObjects.get(i).key,i);
        }
    }

    public interface ListUpdatedEventListener {
        void onListUpdated();
    }
    public void setEventListener(ListUpdatedEventListener listener){
        updateListener = listener;
    }

    public DatabaseReference getRef(){
        return this.database;
    }
}

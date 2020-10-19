package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class AR_activity_add_new_object extends AppCompatActivity implements  View.OnClickListener, AdapterView.OnItemSelectedListener{

    private ArFragment arFragment;
    private ArrayList<AR_object> myObjects;
    private HashMap<String,Integer> myObjectsKeyIndexMapping;
    MyObjectData.ListUpdatedEventListener updateListener;


    public static String objectKey;



    Button button;
    Double latitude;
    Double longitude;




    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r_activity_add_new_object);



        /////
        myObjects = new ArrayList<AR_object>();
        myObjectsKeyIndexMapping = new HashMap<String,Integer>();

        /////


        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        // adding listener for detecting plane
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();

            // adding model to the scene
            ModelRenderable.builder()
                    .setSource(this, Uri.parse("TocoToucan.sfb"))
                    .build()
                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable));
            getCurrentLocation();





        });


        ////klikom na dugme upload u bazu
        button = (Button) findViewById(R.id.btnAddARObject);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference uidRef = rootRef.child("ar_objects").child(uid);

                AR_object ar = new AR_object(latitude, longitude);


                objectKey=ar.getKey();
                AR_object_data.getInstance().addnewArObject(ar);

                Toast.makeText(getApplicationContext(),"AR object added.",Toast.LENGTH_LONG);


                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                uidRef.addListenerForSingleValueEvent(valueEventListener);
            }

        });

    }

    ///////////////otvaranje kamere postavljanje objekta klikom, klik na dugme upload u bazu
    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode node = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem()); //  for moving, resizing object
        transformableNode.setParent(node); // need to attach to parent
        transformableNode.setRenderable(modelRenderable);

        arFragment.getArSceneView().getScene().addChild(node); // adding only parent node, so the child nodes will be added automatically
        transformableNode.select();
    }

    private void getCurrentLocation()
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference uidRef = rootRef.child("UsersLocation").child(uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 latitude = dataSnapshot.child("latitude").getValue(Double.class);
                 longitude = dataSnapshot.child("longitude").getValue(Double.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT); //Don't ignore errors!
            }



        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

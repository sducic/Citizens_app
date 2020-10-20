package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.ColorSpace;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class AR_showObject extends AppCompatActivity {

    private ArFragment arFragment;
    Double lat;
    Double lon;
    double curLat;
    double curLon;
    DatabaseReference ref;

    AnchorNode anchorNode;
    private Renderable renderable;
    private ModelRenderable andyRenderable;

    Scene scene;
    Camera camera;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_show_object);





        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

       /* Toast.makeText(getApplicationContext(),"Tap to see",Toast.LENGTH_LONG).show();

        // adding listener for detecting plane
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();


            // adding model to the scene
            ModelRenderable.builder()
                    .setSource(this, Uri.parse("TocoToucan.sfb"))
                    .build()
                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable));






        });*/

        CustomArFragment fragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        scene = fragment.getArSceneView().getScene();
        camera = scene.getCamera();


        ModelRenderable.builder()
                .setSource(this, Uri.parse("StopSign_1358.sfb"))
                .build()
                .thenAccept(modelRenderable -> {

                    // for (int i = 0; i < 10; i++){

                    Node node = new Node();
                    node.setRenderable(modelRenderable);



                    Random random = new Random();
                    float x = random.nextInt(8) - 4f;
                    float y = random.nextInt(2);
                    float z = random.nextInt(4);

                    Vector3 position = new Vector3( x, y, -z - 5f);
                    Vector3 worldPosition = scene.getCamera().getWorldPosition();


                    node.setWorldPosition(position);
                    node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 230));

                    // scene.addChild(node);
                    addModelToScene(modelRenderable);
                    // }
                });



    }

   /* private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode node = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem()); //  for moving, resizing object
        transformableNode.setParent(node); // need to attach to parent
        transformableNode.setRenderable(modelRenderable);

        arFragment.getArSceneView().getScene().addChild(node); // adding only parent node, so the child nodes will be added automatically
        transformableNode.select();
    }
*/


    private void addModelToScene( ModelRenderable modelRenderable) {
        AnchorNode node = new AnchorNode();
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem()); //  for moving, resizing object
        transformableNode.setParent(node); // need to attach to parent
        transformableNode.setRenderable(modelRenderable);

        transformableNode.getScaleController().setMaxScale(0.02f);
        transformableNode.getScaleController().setMinScale(0.01f);

        arFragment.getArSceneView().getScene().addChild(node); // adding only parent node, so the child nodes will be added automatically
        transformableNode.select();
    }




}

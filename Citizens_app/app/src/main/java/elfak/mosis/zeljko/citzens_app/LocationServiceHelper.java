package elfak.mosis.zeljko.citzens_app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import static android.content.Context.ACTIVITY_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class LocationServiceHelper {

    public static void startLocationService(Context ctx) {
        if(!isLocationServiceRunning(ctx)) {
            Intent serviceIntent  = new Intent(ctx, LocationService.class);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(serviceIntent);
                Toast.makeText(ctx, "udjomo", Toast.LENGTH_SHORT).show();
            }
            else {
                ctx.startService(serviceIntent);
            }

        }

    }

    public static void stopLocationService(Context ctx) {
        ctx.stopService(new Intent(ctx, LocationService.class));
    }

    private static boolean isLocationServiceRunning(Context ctx) {
        ActivityManager manager = (ActivityManager)ctx.getSystemService(ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if("elfak.mosis.zeljko.citzens_app.LocationService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

package elfak.mosis.zeljko.citzens_app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    public static void displayNotification(Context ctx, String title, String text, String user_id) {

        Intent intent = new Intent(ctx, UserProfileActivity.class);
        intent.putExtra("user_id", user_id);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx,
                100,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, HomePage.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat mManagerCompat = NotificationManagerCompat.from(ctx);
        mManagerCompat.notify(1, mBuilder.build());

    }

    public static void sendNotificationNearbyObjects(Context ctx) {

        Intent intent = new Intent(ctx, MapsActivityZara.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx,
                100,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, HomePage.CHANNEL_ID)
                .setSmallIcon(R.drawable.newsfeedicon)
                .setContentTitle("Notification")
                .setContentText("Looks like there is a friend nearby!")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat mManagerCompat = NotificationManagerCompat.from(ctx);
        mManagerCompat.notify(1, mBuilder.build());
    }
}

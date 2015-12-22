package com.squirrel.mynotes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;

/**
 * Created by squirrel on 12/21/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        generateNotification(context, intent.getStringExtra(Constants.NOTIFICATION));
    }

    private void generateNotification(Context context, String reminder) {
        Note aNote = new Note(reminder);
        String description = aNote.getDescription();
        if(aNote.getType().equals(Constants.LIST)) {
            description="";
        }
        Intent notificationIntent = new Intent(context, NoteDetailActivity.class);
        notificationIntent.putExtra(Constants.NOTIFICATION, reminder);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_calendar_clock_grey600_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_calendar_clock_grey600_24dp))
                .setFullScreenIntent(resultPendingIntent, true)
                .setAutoCancel(true)
                .setContentTitle(aNote.getTitle())
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setContentText(description);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(aNote.getId(), builder.build());
    }
}

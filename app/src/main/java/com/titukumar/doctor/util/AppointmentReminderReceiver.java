package com.titukumar.doctor.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.titukumar.doctor.R;
import com.titukumar.doctor.activities.AppointmentActivity;

public class AppointmentReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "appointment_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String doctorName = intent.getStringExtra("doctorName");
        String appointmentDate = intent.getStringExtra("appointmentDate");

        if (doctorName == null) doctorName = "Your doctor";
        if (appointmentDate == null) appointmentDate = "";

        createNotificationChannel(context);

        Intent openAppIntent = new Intent(context, AppointmentActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Appointment Reminder")
                .setContentText("You have an appointment with " + doctorName +
                        (appointmentDate.isEmpty() ? "" : " on " + appointmentDate))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Appointments",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifies about upcoming appointments");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}

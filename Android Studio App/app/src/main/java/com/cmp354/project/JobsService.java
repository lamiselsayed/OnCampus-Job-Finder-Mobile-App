package com.cmp354.project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Timer;
import java.util.TimerTask;

public class JobsService extends Service {
    private String logTAG = "Project", name;
    private FirebaseFirestore db;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;
    private boolean checkTA, checkRA, checkLA, checkIT, checkEV;
    private long timeTA, timeRA, timeLA, timeIT, timeEV;
    private Timer timer;

    @Override
    public void onCreate() {
        Log.d(logTAG, "Service Created");
        db = FirebaseFirestore.getInstance(); // accessing the database on Firebase
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(logTAG, "Service Started");
        name = intent.getStringExtra("name");
        startTimer();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(logTAG, "Service Bound - Not Used!");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(logTAG, "Service Destroyed");
        stopTimer();
    }

    private void startTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() // running in the background
            {
                Log.d(logTAG, "Timer Task Started");

                sharedPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
                checkTA = sharedPrefs.getBoolean("cbTA" + name, true);
                checkRA = sharedPrefs.getBoolean("cbRA" + name, true);
                checkLA = sharedPrefs.getBoolean("cbLA" + name, true);
                checkIT = sharedPrefs.getBoolean("cbIT" + name, true);
                checkEV = sharedPrefs.getBoolean("cbEV" + name, true);

                timeTA = sharedPrefs.getLong("timeTA" + name, -1);
                timeRA = sharedPrefs.getLong("timeRA" + name, -1);
                timeLA = sharedPrefs.getLong("timeLA" + name, -1);
                timeIT = sharedPrefs.getLong("timeIT" + name, -1);
                timeEV = sharedPrefs.getLong("timeEV" + name, -1);

                // notify users of new job updates depending on which job type they're interested in
                if(checkTA) getUpdates("Teaching Assistant", timeTA, "timeTA" + name, 1);
                if(checkRA) getUpdates("Research Assistant", timeRA, "timeRA" + name, 2);
                if(checkLA) getUpdates("Library Assistant", timeLA, "timeLA" + name, 3);
                if(checkIT) getUpdates("IT (CEN-CAS)", timeIT, "timeIT" + name, 4);
                if(checkEV) getUpdates("Event Volunteer", timeEV, "timeEV" + name, 5);
            }
        };

        timer = new Timer(true);
        int delay = 0; // start instantly
        int interval = 5000; // check every five seconds
        timer.schedule(task, delay, interval);
    }

    private void stopTimer() {
        if (timer != null)
            timer.cancel();
    }

    public void getUpdates(String type, long currentTime, String key, int notifID)
    {
        DocumentReference docRef = db.collection("updates").document(type);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    long newTime = (long) documentSnapshot.get(type);
                    if (newTime > currentTime)
                    {
                        editor = sharedPrefs.edit();
                        editor.putLong(key, newTime);
                        editor.commit();
                        sendNotifications("New " + type + " Job Available!", notifID, type);
                        Log.d(logTAG, "New Job Available");
                    }
                    else Log.d(logTAG, "No Job Updates Available");
                }
            }
        });
    }

    public void sendNotifications(String message, int NOTIFICATION_ID, String type)
    {
        NotificationChannel notificationChannel =
                new NotificationChannel("Channel_ID", "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager manager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);

        // create the intent for the notification
        // when pressed, user is directed to the job details page
        Intent notificationIntent = new Intent(this, ViewJobsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("name", name); // needed in case we press 'home' after viewing the master
        notificationIntent.putExtra("type", type); // needed to display a filtered list once you press on the notification

        // create the pending intent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, flags);

        // create the variables for the notification
        int icon = R.drawable.ic_jobs_notifs;
        CharSequence tickerText = "Job Update Available";
        CharSequence contentTitle = getText(R.string.app_name);
        CharSequence contentText = message;

        // create the notification and set its data
        Notification notification = new NotificationCompat
                .Builder(this, "Channel_ID")
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        manager.notify(NOTIFICATION_ID, notification);
    }

}
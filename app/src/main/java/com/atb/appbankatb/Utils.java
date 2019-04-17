package com.atb.appbankatb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.atb.appbankatb.AccountServices.AccountServicesActivity;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.atomic.AtomicLong;

import static java.security.AccessController.getContext;

public  class Utils {

    public static String msgEmptyField = "S'il vous plait, remplissez tous les champs";
    private static final AtomicLong LAST_TIME_MS = new AtomicLong();


    public static void displayMessage(Context c, String msg) {


        Toast toast = Toast.makeText(c, msg, Toast.LENGTH_SHORT);
        View view = toast.getView();

//Gets the actual oval background of the Toast then sets the colour filter
        view.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

//Gets the TextView from the Toast so it can be editted
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(Color.WHITE);

        toast.show();
    }


    public static int generateRandInt(int min,int max)
    {
        return min + (int)(Math.random() * ((max - min) + 1));
    }


    public static long uniqueCurrentTimeMS() {
        long now = System.currentTimeMillis();
        while(true) {
            long lastTime = LAST_TIME_MS.get();
            if (lastTime >= now)
                now = lastTime+1;
            if (LAST_TIME_MS.compareAndSet(lastTime, now))
                return now;
        }
    }



    public static void notificationDialog(Context c) {
        Intent intent = new Intent(c, AccountServicesActivity.class);
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "tutorialspoint_01";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notificationChannel.setDescription("Sample Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(c, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Tutorialspoint")
                //.setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Vente Service")
                .setContentText("Vous avez un nouveau vente dans vos services .")
                
                .setContentInfo("Information");
        notificationManager.notify(1, notificationBuilder.build());
    }



    public static void addRealtimeUpdate(Context c, FirebaseFirestore firebaseFirestore, String currentUID) {


        firebaseFirestore.collection("mysales")
                .whereEqualTo("id_owner_service", currentUID)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("listen:error", "listen:error", e);
                            return;
                        }


                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d("add", "New doc: " + dc.getDocument().getData());
                                    Utils.notificationDialog(c);
                                    Toast.makeText(c, ""+ dc.getDocument().getData(), Toast.LENGTH_LONG).show();

                                    break;
                                case MODIFIED:
                                    Toast.makeText(c, "hellllllllllllllooooo", Toast.LENGTH_SHORT).show();

                                    Log.d("modif", "Modified doc: " + dc.getDocument().getData());

                                    break;
                                case REMOVED:
                                    Log.d("remove", "Removed doc: " + dc.getDocument().getData());
                                    break;
                            }
                        }

                    }
                });

    }

}

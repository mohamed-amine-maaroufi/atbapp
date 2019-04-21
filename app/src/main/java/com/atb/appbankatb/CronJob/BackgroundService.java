package com.atb.appbankatb.CronJob;

import android.app.Service;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import com.atb.appbankatb.R;
import com.atb.appbankatb.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BackgroundService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mFirebaseAuth;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        String currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        int time = 3600000 ; //24hours = 3600000 milliseconds


       // Toast.makeText(this, "Service created!", Toast.LENGTH_SHORT).show();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {



                firebaseFirestore.collection(getString(R.string.collection_comptes))
                        .document(mFirebaseAuth.getUid())
                        .update(
                                "total_transaction_perday", 0
                        );

                handler.postDelayed(runnable, time);
            }
        };

        handler.postDelayed(runnable, time);
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        //Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
        Log.d("service in backg", "service Stopped");
         }

    @Override
    public void onStart(Intent intent, int startid) {
        Log.d("service in backg", "Service started by user.");

        //Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
    }
}

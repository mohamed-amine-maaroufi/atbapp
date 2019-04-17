package com.atb.appbankatb.Home;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.atb.appbankatb.AccountServices.AccountServicesActivity;
import com.atb.appbankatb.BuyingService.ScanQrCodeActivity;
import com.atb.appbankatb.CronJob.BackgroundService;
import com.atb.appbankatb.GenerateQrCode.GenerateQrCodeActivity;
import com.atb.appbankatb.R;
import com.atb.appbankatb.Signin.SigninActivity;
import com.atb.appbankatb.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

import static android.support.constraint.Constraints.TAG;


public class HomeActivity extends AppCompatActivity  {

    private Button btnAccountServices, btnGenerateQrcode, btnBuyService;
    private Toolbar toolbar;

    private final int REQUEST_PERMISSION=1;
    private FirebaseFirestore firebaseFirestore;
    private String sessionId;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        firebaseFirestore = FirebaseFirestore.getInstance();
        sessionId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //check permission to write to the storage
        checkPermission();




        if (!isMyServiceRunning(HomeActivity.this, BackgroundService.class)) { // Service class name
           //cron job for transaction per day
            startService(new Intent(this, BackgroundService.class));
        }

        firebaseFirestore = FirebaseFirestore.getInstance();

        String currentUId = getIntent().getStringExtra("SESSION_ID");

        //Log.d("currentUId",currentUId);



        SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        DocumentReference docRef = firebaseFirestore.collection(getString(R.string.collection_comptes)).document(sessionId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());


                        Double ownerofService = document.getDouble("Num_compte");

                        editor.putString("num_compte_currentuser", String.valueOf(ownerofService));
                        editor.commit();

                        Log.d("Num_compte", String.valueOf(ownerofService));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        btnAccountServices = (Button) findViewById(R.id.btnAccountServices);
        btnGenerateQrcode = (Button) findViewById(R.id.btngenerateQrCode);
        btnBuyService = (Button) findViewById(R.id.btnBuyservice);

        btnAccountServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,AccountServicesActivity.class));
            }
        });

        btnGenerateQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,GenerateQrCodeActivity.class);
                Log.d("sessionId", sessionId);
                intent.putExtra("SESSION_ID", sessionId);
                startActivity(intent );
            }
        });


        btnBuyService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,ScanQrCodeActivity.class));
            }
        });
    }



    public static boolean isMyServiceRunning(Activity activity, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Utils.displayMessage(HomeActivity.this, "Cliquez une autre fois pour quitter.");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }




    private void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showExplanation("Besoin de permission", "Vous êtes besoin d'accepter cette permission pour la génération et le sauvgarde des QrCodes" +
                        " de vos services. ", Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSION);
            } else {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSION);
            }
        } else {
            Log.d("tag","Permission (already) Granted!");
        }
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Utils.displayMessage(HomeActivity.this,"Permisssion attribuée");
                } else {
                    Utils.displayMessage(HomeActivity.this,"Permisssion rejetée");
                }
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, SigninActivity.class);
                startActivity(intent);
                return true;
            case R.id.account_services:
                Intent intent2 = new Intent(this, AccountServicesActivity.class);
                startActivity(intent2);
                return true;
            case R.id.generate_qc:
                Intent intent3 = new Intent(this, GenerateQrCodeActivity.class);
                intent3.putExtra("SESSION_ID", sessionId);
                startActivity(intent3);
                return true;
            case R.id.payement_service:
                Intent intent4 = new Intent(this, ScanQrCodeActivity.class);
                startActivity(intent4);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}

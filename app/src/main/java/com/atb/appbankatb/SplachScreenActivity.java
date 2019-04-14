package com.atb.appbankatb;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.atb.appbankatb.R;

import com.atb.appbankatb.Signin.SigninActivity;

import java.net.InetAddress;
import java.util.Calendar;

public class SplachScreenActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 1700;
    AlphaAnimation alpha;
    TextView splashText, poweredbyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splach_screen);
        alpha = new AlphaAnimation(0, 1);
        alpha.setDuration(1600);
        poweredbyText = (TextView) findViewById(R.id.poweredby);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        poweredbyText.setText("Copyright © " + year + " " + getString(R.string.app_name));
        splashText = (TextView) findViewById(R.id.text_splash);
        splashText.setAnimation(alpha);



        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {




                if(isOnline()){
                    startActivity(new Intent(SplachScreenActivity.this, SigninActivity.class));
                }
                else{


                    new AlertDialog.Builder(SplachScreenActivity.this).setTitle("Vérifiez la connexion Internet")
                            .setMessage("Aucune connexion Internet disponible! Activez-le, puis cliquez sur Actualiser.")
                            .setCancelable(false)
                            .setPositiveButton("Quiter",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                finishAffinity();
                                            }
                                            System.exit(1);
                                        }
                                    })
                            .setNegativeButton("Actualiser", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(SplachScreenActivity.this, SplachScreenActivity.class));

                                }
                            })
                            .create()
                            .show();
                }


            }
        }, SPLASH_TIME_OUT);



    }


    //check internet connexion
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }



}

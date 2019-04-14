package com.atb.appbankatb.BuyingService;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import static android.Manifest.permission.CAMERA;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.atb.appbankatb.AccountServices.AccountServicesActivity;
import com.atb.appbankatb.GenerateQrCode.GenerateQrCodeActivity;
import com.atb.appbankatb.Home.HomeActivity;
import com.atb.appbankatb.R;
import com.atb.appbankatb.Signin.SigninActivity;
import com.atb.appbankatb.Utils;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ScanQrCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private Toolbar toolbar;
    private String sessionId;

    private EditText edttext_libelle, edttext_price;
    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView mScannerView;

    private String price = null, libelle = null,id_client = null, id_qrcode = null, ownerofService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

//        toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle(getResources().getString(R.string.app_name));
//        setSupportActionBar(toolbar);



        sessionId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermissioncamera()) {
                Log.d("permissioncamera", "Permission déja attribuée au camera");

            } else {
                requestPermission();
            }
        }

    }

   private boolean checkPermissioncamera() {
        return ( ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA ) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted){



                        Utils.displayMessage(ScanQrCodeActivity.this, "Permission été attribuée au camera");
                    }else {

                        Utils.displayMessage(ScanQrCodeActivity.this, "Permission été réfusée au camera, Vous devez allouer la permission du camera.");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("Vous devez autoriser l'accès aux deux autorisations.",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                        startActivity(new Intent(ScanQrCodeActivity.this,HomeActivity.class));

                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(ScanQrCodeActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Annuler", null)
                .create()
                .show();
    }


    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermissioncamera()) {
                if(mScannerView == null) {
                    mScannerView = new ZXingScannerView(this);
                    setContentView(mScannerView);
                }
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }


    @Override
    public void handleResult(Result rawResult) {

        JSONObject obj = null;


        if(isJSONValid(rawResult.getText())) {

            try {


                obj = new JSONObject(rawResult.getText());

                ownerofService = obj.getString("ownerofService");
                Log.d("ownerofService_obj1",ownerofService);
                if (obj.has("Prix") && obj.has("Libelle") && obj.has("id_client") && obj.has("idCodeQr") && obj.has("ownerofService")) {
                    price = obj.getString("Prix");
                    libelle = obj.getString("Libelle");
                    id_client = obj.getString("id_client");
                    id_qrcode = obj.getString("id_codeqr");
                    ownerofService = obj.getString("ownerofService");

                    Log.d("test","test");
                    Log.d("ownerofService_obj",ownerofService);

                }else{
                    Utils.displayMessage(ScanQrCodeActivity.this, "le QrCode ne correspand pas aux services de ATB banque.");
                    Log.d("myjson qrcode obj", obj.toString());
                    return;

                }






            } catch (JSONException e) {
                Log.e("My App", "Could not parse malformed JSON: \"" + rawResult.getText() + "\"");
                e.printStackTrace();
            }


            final String result = "le prix est : " + price + " et le libelle est : " + libelle + " owner of service : " + ownerofService;

            Log.d("QRCodeScanner", result);
            Log.d("QRCodeScanner", rawResult.getBarcodeFormat().toString());

            Intent intent = new Intent(ScanQrCodeActivity.this, BuyingServiceActivity.class);

            intent.putExtra("price", price);
            intent.putExtra("libelle", libelle);
            intent.putExtra("id_client", id_client);
            intent.putExtra("id_qrcode", id_qrcode);
            intent.putExtra("ownerofService", ownerofService);
            intent.putExtra("SESSION_ID", sessionId);


            startActivity(intent);

        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scanner Resultat");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mScannerView.resumeCameraPreview(ScanQrCodeActivity.this);
            }
        });

        builder.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result));
                startActivity(browserIntent);
            }
        });

        builder.setMessage(result);
        AlertDialog alert1 = builder.create();
        alert1.show();*/

        }else{
            Utils.displayMessage(ScanQrCodeActivity.this, "le QrCode ne correspand pas aux services de ATB banque.");

        }
    }


    //function to ckech if string is a valid json structure
    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(this,HomeActivity.class));
    }

}

package com.atb.appbankatb.GenerateQrCode;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.atb.appbankatb.AccountServices.AccountServicesActivity;

import com.atb.appbankatb.BuyingService.ScanQrCodeActivity;
import com.atb.appbankatb.Home.HomeActivity;
import com.atb.appbankatb.R;
import com.atb.appbankatb.Signin.SigninActivity;
import com.atb.appbankatb.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.support.constraint.Constraints.TAG;


public class GenerateQrCodeActivity extends AppCompatActivity {


    private Button btngenerateQrc;
    private EditText editText_libelle, editText_price;
    private ImageView image_qrcode;
    private Toolbar toolbar;
    private FirebaseFirestore firebaseFirestore;
    public Double solde = 0.0, ownerofService;
    public ProgressDialog mDialog;
    Animation performAnimation;
    //String ownerofService ;

    private final int REQUEST_PERMISSION=1;


    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr_code);

       // ActivityCompat.requestPermissions(GenerateQrCodeActivity.this,
                //new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        //checkPermission to write in storage
        checkPermission();

        //initialize the storage of firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        final Context context = this;
        editText_libelle = (EditText) findViewById(R.id.libelle);
        editText_price = (EditText) findViewById(R.id.price);
        btngenerateQrc = (Button) findViewById(R.id.btngenerate_qc);
        image_qrcode = (ImageView) findViewById(R.id.image_qrcode);

        performAnimation = AnimationUtils.loadAnimation(GenerateQrCodeActivity.this, R.anim.shake_view);
        performAnimation.setRepeatCount(4);

        mDialog = new ProgressDialog(GenerateQrCodeActivity.this);
        mDialog.setMessage("veuillez patienter ...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);




        firebaseFirestore = FirebaseFirestore.getInstance();

        String currentUId = getIntent().getStringExtra("SESSION_ID");

        Log.d("currentUId",currentUId);

        SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();


        DocumentReference docRef = firebaseFirestore.collection(getString(R.string.collection_comptes)).document(currentUId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        solde = document.getDouble("solde");
                        ownerofService = document.getDouble("Num_compte");

                        editor.putString("num_compte_currentuser", String.valueOf(ownerofService));
                        editor.commit();

                        Log.d("solde", String.valueOf(solde));
                        Log.d("Num_compte", String.valueOf(ownerofService));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });






        btngenerateQrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("solde avant submit", String.valueOf(solde));

                    mDialog.show();

                    String str_libelle = editText_libelle.getText().toString();
                    String str_price = editText_price.getText().toString();



                    if (TextUtils.isEmpty(str_libelle)) {
                        Utils.displayMessage(GenerateQrCodeActivity.this, "S'il vous remplissez le libelle.");
                        editText_libelle.startAnimation(performAnimation);

                        mDialog.dismiss();
                        return;
                    }

                    if (TextUtils.isEmpty(str_price)) {
                        Utils.displayMessage(GenerateQrCodeActivity.this, "S'il vous plait, remplissez le prix");
                        editText_price.startAnimation(performAnimation);

                        mDialog.dismiss();
                        return;
                    }


                    double price = Double.parseDouble(str_price);

                    if (price > 250) {
                        Utils.displayMessage(GenerateQrCodeActivity.this, "S'il vous plait,le prix doit être inférieur ou égale à 250 DT");
                        editText_price.startAnimation(performAnimation);

                        mDialog.dismiss();
                        return;
                    }



                    if (price + solde > 500) {
                        Utils.displayMessage(GenerateQrCodeActivity.this, "S'il vous plait,la somme du prix et de votre solde doit être inférieur à 500 DT");
                        editText_price.startAnimation(performAnimation);

                        mDialog.dismiss();
                        return;
                    }

                     mDialog.show();

                    //close keyboard
                    if (((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).isAcceptingText()) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                    }




                    JSONObject jsonObj = new JSONObject();
                    //get the current time with millisecond // we need it to create a unique ID for each codeqr
                    long currenttimeMS = Utils.uniqueCurrentTimeMS();
                //create qrCode unique ID
                    final String idCodeqr = UUID.randomUUID().toString() + currenttimeMS;

                    try {

                        jsonObj.put("idCodeQr", idCodeqr);
                        jsonObj.put("id_client", currentUId);
                        jsonObj.put("Libelle", str_libelle);
                        jsonObj.put("Prix", str_price);
                        jsonObj.put("ownerofService", ownerofService);
                        Log.d("ownerofserviceObj", String.valueOf(ownerofService));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mDialog.dismiss();
                    }

                    Uri uri = null;
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(jsonObj.toString(), BarcodeFormat.QR_CODE, 400, 400);

                        int height = bitMatrix.getHeight();
                        int width = bitMatrix.getWidth();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bmp.setPixel(x, y, bitMatrix.get(x, y) ? ContextCompat.getColor(context, R.color.colorAccent) :
                                        ContextCompat.getColor(context, R.color.colorPrimaryDark));
                            }
                        }

                        uri = getImageUri(GenerateQrCodeActivity.this, bmp);
                        image_qrcode.setImageBitmap(bmp);


                    } catch (WriterException e) {
                        e.printStackTrace();
                        mDialog.dismiss();
                    }

                    editText_price.setText("");
                    editText_libelle.setText("");
                    Utils.displayMessage(GenerateQrCodeActivity.this, "Vous avez généré un nouveau QR Code pour votre service");


                    final StorageReference ref_qrcode = storageReference.child("images_codeQR/" + currentUId + idCodeqr + ".png");
                    ref_qrcode.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            ref_qrcode.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Map<String, Object> codeqrMap = new HashMap<>();
                                    codeqrMap.put("libelle", str_libelle);
                                    codeqrMap.put("image", uri.toString());
                                    codeqrMap.put("prix", str_price);
                                    codeqrMap.put("id_client", currentUId);
                                    codeqrMap.put("id_codeqr", idCodeqr);
                                    codeqrMap.put("ownerofService", ownerofService);
                                    Log.d("ownerofService2", String.valueOf(ownerofService));

                                    firebaseFirestore.collection(getString(R.string.collection_codeqrs)).document(idCodeqr)
                                            .set(codeqrMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDialog.dismiss();
                                            Utils.displayMessage(GenerateQrCodeActivity.this, "CodeQr été sauvgardé");
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mDialog.dismiss();
                                            Utils.displayMessage(GenerateQrCodeActivity.this, "Error: " + e.getMessage());

                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mDialog.dismiss();
                                    Utils.displayMessage(GenerateQrCodeActivity.this, "Failed " + e.getMessage());

                                }
                            });

                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Utils.displayMessage(GenerateQrCodeActivity.this, "Failed " + e.getMessage());

                    }
                });









                    /******************
                    StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
                    ref.putFile(uri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //progressDialog.dismiss();





                                    mDialog.dismiss();
                                    Utils.displayMessage(GenerateQrCodeActivity.this, "CodeQr été sauvgardé.");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //progressDialog.dismiss();
                                    mDialog.dismiss();
                                    Utils.displayMessage(GenerateQrCodeActivity.this, "Failed " + e.getMessage());
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                            .getTotalByteCount());
                                }
                            });


                    /**************** */


            }

        });
        //to close keyboard when tap out of edittext
        findViewById(R.id.layoutparent).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).isAcceptingText()) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                }
                return true;

            }
        });
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
                    Utils.displayMessage(GenerateQrCodeActivity.this,"Permisssion attribuée");
                } else {
                    Utils.displayMessage(GenerateQrCodeActivity.this,"Permisssion rejetée");
                    startActivity(new Intent(GenerateQrCodeActivity.this, HomeActivity.class));
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
            case R.id.payement_service:
                Intent intent4 = new Intent(this, ScanQrCodeActivity.class);
                startActivity(intent4);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




    //register qrcode

    public Uri getImageUri(Context inContext, Bitmap inImage) {


            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
            Log.d("path", path);
            Log.d("path", Uri.parse(path).toString());



        return Uri.parse(path);

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(this,HomeActivity.class));
    }


}

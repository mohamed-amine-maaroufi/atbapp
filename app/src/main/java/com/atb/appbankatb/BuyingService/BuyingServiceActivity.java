package com.atb.appbankatb.BuyingService;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.atb.appbankatb.AccountServices.AccountServicesActivity;
import com.atb.appbankatb.GenerateQrCode.GenerateQrCodeActivity;
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
import com.google.firebase.firestore.QuerySnapshot;

import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.grpc.okhttp.internal.Util;

public class BuyingServiceActivity extends AppCompatActivity {

    private EditText edttext_price, edttext_libelle;
    private String price,libelle, id_client, id_qrcode,ownerofService;
    private String sessionId;
    private Toolbar toolbar;
    private Button btn_buy, btn_rescan;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog mDialog;
    private String currentUId;
    SharedPreferences shared;
    String num_compte_currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buying_service);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        sessionId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUId = getIntent().getStringExtra("SESSION_ID");

        shared = getSharedPreferences("Prefs", MODE_PRIVATE);
        num_compte_currentuser = (shared.getString( "num_compte_currentuser", ""));


        mDialog = new ProgressDialog(BuyingServiceActivity.this);
        mDialog.setMessage("veuillez patienter ...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        price = getIntent().getStringExtra("price");
        libelle = getIntent().getStringExtra("libelle");
        id_client = getIntent().getStringExtra("id_client");
        id_qrcode = getIntent().getStringExtra("id_qrcode");
        ownerofService = getIntent().getStringExtra("ownerofService");
        //Log.d("ownerofService3",ownerofService);

        edttext_price = (EditText) findViewById(R.id.price);
        edttext_libelle = (EditText) findViewById(R.id.libelle);
        btn_buy = (Button) findViewById(R.id.btn_buy) ;
        btn_rescan = (Button) findViewById(R.id.btng_rescan) ;

        edttext_price.setText(price.toString());
        edttext_libelle.setText(libelle.toString());

        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mDialog.show();

                DocumentReference docRefTransaction = firebaseFirestore.collection(getString(R.string.collection_transactions)).document(currentUId);
                DocumentReference docRefCompte = firebaseFirestore.collection(getString(R.string.collection_comptes)).document(id_client);
                DocumentReference docRefCompteConnectedUser = firebaseFirestore.collection(getString(R.string.collection_comptes)).document(currentUId);

                Log.d("id_client",id_client);

                Log.d("currentUId",currentUId);




                //function to check the time every 24 hours
                //we need it to intialize the value of total_transaction_perday in the collection comptes to zero for the current user

                Calendar calendar = Calendar.getInstance();
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                SharedPreferences settings = getSharedPreferences("PREFS",0);
                int lastDay = settings.getInt("day",0);

                    if(lastDay != currentDay){
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("day",currentDay);
                        editor.commit();

                        //excute task


                    }







                //convert the price of the service (qrcode) from string to double
                Double price1 = Double.parseDouble(price);

                docRefCompteConnectedUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful()){
                            DocumentSnapshot documentCurrentUser = task.getResult();

                            double balance_currentUser = documentCurrentUser.getDouble("solde");
                            double total_transaction_perday = documentCurrentUser.getDouble("total_transaction_perday");

                            if(balance_currentUser < price1){
                                mDialog.dismiss();
                                Utils.displayMessage(BuyingServiceActivity.this, "Vous n'avez pas assez d'argent pour achéter ce service.");
                            }else if(total_transaction_perday + price1 > 250){
                                mDialog.dismiss();
                                Utils.displayMessage(BuyingServiceActivity.this, "Vous ne pouvez pas passer des transactions ,leur totale supérieur 250 DT par day.");


                            } else{


                                docRefCompte.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();

                                            if (document.exists()) {
                                                Log.d("DocumentSnapshot", "DocumentSnapshot data: " + document.getData());

                                                Double old_balance = document.getDouble("solde");

                                                //Double old_balance1 = Double.parseDouble(old_balance);

                                                Double newbalance = old_balance + price1;

                                                //update the balance of the owner of the service
                                                docRefCompte.update("solde", newbalance)
                                                        .addOnSuccessListener(new OnSuccessListener< Void >() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                mDialog.dismiss();
                                                                Utils.displayMessage(BuyingServiceActivity.this, "Vous avez achété ce service.");

                                                            }
                                                        });


                                                //update the balance of the owner of the service
                                                Double newbalanceCurrentUser = balance_currentUser - price1;
                                                Double newtotal_transaction_perday = total_transaction_perday + price1;
                                                docRefCompteConnectedUser.update("solde",newbalanceCurrentUser ,
                                                        "total_transaction_perday",newtotal_transaction_perday)
                                                        .addOnSuccessListener(new OnSuccessListener< Void >() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                  Log.d("new_balance", "balance of current solde was updated");
                                                            }

                                                        });



                                                /**************/
                                                 //add new transaction
                                                /**************/
                                                long currenttimeMS = Utils.uniqueCurrentTimeMS();

                                                //get current date
                                                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                                Date date = new Date();

                                                //create qrCode unique ID
                                                final String id_transaction = UUID.randomUUID().toString() + currenttimeMS;

                                                Map<String, Object> transactionMap = new HashMap<>();
                                                transactionMap.put("id_client", currentUId);
                                                transactionMap.put("id_owner_service", id_client);
                                                transactionMap.put("ownerofService", ownerofService);
                                                transactionMap.put("id_transaction", id_transaction);
                                                transactionMap.put("montant", price1);
                                                transactionMap.put("libelle", libelle);
                                                transactionMap.put("date_transaction", dateFormat.format(date));

                                                //add transaction collection for the current user
                                                firebaseFirestore.collection(getString(R.string.collection_transactions)).document(id_transaction)
                                                        .set(transactionMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("add_transaction","on success: adding new transaction" );
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("add_transaction","failed to add new transaction" );

                                                    }
                                                });




                                                Map<String, Object> salesMap = new HashMap<>();
                                                salesMap.put("id_owner_service", id_client);
                                                salesMap.put("buyer", num_compte_currentuser);
                                                salesMap.put("id_transaction", id_transaction);
                                                salesMap.put("montant", price);
                                                salesMap.put("libelle", libelle);
                                                salesMap.put("date_transaction", dateFormat.format(date));

                                                //add transaction collection for the current user
                                                firebaseFirestore.collection(getString(R.string.collection_mysales)).document(id_transaction)
                                                        .set(salesMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("add_transaction_sales","on success: adding new sale" );
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("add_transaction_sales","failed to add new sale" );

                                                    }
                                                });




                                            } else {
                                                mDialog.dismiss();
                                                Log.d("no document", "No such document");
                                                Utils.displayMessage(BuyingServiceActivity.this, "Désolé c'est ancien QrCode. Ce service n'est pas disponible.");
                                            }
                                        } else {
                                            mDialog.dismiss();
                                            Log.d("failed to get document", "get failed with ", task.getException());
                                        }
                                    }
                                });



                            }
                        }
                    }
                });



               /* double newsolde = 220.0;

               DocumentReference docRef = firebaseFirestore.collection(getString(R.string.collection_comptes)).document(id_client);
               Log.d("document comptes", String.valueOf(docRef));
                docRef.update("solde", newsolde)
                        .addOnSuccessListener(new OnSuccessListener< Void >() {
                            @Override
                            public void onSuccess(Void aVoid) {



                                Toast.makeText(BuyingServiceActivity.this, "Vous avez achété ce service.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });


               /* DocumentReference docRef = firebaseFirestore.collection(getString(R.string.collection_clients)).document(id_qrcode);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("DocumentSnapshot data", "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d("no document", "No such document");
                            }
                        } else {
                            Log.d("failed to get document", "get failed with ", task.getException());
                        }
                    }
                });*/
            }
        });




        btn_rescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BuyingServiceActivity.this,ScanQrCodeActivity.class));
            }
        });
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

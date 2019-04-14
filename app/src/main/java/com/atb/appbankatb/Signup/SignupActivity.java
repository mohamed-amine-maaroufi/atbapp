package com.atb.appbankatb.Signup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.atb.appbankatb.Beans.Client;
import com.atb.appbankatb.Home.HomeActivity;
import com.atb.appbankatb.R;
import com.atb.appbankatb.Signin.SigninActivity;
import com.atb.appbankatb.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.grpc.Compressor;

import static java.security.AccessController.getContext;

public class SignupActivity extends AppCompatActivity {

    EditText login,password, firstname, lastname, tel, adress;
    TextView loginButton;
    Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    public StorageReference storageReference;
    public ProgressDialog mDialog;
    Animation performAnimation;
   // ImageView androidImageView;

    public Uri imageUri;

    public String str_login, str_password, str_firstname,str_lastname,str_adress,str_tel;

    public  String userUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

       /* toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);*/

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("images");
        firebaseFirestore = FirebaseFirestore.getInstance();
        imageUri=null;
        //UserHelper userHelper = new UserHelper(this);


        login = (EditText) findViewById(R.id.edittext_login);
        password = (EditText) findViewById(R.id.edittext_password);
        firstname = (EditText) findViewById(R.id.edittext_firstname);
        lastname = (EditText) findViewById(R.id.edittext_lastname);
        adress = (EditText) findViewById(R.id.edittext_address);
        tel = (EditText) findViewById(R.id.edittext_tel);

        performAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_view);
        performAnimation.setRepeatCount(4);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("veuillez patienter ...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);


        registerButton = (Button) findViewById(R.id.btnsignup);
        loginButton = (TextView) findViewById(R.id.btn_go_to_signin);



        //to close keyboard when tap out of edittext
        findViewById(R.id.layoutparent).setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).isAcceptingText()) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                }
                return true;

            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, SigninActivity.class));
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               // if(imageUri!=null){
                 str_login = login.getText().toString();
                 str_password = password.getText().toString();
                 str_firstname = firstname.getText().toString();
                 str_lastname = lastname.getText().toString();
                 str_adress = adress.getText().toString();
                 str_tel = tel.getText().toString();

                mDialog.show();





                if (TextUtils.isEmpty(str_lastname)) {
                    Utils.displayMessage(SignupActivity.this, "S'il vous plait, remplissez le nom");
                    lastname.startAnimation(performAnimation);

                    mDialog.dismiss();
                    return;
                }

                if (TextUtils.isEmpty(str_firstname)) {
                    Utils.displayMessage(SignupActivity.this, "S'il vous plait, remplissez le prénom");
                    firstname.startAnimation(performAnimation);
                    mDialog.dismiss();
                    return;
                }

                if (TextUtils.isEmpty(str_tel)) {
                    Utils.displayMessage(SignupActivity.this, "S'il vous plait,remplissez le num de Tel");
                    tel.startAnimation(performAnimation);
                    mDialog.dismiss();
                    return;
                }

                if (TextUtils.isEmpty(str_adress)) {
                    Utils.displayMessage(SignupActivity.this, "S'il vous plait, remplissez l'addresse");
                    adress.startAnimation(performAnimation);
                    mDialog.dismiss();
                    return;
                }

                if (TextUtils.isEmpty(str_login)) {
                    Utils.displayMessage(SignupActivity.this, "S'il vous plait, remplissez le login");
                    login.startAnimation(performAnimation);
                    mDialog.dismiss();
                    return;
                }


                if (TextUtils.isEmpty(str_password)) {
                    Toast.makeText(getApplicationContext(), "S'il vous plait, remplissez tous les champs", Toast.LENGTH_SHORT).show();
                    Utils.displayMessage(SignupActivity.this, "S'il vous plait, remplissez le mot de passe");
                    password.startAnimation(performAnimation);

                    mDialog.dismiss();
                    return;
                }

                if (str_password.length() < 6) {
                    Utils.displayMessage(SignupActivity.this, "Le mot de passe doit être au moins de 6 caractères");
                    password.startAnimation(performAnimation);
                    mDialog.dismiss();
                }


                if (!TextUtils.isEmpty(str_login) || !TextUtils.isEmpty(str_firstname) || !TextUtils.isEmpty(str_adress) ||
                        !TextUtils.isEmpty(str_lastname) || !TextUtils.isEmpty(str_password) || !TextUtils.isEmpty(str_tel)) {

                    firebaseFirestore.collection("Usernames")
                            .document(str_login)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (!documentSnapshot.exists()) {
                                        registerUser();
                                    } else {
                                        Utils.displayMessage(SignupActivity.this, "Login déja existe");
                                        mDialog.dismiss();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Error", e.getMessage());
                                }
                            });

                } else {

                    mDialog.dismiss();

                }

            }
        });


    }


    private void registerUser() {

        mAuth.createUserWithEmailAndPassword(str_login, str_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull final Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    Map<String,Object> usernameMap=new HashMap<String, Object>();
                    usernameMap.put("username",str_login);

                    firebaseFirestore.collection("Usernames")
                            .document(str_login)

                            .set(usernameMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    task.getResult()
                                            .getUser()
                                            .sendEmailVerification()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    userUid = task.getResult().getUser().getUid();




                                                    Map<String, Object> userMap = new HashMap<>();
                                                    userMap.put("id", userUid);
                                                    userMap.put("firstname", str_firstname);
                                                    userMap.put("lasttname", str_lastname);
                                                    userMap.put("adress", str_adress);
                                                    userMap.put("tel", str_tel);
                                                    userMap.put("login", str_login);
                                                    userMap.put("password", str_password);
                                                    //userMap.put("image", uri.toString());




                                                    firebaseFirestore.collection("clients").document(userUid).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {

                                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            //create collection compte
                                                            createCollectionCompte(userUid);

                                                            //create transaction compte
                                                            //createCollectionTransaction(userUid);

                                                            //create collection codeqr
                                                            createCollectionCodeQr(userUid);

                                                            mDialog.dismiss();
                                                            Utils.displayMessage(SignupActivity.this, "Creation de compte avec succées");
                                                            startActivity( new Intent(SignupActivity.this,HomeActivity.class) );

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            mDialog.dismiss();
                                                            Utils.displayMessage(SignupActivity.this, "Error: " + e.getMessage());
                                                        }
                                                    });

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    task.getResult().getUser().delete();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Error",e.getMessage());
                                }
                            });


                } else {
                    mDialog.dismiss();
                    Utils.displayMessage(SignupActivity.this, "Error: " + task.getException().getMessage());

                }
            }
        });

    }


    private void createCollectionCompte(String userUid){

        Map<String, Object> compte = new HashMap<>();
        compte.put("Num_compte", 123456789);
        compte.put("Nom_compte", "compte name");
        compte.put("RIB", (int)Math.random() * 1000);
        compte.put("type_compte", "type compte");
        compte.put("solde", (double)Math.random() * 1000);
        compte.put("total_transaction_perday", 0);
        compte.put("devise", "devise");
        compte.put("id_client", userUid);

        firebaseFirestore.collection(getString(R.string.collection_comptes)).document(userUid).set(compte)
                .addOnSuccessListener(new OnSuccessListener < Void > () {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("compte collection  ", "compte collection added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("compte collection  ", "compte collection error" + e.toString());
                    }
                });

    }





    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createCollectionTransaction(String userUid){

        Map<String, Object> Trasaction = new HashMap<>();

        //get current date and time
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        Trasaction.put("id_trasaction", (int)Math.random() * 1000);
        Trasaction.put("type_trasaction", "type trasaction");
        Trasaction.put("Nom_compte", "compte name");
        Trasaction.put("date_trasction", dtf.format(now));
        Trasaction.put("montant", "type compte");
        Trasaction.put("num_compte", (double)Math.random() * 1000);
        Trasaction.put("id_client", userUid);

        firebaseFirestore.collection(getString(R.string.collection_transactions)).document(userUid).set(Trasaction)
                .addOnSuccessListener(new OnSuccessListener < Void > () {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.d("collection Trasaction","Trasaction added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d("Trasaction ERROR", "Trasaction ERROR" + e.toString());
                    }
                });

    }


    private void createCollectionCodeQr(String userUid){

        //create collection for codeqr
        Map<String, Object> codeqr = new HashMap<>();

        codeqr.put("id_codeqr", (int)Math.random() * 1000);
        codeqr.put("description", "description");
        codeqr.put("Couleur", "couleur");
        codeqr.put("id_client", userUid);

        firebaseFirestore.collection(getString(R.string.collection_codeqrs)).document(userUid).set(codeqr)
                .addOnSuccessListener(new OnSuccessListener < Void > () {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("codeqr collection ","codeqr collection added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("codeqr collection  ", "codeqr collection error" + e.toString());
                    }
                });

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

        Utils.displayMessage(SignupActivity.this, "Cliquez une autre fois pour quitter.");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }


}

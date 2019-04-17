package com.atb.appbankatb.AccountServices;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.atb.appbankatb.BuyingService.ScanQrCodeActivity;
import com.atb.appbankatb.GenerateQrCode.GenerateQrCodeActivity;
import com.atb.appbankatb.Home.HomeActivity;
import com.atb.appbankatb.R;
import com.atb.appbankatb.Signin.SigninActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AccountServicesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PageAdapter pageAdapter;
    private TabItem tabitem_balance;
    private TabItem tabitem_history_transactions;

    private String sessionId;


    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private  String currentUID;
    DocumentReference docRef;
    private DatabaseReference dbRef;
    private FirebaseDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_services);
    toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
        //getSupportActionBar().hide();


        database = FirebaseDatabase.getInstance();

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbRef = database.getReference();
        docRef = firebaseFirestore.collection(getString(R.string.collection_comptes)).document(currentUID);

        /*************************/








        /*************************/

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });


        tabLayout = findViewById(R.id.tablayout_accServ);
        tabitem_balance = findViewById(R.id.tabitem_balance);
        tabitem_history_transactions = findViewById(R.id.tabitem_history_transactions);
        viewPager = findViewById(R.id.viewPager);

        sessionId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                /*if (tab.getPosition() == 1) {
                    toolbar.setBackgroundColor(ContextCompat.getColor(SigninActivity.this,
                            R.color.Dark));
                    tabLayout.setBackgroundColor(ContextCompat.getColor(SigninActivity.this,
                            R.color.Dark));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(ContextCompat.getColor(SigninActivity.this,
                                R.color.Dark));
                    }
                } else {
                    toolbar.setBackgroundColor(ContextCompat.getColor(SigninActivity.this,
                            R.color.colorAccent));
                    tabLayout.setBackgroundColor(ContextCompat.getColor(SigninActivity.this,
                            R.color.colorAccent));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(ContextCompat.getColor(SigninActivity.this,
                                R.color.colorAccent));

                    }
                }*/
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(this,HomeActivity.class));
    }

}

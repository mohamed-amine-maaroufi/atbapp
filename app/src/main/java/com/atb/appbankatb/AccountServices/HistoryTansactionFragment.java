package com.atb.appbankatb.AccountServices;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atb.appbankatb.Beans.Transaction;
import com.atb.appbankatb.GenerateQrCode.GenerateQrCodeActivity;
import com.atb.appbankatb.R;
import com.atb.appbankatb.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.grpc.okhttp.internal.Util;

import static android.support.constraint.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryTansactionFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private  String currentUID;
    private ListView listv_transactions;
    private TransactionAdapter mAdapter;
    private ProgressDialog mDialog;
    private ImageButton selectDate;
    private TextView tv_date,text_for_empty_list;
    private ArrayList<Transaction> listTransactions;
    private ArrayList<Transaction> listFiltredTransactions = new ArrayList<>();;
    private RelativeLayout layoutfilter;

    public HistoryTansactionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history_tansaction, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDialog = new ProgressDialog(getContext());
        mDialog.setMessage("veuillez patienter ...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.show();

        listv_transactions = (ListView) view.findViewById(R.id.list_transactions);
        text_for_empty_list = (TextView) view.findViewById(R.id.text_for_empty_list);
        layoutfilter = (RelativeLayout) view.findViewById(R.id.layoutfilter);
        selectDate = view.findViewById(R.id.btnDate);
        tv_date = view.findViewById(R.id.tvSelectedDate);

        //get the history of transactions for the current user
        getTransactions();



        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                String day1 =  day < 10 ? "0"+ day : "" + day;
                                month = month + 1;
                                String month1 =  month < 10 ? "0"+ month : "" + month;

                                tv_date.setText(day1 + "/" + month1 + "/" + year);

                                for( Transaction transaction : listTransactions){
                                    if(transaction.getDate().equals(tv_date.getText())){
                                        listFiltredTransactions.add(new Transaction(transaction.getLibelle(),transaction.getDate(),transaction.getPrice(),transaction.getOwnerofService()));

                                    }
                                }

                                if (!listFiltredTransactions.isEmpty()){
                                    mAdapter = new TransactionAdapter(getContext(),listFiltredTransactions);
                                    listv_transactions.setAdapter(mAdapter);
                                }else {
                                    Utils.displayMessage(getContext(),"Pas de transactions pour cette date.");
                                }


                            }
                        }, year, month, dayOfMonth);

                datePickerDialog.show();

            }
        });




        return view;
    }


    public void getTransactions(){

        firebaseFirestore.collection(getString(R.string.collection_transactions)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    listTransactions = new ArrayList<>();


                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.get("id_client").equals(currentUID)){
                            Log.d("id_client",document.get("id_client") + " / id_transaction:" + document.get("id_transaction"));

                            listTransactions.add(new Transaction(document.get("libelle"),document.get("date_transaction") , document.get("montant"),document.get("ownerofService")));
                        }

                    }

                    if(listTransactions.isEmpty()){
                        Log.d("list_empty", "there is no transactions available fot this user");
                        layoutfilter.setAlpha(0);
                        mDialog.dismiss();

                    }else{
                        //set the data in listview
                        text_for_empty_list.setAlpha(0);
                        mAdapter = new TransactionAdapter(getContext(),listTransactions);
                        listv_transactions.setAdapter(mAdapter);
                        mDialog.dismiss();
                    }



                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }


}

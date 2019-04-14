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
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.atb.appbankatb.Beans.Transaction;
import com.atb.appbankatb.R;
import com.atb.appbankatb.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

import static android.support.constraint.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class MysalesFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private  String currentUID;
    private ListView listv_sales;
    private SalesAdapter mAdapter;
    private ProgressDialog mDialog;
    private ImageButton selectDate;
    private TextView tv_date,text_for_empty_list;
    private ArrayList<Transaction> listSales;
    private ArrayList<Transaction> listFiltredSales = new ArrayList<>();;
    private RelativeLayout layoutfilter;

    public MysalesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mysales, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDialog = new ProgressDialog(getContext());
        mDialog.setMessage("veuillez patienter ...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.show();

        listv_sales = (ListView) view.findViewById(R.id.list_sales);
        text_for_empty_list = (TextView) view.findViewById(R.id.text_for_empty_list);
        layoutfilter = (RelativeLayout) view.findViewById(R.id.layoutfilter);
        selectDate = view.findViewById(R.id.btnDate);
        tv_date = view.findViewById(R.id.tvSelectedDate);


        //get the history of transactions for the current user
        getSales();



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

                                for( Transaction transaction : listSales){
                                    if(transaction.getDate().equals(tv_date.getText())){
                                        listFiltredSales.add(new Transaction(transaction.getLibelle(),transaction.getDate(),transaction.getPrice(),transaction.getOwnerofService()));

                                    }
                                }

                                if (!listFiltredSales.isEmpty()){
                                    mAdapter = new SalesAdapter(getContext(),listFiltredSales);
                                    listv_sales.setAdapter(mAdapter);
                                }else {
                                    Utils.displayMessage(getContext(),"Pas de ventes pour cette date.");
                                }


                            }
                        }, year, month, dayOfMonth);

                datePickerDialog.show();

            }
        });


        return view;
    }



    public void getSales(){

        firebaseFirestore.collection(getString(R.string.collection_mysales)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    listSales = new ArrayList<>();


                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.get("id_owner_service").equals(currentUID)){
                            Log.d("id_owner_service",document.get("id_owner_service") + " / id_transaction:" + document.get("id_transaction"));

                            listSales.add(new Transaction(document.get("libelle"),document.get("date_transaction") , document.get("montant"),document.get("buyer")));
                        }

                    }

                    if(listSales.isEmpty()){
                        Log.d("list_empty", "there is no transactions available fot this user");
                        layoutfilter.setAlpha(0);
                        mDialog.dismiss();

                    }else{
                        //set the data in listview
                        text_for_empty_list.setAlpha(0);
                        mAdapter = new SalesAdapter(getContext(),listSales);
                        listv_sales.setAdapter(mAdapter);
                        mDialog.dismiss();
                    }



                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }


}

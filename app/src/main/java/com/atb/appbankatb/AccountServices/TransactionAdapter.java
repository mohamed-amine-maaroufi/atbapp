package com.atb.appbankatb.AccountServices;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import com.atb.appbankatb.Beans.Transaction;
import com.atb.appbankatb.R;


public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private Context mContext;
    private List<Transaction> TransactionList = new ArrayList<>();

    public TransactionAdapter(@NonNull Context context, ArrayList<Transaction> list) {
        super(context, 0 , list);
        mContext = context;
        TransactionList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.transaction_row,parent,false);

        Transaction currentTransaction = TransactionList.get(position);


        TextView price = (TextView) listItem.findViewById(R.id.textprice);
        price.setText(currentTransaction.getPrice().toString());

        TextView libelle = (TextView) listItem.findViewById(R.id.textlibelle);
        libelle.setText(currentTransaction.getLibelle().toString());

        TextView date = (TextView) listItem.findViewById(R.id.textdate);
        date.setText(currentTransaction.getDate().toString());

        TextView owner = (TextView) listItem.findViewById(R.id.textowner);
        owner.setText(currentTransaction.getOwnerofService().toString());

        return listItem;
    }
}

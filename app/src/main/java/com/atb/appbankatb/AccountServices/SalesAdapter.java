package com.atb.appbankatb.AccountServices;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.atb.appbankatb.Beans.Transaction;
import com.atb.appbankatb.R;

import java.util.ArrayList;
import java.util.List;


public class SalesAdapter extends ArrayAdapter<Transaction> {

    private Context mContext;
    private List<Transaction> SalesList = new ArrayList<>();

    public SalesAdapter(@NonNull Context context, ArrayList<Transaction> list) {
        super(context, 0 , list);
        mContext = context;
        SalesList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.transaction_row,parent,false);

        Transaction currentTransaction = SalesList.get(position);

        TextView num_compte = (TextView) listItem.findViewById(R.id.text_num_compte);
        num_compte.setText("Achéteur de service");

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

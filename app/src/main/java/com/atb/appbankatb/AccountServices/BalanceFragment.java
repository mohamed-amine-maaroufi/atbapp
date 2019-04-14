package com.atb.appbankatb.AccountServices;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.atb.appbankatb.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.support.constraint.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class BalanceFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    public TextView textSolde;

    public BalanceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_balance, container, false);

        textSolde = (TextView)  view.findViewById(R.id.textSolde);


        firebaseFirestore = FirebaseFirestore.getInstance();
        String currentUId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG,currentUId);


        DocumentReference docRef = firebaseFirestore.collection(getString(R.string.collection_comptes)).document(currentUId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        Double solde = document.getDouble("solde");
                        textSolde.setText(String.valueOf(solde) + "  DT");
                        Log.d(TAG, String.valueOf(solde));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

}

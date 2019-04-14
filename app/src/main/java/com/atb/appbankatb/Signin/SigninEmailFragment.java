package com.atb.appbankatb.Signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.atb.appbankatb.Home.HomeActivity;
import com.atb.appbankatb.R;
import com.atb.appbankatb.Signup.SignupActivity;
import com.atb.appbankatb.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SigninEmailFragment extends Fragment implements View.OnClickListener {


    private TextView registerButton;

    private EditText input_email,input_password;
    private Button btnLogin;
    private FirebaseAuth auth;
    public ProgressDialog mDialog;
    Animation performAnimation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        auth = FirebaseAuth.getInstance();
        //check the current user
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(getContext(), HomeActivity.class));
            getActivity().finish();
        }

        View view = inflater.inflate(R.layout.fragment_signin_email,container, false);
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);


        performAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.shake_view);
        performAnimation.setRepeatCount(4);

        mDialog = new ProgressDialog(getContext());
        mDialog.setMessage("veuillez patienter ...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);


        input_email = (EditText)view.findViewById(R.id.edittext_email);
        input_password = (EditText)view.findViewById(R.id.edittext_password);
        btnLogin = (Button)view.findViewById(R.id.btnlogin);
        registerButton = (TextView) view.findViewById(R.id.btn_go_to_signup);



        btnLogin.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        auth = FirebaseAuth.getInstance();
        btnLogin.setOnClickListener(this);


        //to close keyboard when tap out of edittext
        view.findViewById(R.id.layoutparent).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (((InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE)).isAcceptingText()) {

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                }
                return true;

            }
        });


        auth = FirebaseAuth.getInstance();
        return view;


    }


    @Override
    public void onClick(View view)
    {
        if(view.getId()==R.id.btnlogin) {
            signInWithEmailAndPassword();
        }

        if(view.getId()==R.id.btn_go_to_signup) {
            startActivity(new Intent(getContext(),SignupActivity.class));
            getActivity().finish();
        }
    }



    private void signInWithEmailAndPassword() {
        String email = input_email.getText().toString();
        String password = input_password.getText().toString();

        mDialog.show();

        if(TextUtils.isEmpty(email)){
            Utils.displayMessage(getContext(),"S'il vous plait, remplissez le login");
            input_email.startAnimation(performAnimation);
            mDialog.dismiss();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Utils.displayMessage(getContext(),"S'il vous plait, remplissez le mot de passe");
            input_password.startAnimation(performAnimation);
            mDialog.dismiss();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        String message;
                        if (task.isSuccessful()) {
                            message = "Authentification avec succées.";

                            //sharedPreference to store login and password
                            SharedPreferences pref = getContext().getSharedPreferences("Prefs", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("email_user", input_email.getText().toString());   //store email
                            editor.putString("password_user", input_password.getText().toString()); //store password
                            editor.commit(); // commit changes

                            Utils.displayMessage(getContext(),message);
                            startActivity(new Intent(getContext(),HomeActivity.class));
                            getActivity().finish();

                        } else {
                            message = "Vérifiez votre mot de passe ou votre login.";
                            Utils.displayMessage(getContext(),message);
                            mDialog.dismiss();

                        }

                        Log.d("message", message);
                        //mPasswordTextView.setText(message);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //mPasswordTextView.setText(e.getMessage());
                e.printStackTrace();
            }
        });
    }



}
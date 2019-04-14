package com.atb.appbankatb.Signin;


import android.Manifest;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.atb.appbankatb.Home.HomeActivity;
import com.atb.appbankatb.R;
import com.atb.appbankatb.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;
import static android.support.constraint.Constraints.TAG;


public class SigninFingerprintFragment extends Fragment implements FingerprintHelper.FingerprintHelperListener{

    //your other variables here
    // -----------------------------------------------
    private FingerprintHelper fingerprintHelper;
    private FingerprintManager fingerprintManager;
    TextView texterror;
    private FirebaseAuth auth;
    public ProgressDialog mDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin_fingerprint, container, false);
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        auth = FirebaseAuth.getInstance();
        //check the current user
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(getContext(), HomeActivity.class));
            getActivity().finish();
        }

        mDialog = new ProgressDialog(getContext());
        mDialog.setMessage("veuillez patienter ...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        texterror = (TextView) view.findViewById(R.id.errorText);
        return view;
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        //Check for the fingerprint permission and listen for fingerprint
        //add additional checks along with this condition based on your logic
        if (checkFingerPrintSettings(this)) {
            //Fingerprint is available, update the UI to ask user for Fingerprint auth

            //start listening for Fingerprint
            fingerprintHelper = new FingerprintHelper(this);
            fingerprintManager = (FingerprintManager) getContext().getSystemService(FINGERPRINT_SERVICE);
            fingerprintHelper.startAuth(fingerprintManager, null);
        } else {
            Log.d(TAG, " Empreinte digitale désactivée en raison de l'absence d'identifiant de connexion ou d'empreinte digitale ");

            Utils.displayMessage(getContext(), "Empreinte digitale désactivée en raison de l'absence d'identifiant de connexion ou d'empreinte digitale.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkFingerPrintSettings(SigninFingerprintFragment signinFingerprintFragment) {

        final String TAG = "FP-Check";

        //Check for android version, FingerPrint is not available below Marshmallow
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "Cette version Android ne supporte pas l'authentification par empreinte digitale.");
            Utils.displayMessage(getContext(), "Cette version Android ne supporte pas l'authentification par empreinte digitale.");

            return false;
        }

        //Check whether the security option for phone is set.
        //ie. LockScreen is enabled or not.
        KeyguardManager keyguardManager = (KeyguardManager) getContext().getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getContext().getSystemService(FINGERPRINT_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (!keyguardManager.isKeyguardSecure()) {
                Log.d(TAG, "L'utilisateur n'a pas activé l'écran de verrouillage");
                Toast.makeText(getContext(),"L'utilisateur n'a pas activé l'écran de verrouillage.", Toast.LENGTH_SHORT).show();

                Utils.displayMessage(getContext(), "L'utilisateur n'a pas activé l'écran de verrouillage.");

                return false;
            }
        }

        //check if user have registered any fingerprints
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            Log.d(TAG, "User hasn't registered any fingerprints");
            Toast.makeText(getContext()," L'utilisateur n'a pas enregistré d'empreintes digitales.", Toast.LENGTH_SHORT).show();

            Utils.displayMessage(getContext(), "Vous n'avez pas configuré votre empreinte digitale pour ce télèphone.");


            return false;
        }

        //check for app permissions
        //Make sure you have mentioned the permission on AndroidManifest.xml
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "L'utilisateur n'a pas accordé la permission d'utiliser l'empreinte digitale");

            Utils.displayMessage(getContext(), "Vous n'avez pas accordé la permission d'utiliser l'empreinte digitale.");

            return false;
        }

        Log.d(TAG, "Fingerprint authentication is set.");
        //Toast.makeText(getContext(),"L'authentification par empreinte digitale est définie.", Toast.LENGTH_SHORT).show();

        return true;

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onPause() {
        super.onPause();
        if (fingerprintHelper != null)
            fingerprintHelper.cancel();
    }

    @Override
    public void authenticationFailed(String error) {
        //Authentication failed. ie user tried an invalid fingerprint
        //Toast.makeText(getContext(), "Invalid Fingeprint", Toast.LENGTH_LONG).show();
        texterror.setText("Empriente Invalide.");

    }
    @Override
    public void authenticationSuccess(FingerprintManager.AuthenticationResult result) {

        mDialog.show();
        SharedPreferences pref = getContext().getSharedPreferences("Prefs", 0);
        String email = pref.getString("email_user", null); // getting email
        String password = pref.getString("password_user", null); // getting password

        if(email != null || password != null)
        {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            String message;
                            if (task.isSuccessful()) {
                                message = "Authentification avec succées.";

                                mDialog.dismiss();
                                Utils.displayMessage(getContext(),message);
                                startActivity(new Intent(getContext(),HomeActivity.class));
                                getActivity().finish();

                            } else {
                                message = "Essayez d'authentifier par email et mot de passe.";
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

        }else{
            mDialog.dismiss();
            Utils.displayMessage(getContext(), "Il faudrait authentifier avec email et mot de passe pour la première fois.");

        }



    }




}

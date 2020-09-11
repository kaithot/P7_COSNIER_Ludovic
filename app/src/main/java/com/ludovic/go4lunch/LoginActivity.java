package com.ludovic.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.ludovic.go4lunch.api.UserHelper;
import com.ludovic.go4lunch.utils.BaseActivity;

import java.util.List;
import java.util.Objects;

public class LoginActivity extends BaseActivity implements View.OnClickListener {



    //FOR DATA
    //Identifier for Sign-In Activity
    private static final int RC_SIGN_IN = 123;

    List<AuthUI.IdpConfig> providers;
    private FirebaseAuth mAuth;

    GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);

    }

    @Override
    public int getFragmentLayout() {
        return R.layout.login_activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = getCurrentUser();

        if (isCurrentUserLogged()) {
            startLunchActivity();
        } else {

        }
    }

    private void updateUI(FirebaseUser account) {
        Toast.makeText(this, "" + account.getEmail(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in_button:
                googleSignIn();
                break;
            default:
                System.out.println("case not implemented");
        }
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d("info :", "firebaseAuthWithGoogle:" + account.getId());
            if(firebaseAuthWithGoogle(account.getIdToken()) == true) {
                createUserInFirestore();
                startLunchActivity();
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("sign in activity", "signInResult:failed code=" + e.getStatusCode(), e);
            updateUI(null);
        }
    }

    private boolean firebaseAuthWithGoogle(String idToken) {

        final boolean[] retvalue = new boolean[1];
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Sign in success, update UI with the signed-in user's information
                            Log.d("sign in activity","signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            startLunchActivity();
                            retvalue[0] = true;
                        } else {
                            // If in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,"Authentication Failed.",Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            retvalue[0] = false;
                        }
                    }
                });
        return retvalue[0];
    }

    //---------------------
    // REST REQUEST
    //---------------------
    // Http request that create user in firestore

    private void createUserInFirestore(){
        if (isCurrentUserLogged()) {

            Log.d("create user : ", "createUserInFirestore");
            String urlPicture = (Objects.requireNonNull(this.getCurrentUser()).getPhotoUrl() != null) ? Objects.requireNonNull(this.getCurrentUser().getPhotoUrl()).toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();
            String userEmail = this.getCurrentUser().getEmail();
            UserHelper.createUser(uid, username, userEmail, urlPicture).addOnFailureListener(this.onFailureListener());
        }
    }

    // launch lunch activity
    private void startLunchActivity() {
        Intent intent = new Intent(this, LunchActivity.class);
        intent.putExtra("USER_ID", Objects.requireNonNull(this.getCurrentUser()).getUid());
        startActivity(intent);
    }
}

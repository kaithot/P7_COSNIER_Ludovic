package com.ludovic.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.ludovic.go4lunch.api.UserHelper;
import com.ludovic.go4lunch.utils.BaseActivity;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends BaseActivity{



    //FOR DATA
    //Identifier for Sign-In Activity
    private static final int RC_SIGN_IN = 123;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            startApp();

        }

        @Override
        public int getFragmentLayout() {
            return R.layout.login_activity;
        }

        //Launch activity and send a welcome back message
        private void startApp() {
            if (getCurrentUser()!=null){
                this.startLunchActivity();
                Toast toast = Toast.makeText(this, "Welcome back "+getCurrentUser().getDisplayName(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();

            }else {
                this.startSignInActivity();
            }
        }


        //------------------
        //UTILS
        //------------------

        @Override
        protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            this.handleResponseAfterSignIn(requestCode, resultCode, data);
        }


        // Sign-in methods
        private void startSignInActivity() {
            startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setTheme(R.style.LoginTheme)
                            .setAvailableProviders(
                                    Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(), //By google
                                            new AuthUI.IdpConfig.EmailBuilder().build(), // By mail
                                            new AuthUI.IdpConfig.FacebookBuilder().build()) //By Facebook
                            )
                            .setLogo(R.drawable.ic_logo_background)
                            .setTheme(R.style.LoginTheme)
                            .setIsSmartLockEnabled(false, true)
                            .build(),
                    RC_SIGN_IN);
        }


        //show snack bar with a message after sign-in
        private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (requestCode == RC_SIGN_IN) {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, getString(R.string.connection_succeed), Toast.LENGTH_LONG).show();
                    startLunchActivity();
                    this.createUserInFirestore();
                } else if (response == null) {
                    Toast.makeText(this, getString(R.string.error_authentication_canceled), Toast.LENGTH_LONG).show();
                } else if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_LONG).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
                }
            }
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

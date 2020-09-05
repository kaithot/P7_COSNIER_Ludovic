package com.ludovic.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ludovic.go4lunch.utils.BaseActivity;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ludovic Cosnier 01/09/2020
 */
public class LoginActivity extends BaseActivity {

    //FOR DATA
    //Identifier for Sign-In Activity
    private static final int RC_SIGN_IN = 123;
    //private Button loginBtn;

    List<AuthUI.IdpConfig>providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        //loginBtn = findViewById(R.id.main_activity_button_login);

        //Init  provider
        providers = Arrays.asList(

                new AuthUI.IdpConfig.FacebookBuilder().build(), //Facebook builder
                new AuthUI.IdpConfig.GoogleBuilder().build()); //Google builder

        showSignInOption();

    }

    private void showSignInOption() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .build(),RC_SIGN_IN
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN)
            {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK)
            {
                //Get User
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //Get email on Toast
                Toast.makeText(this,""+user.getEmail(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.lunch_activity;
    }
}

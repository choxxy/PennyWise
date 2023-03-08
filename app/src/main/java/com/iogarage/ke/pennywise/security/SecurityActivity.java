package com.iogarage.ke.pennywise.security;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import com.iogarage.ke.pennywise.PennyMain;
import com.iogarage.ke.pennywise.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * A login screen that offers login via email/password.
 */
public class SecurityActivity extends AppCompatActivity {

    public static final String EXTRA_ACTION = "ACTION";

    public static final int REGISTRATION = 1;
    public static final int SIGNIN = 2;
    public static final int FORGOT_PASSWORD = 3;
    private int action;
    private SharedPreferences sharedPref;


    public static Intent newIntent(Context context, int action) {
        Intent ret = new Intent(context, SecurityActivity.class);
        ret.putExtra(EXTRA_ACTION,action);
        return ret;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        action = getIntent().getIntExtra(EXTRA_ACTION, SIGNIN);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);


        if(sharedPref != null){

            boolean showLogin = sharedPref.getBoolean(getString(R.string.prefTogglePin), false);

            if(!showLogin){
                startActivity(new Intent(this, PennyMain.class));
                finish();
            }
        }


        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (action) {
            case SIGNIN:
                fragmentManager.beginTransaction().add(R.id.container, LoginFragment.newInstance()).commit();
                break;
            case REGISTRATION:
                fragmentManager.beginTransaction().add(R.id.container, RegistrationFragment.newInstance()).commit();
                break;
            case FORGOT_PASSWORD:
                fragmentManager.beginTransaction().add(R.id.container, ForgotPassword.newInstance()).commit();
                break;
        }

    }


    @Subscribe
    public void onLogin(LoginEvent event){
        startActivity(new Intent(this,PennyMain.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected  void onPause(){
        EventBus.getDefault().unregister(this);
        super.onPause();

    }
}


package com.iogarage.ke.pennywise.security;


import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.iogarage.ke.pennywise.DaggerScope;
import com.iogarage.ke.pennywise.PennyApp;
import com.iogarage.ke.pennywise.R;
import com.iogarage.ke.pennywise.entities.DaoSession;
import com.iogarage.ke.pennywise.entities.User;
import com.iogarage.ke.pennywise.entities.UserDao;

import javax.inject.Inject;

import autodagger.AutoInjector;
import autodagger.AutoSubcomponent;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AutoSubcomponent
@AutoInjector
@DaggerScope(RegistrationFragment.class)
public class RegistrationFragment extends Fragment {

    private TextView email;
    private TextView password;
    private TextView password_confirmation;
    private Button save;

    @Inject
    DaoSession session;

    UserDao userDao;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    public RegistrationFragment() {
        // Required empty public constructor
    }


    public static RegistrationFragment newInstance() {
        RegistrationFragment fragment = new RegistrationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((PennyApp) getActivity().getApplication())
                .getComponent()
                .plusRegistrationFragmentComponent()
                .inject(this);

        userDao = session.getUserDao();

        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registration, container, false);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        password_confirmation = view.findViewById(R.id.confirm_password);
        save = view.findViewById(R.id.action_save);

        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValid()){

                    User user = new User();
                    user.setEmail(email.getText().toString());
                    user.setPassword(password.getText().toString());
                    user.setUsername(email.getText().toString());
                    userDao.insert(user);

                }
            }
        });

        return view;
    }

    private boolean isValid() {

        String strEmail =  email.getText().toString();

        if(TextUtils.isEmpty(strEmail)
                && !(android.util.Patterns.EMAIL_ADDRESS.matcher(strEmail).matches())){
            tilEmail.setErrorEnabled(true);
            tilEmail.setError("A valid email address is required.");
            return false;
        }else
            tilEmail.setErrorEnabled(false);

        if(TextUtils.isEmpty(password.getText().toString())){
            tilPassword.setErrorEnabled(true);
            tilPassword.setError("Password cannot be empty");
            return  false;
        }else
            tilPassword.setErrorEnabled(false);

        if(TextUtils.isEmpty(password_confirmation.getText().toString())){

            tilConfirmPassword.setErrorEnabled(true);
            tilConfirmPassword.setError("Confirmation password cannot be empty");
            return  false;
        }else
           tilConfirmPassword.setErrorEnabled(false);

        if(!(password.getText().toString().equals(password_confirmation.getText().toString()))){
            tilConfirmPassword.setErrorEnabled(true);
            tilConfirmPassword.setError("Passwords do not match");
            return  false;
        }else
            tilConfirmPassword.setErrorEnabled(false);
        return true;
    }

}

package com.iogarage.ke.pennywise;


import com.tapadoo.alerter.Alerter;

import io.multimoon.colorful.CAppCompatActivity;

public class BaseActivity extends CAppCompatActivity {


    void showErrorAlert(String title, String message) {
        Alerter.create(this)
                .setTitle(title)
                .setText(message)
                .setBackgroundColorRes(R.color.coral) // or setBackgroundColorInt(Color.CYAN)
                .show();
    }

    void showInfoAlert(String title, String message) {
        Alerter.create(this)
                .setTitle(title)
                .setText(message)
                .setBackgroundColorRes(R.color.md_blue_500) // or setBackgroundColorInt(Color.CYAN)
                .show();
    }
}

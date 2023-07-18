package com.iogarage.ke.pennywise.views

import androidx.appcompat.app.AppCompatActivity
import com.iogarage.ke.pennywise.R
import com.tapadoo.alerter.Alerter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    fun showErrorAlert(title: String?, message: String?) {
        Alerter.create(this)
            .setTitle(title!!)
            .setText(message!!)
            .setBackgroundColorRes(R.color.coral) // or setBackgroundColorInt(Color.CYAN)
            .show()
    }

    fun showInfoAlert(title: String?, message: String?) {
        Alerter.create(this)
            .setTitle(title!!)
            .setText(message!!)
            .setBackgroundColorRes(R.color.md_blue_500) // or setBackgroundColorInt(Color.CYAN)
            .show()
    }
}
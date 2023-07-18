package com.iogarage.ke.pennywise.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("app:errorText")
fun TextInputLayout.setErrorText(errorMessage: String) {
    if (errorMessage.isEmpty())
        this.error = null
    else
        this.error = errorMessage;
}

@BindingAdapter("app:errorText")
fun TextView.setErrorText(errorMessage: String) {
    if (errorMessage.isEmpty())
        this.text = null
    else
        this.text = errorMessage;
}
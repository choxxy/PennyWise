package com.iogarage.ke.pennywise.views.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.iogarage.ke.pennywise.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}
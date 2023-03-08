package com.iogarage.ke.pennywise;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;

import com.iogarage.ke.pennywise.dialogs.colordialog.ColorPickerDialog;
import com.iogarage.ke.pennywise.security.SecurityActivity;
import com.iogarage.ke.pennywise.util.Constants;
import com.iogarage.ke.pennywise.util.Prefs;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.multimoon.colorful.BaseTheme;
import io.multimoon.colorful.CThemeInterface;
import io.multimoon.colorful.ThemeColor;
import kotlin.Unit;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static io.multimoon.colorful.ColorfulKt.Colorful;


public class SettingsActivity extends AppCompatPreferenceActivity implements CThemeInterface {
    private Toolbar toolbar;
    private TextView title;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        handleOnCreate(this, savedInstanceState, BaseTheme.THEME_APPCOMPAT, true);


        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        title = findViewById(R.id.title);
        title.setText("");

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.content, new SettingsFragment())
                    .commit();
        }
    }

    @NotNull
    @Override
    public String getThemeString() {
        return "";
    }

    @Override
    public void setThemeString(String s) {

    }

    @Override
    public void handleOnResume(Activity activity) {
        Colorful().apply(this, true, BaseTheme.THEME_APPCOMPAT);
    }

    @Override
    public void handleOnCreate(Activity activity, Bundle bundle, BaseTheme baseTheme, boolean b) {
        Colorful().apply(this, true, baseTheme);
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        ListPreference currencyPreference;
        ListPreference backUpProviderPreference;
        Preference pinPreference;
        Preference primaryColorPreference;
        Preference accentColorPreference;
        private EditTextPreference editTextPreference;


        private void callBack() {
            return;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.prefs, false);


            setupCurrencyPreference();
            setupBackServicePreference();

            pinPreference = findPreference(getActivity().getString(R.string.prefShowPinEntry));
            pinPreference.setOnPreferenceChangeListener(this);
            pinPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // init  DriveSyncController
                    Intent intent = SecurityActivity.newIntent(preference.getContext(), SecurityActivity.REGISTRATION);
                    startActivity(intent);

                    return true;
                }
            });

            primaryColorPreference = findPreference(getActivity().getString(R.string.preference_primary_color));
            primaryColorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ColorPickerDialog dialog = new ColorPickerDialog(getActivity());
                    dialog.setOnColorSelectedListener(new ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(ThemeColor color) {
                            //TODO: Do something with the color
                            Colorful().edit()
                                    .setPrimaryColor(color)
                                    .setAccentColor(Colorful().getAccentColor())
                                    .setDarkTheme(Colorful().getDarkTheme())
                                    .setTranslucent(Colorful().getTranslucent())
                                    .apply(getActivity(), () -> {
                                        return Unit.INSTANCE;
                                    });
                        }
                    });
                    dialog.show();

                    return true;
                }
            });


            accentColorPreference = findPreference(getActivity().getString(R.string.preference_accent_color));
            accentColorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ColorPickerDialog dialog = new ColorPickerDialog(getActivity());
                    dialog.setOnColorSelectedListener(new ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(ThemeColor color) {
                            //TODO: Do something with the color
                            Colorful().edit()
                                    .setPrimaryColor(Colorful().getPrimaryColor())
                                    .setAccentColor(color)
                                    .setDarkTheme(Colorful().getDarkTheme())
                                    .setTranslucent(Colorful().getTranslucent())
                                    .apply(getActivity(), () -> {
                                        return Unit.INSTANCE;
                                    });
                        }
                    });
                    dialog.show();

                    return true;
                }
            });

            editTextPreference = (EditTextPreference) findPreference(getActivity().getString(R.string.prefTemplate));
            editTextPreference.setOnPreferenceChangeListener(this);

            SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
            editTextPreference.setSummary(myPreference.getString(getActivity().getString(R.string.prefTemplate), ""));
        }

        private void setupCurrencyPreference() {

            currencyPreference = (ListPreference) findPreference(getActivity().getString(R.string.prefCurrency));
            String value = currencyPreference.getValue();
            int index = currencyPreference.findIndexOfValue(value);
            CharSequence[] entries = currencyPreference.getEntries();


            currencyPreference.setDefaultValue(index);
            currencyPreference.setSummary(index >= 0 ? entries[index] : null);
            currencyPreference.setOnPreferenceChangeListener(this);

        }

        private void setupBackServicePreference() {

            backUpProviderPreference = (ListPreference) findPreference(getActivity().getString(R.string.prefBackUpService));
            String value = backUpProviderPreference.getValue();
            int index = backUpProviderPreference.findIndexOfValue(value);
            CharSequence[] entries = backUpProviderPreference.getEntries();

            if (index == 0) {
                backUpProviderPreference.setIcon(R.mipmap.ic_google_drive);
            } else if (index == 1) {
                backUpProviderPreference.setIcon(R.mipmap.ic_dropbox);
            } else {
                backUpProviderPreference.setIcon(R.mipmap.ic_backup);
                backUpProviderPreference.setValueIndex(0);
            }

            long date = Prefs.getLong(Constants.LAST_BACK_UP_DATE, 0);

            if (date != 0) {
                //Last backup done on: dd/mm/yyyy
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String strDate = simpleDateFormat.format(new Date(date));
                backUpProviderPreference.setSummary(String.format("Last backup done on: %s", strDate));
            }

            backUpProviderPreference.setDefaultValue(index);
            String title = backUpProviderPreference.getTitle().toString();
            backUpProviderPreference.setTitle(index >= 0 ? entries[index] : title);
            backUpProviderPreference.setOnPreferenceChangeListener(this);

        }


        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference.getKey().equals(getActivity().getString(R.string.prefCurrency))) {
                String textValue = newValue.toString();
                int index = currencyPreference.findIndexOfValue(textValue);
                CharSequence[] entries = currencyPreference.getEntries();


                preference.setDefaultValue(index);
                preference.setSummary(index >= 0 ? entries[index] : null);
                preference.setPersistent(true);
            }

            if (preference.getKey().equals(getActivity().getString(R.string.prefBackUpService))) {
                String textValue = newValue.toString();
                int index = backUpProviderPreference.findIndexOfValue(textValue);
                CharSequence[] entries = backUpProviderPreference.getEntries();

                if (index == 0)
                    backUpProviderPreference.setIcon(R.mipmap.ic_google_drive);
                else if (index == 1)
                    backUpProviderPreference.setIcon(R.mipmap.ic_dropbox);
                else
                    backUpProviderPreference.setIcon(R.mipmap.ic_backup);

                preference.setDefaultValue(index);
                preference.setTitle(index >= 0 ? entries[index] : null);
                preference.setPersistent(true);
            }

            if (preference.getKey().equals(getActivity().getString(R.string.prefTemplate))) {
                EditTextPreference editTextPref = (EditTextPreference) preference;
                String txt = (String) newValue;
                editTextPref.setSummary(txt);
            }
            return true;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        handleOnResume(this);

        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_action_back);
            toolbar.setNavigationOnClickListener(v -> {
                setResult(RESULT_CANCELED, new Intent());
                finish();
            });
        }
    }
}

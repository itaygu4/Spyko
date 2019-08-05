package com.example.itayg.spykomusic;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    Preference namePref;
    Preference emailPref;
    Preference privatePref;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);



        namePref = findPreference("account_name");
        emailPref = findPreference("account_email");
        privatePref = findPreference("account_private");

    }

    public Preference getNamePref() {
        return namePref;
    }

    public Preference getEmailPref() {
        return emailPref;
    }
}
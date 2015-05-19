package ua.parus.pmo.parus8claims;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {
    public static final String PREF_PASSWORD = "password";
    public static final String PREF_USERNAME = "username";
    public static final String PREF_RESET_CACHE = "cache";

    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    String stringValue = value.toString();
                    if (preference instanceof ListPreference) {
                        // For list preferences, look up the correct display value in
                        // the preference's 'entries' list.
                        ListPreference listPreference = (ListPreference) preference;
                        int index = listPreference.findIndexOfValue(stringValue);
                        // Set the summary to reflect the new value.
                        preference.setSummary(
                                index >= 0
                                        ? listPreference.getEntries()[index]
                                        : null);

                    } else {
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        if (preference.getKey().equals(PREF_PASSWORD)) {
                            EditText edit = ((EditTextPreference) preference).getEditText();
                            String pref = edit.getTransformationMethod().getTransformation(stringValue, edit).toString();
                            preference.setSummary(pref);
                        } else {

                            preference.setSummary(stringValue);
                        }
                    }
                    return true;
                }
            };

    public static boolean isCredentialsSet(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return !TextUtils.isEmpty(sharedPrefs.getString(SettingsActivity.PREF_USERNAME, null))
                && !TextUtils.isEmpty(sharedPrefs.getString(SettingsActivity.PREF_PASSWORD, null));
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void onBackPressed() {
        if (isCredentialsSet(this)) {
            Intent intentResult = new Intent();
            setResult(Intents.RESULT_CANCEL, intentResult);
            finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.activity_settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(PREF_USERNAME));
        bindPreferenceSummaryToValue(findPreference(PREF_PASSWORD));
        findPreference(PREF_RESET_CACHE).setOnPreferenceClickListener(this);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(PREF_RESET_CACHE)) {
            if (isCredentialsSet(this)) {
                Intent intentResult = new Intent();
                setResult(Intents.RESULT_NEED_REFRESH_DICTIONARIES_CACHE, intentResult);
                finish();
            }
        }
        return false;
    }
}

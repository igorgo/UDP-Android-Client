package ua.parus.pmo.parus8claims;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Locale;

import roboguice.util.Ln;
import ua.parus.pmo.parus8claims.gui.MaterialDialogBuilder;
import ua.parus.pmo.parus8claims.utils.Constants;
import ua.parus.pmo.parus8claims.utils.FontCache;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    private SharedPreferences prefs;
    private SettingsActivity instance;

    private final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
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
                    }
                    if (preference instanceof EditTextPreference) {
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        if (preference.getKey().equals(Constants.PREF_PASSWORD)) {
                            EditText edit = ((EditTextPreference) preference).getEditText();
                            String pref = edit.getTransformationMethod().getTransformation(stringValue, edit).toString();
                            preference.setSummary(pref);
                        } else {
                            preference.setSummary(stringValue);
                        }
                    }
                    if (preference instanceof CheckBoxPreference) {
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        if (preference.getKey().equals(Constants.PREF_FONT)) {
                            FontCache.getInstance(instance).clear();
                        }
                    }
                    return true;
                }
            };

    public boolean isCredentialsSet(Context context) {
        return !TextUtils.isEmpty(prefs.getString(Constants.PREF_USERNAME, null))
                && !TextUtils.isEmpty(prefs.getString(Constants.PREF_PASSWORD, null));
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof EditTextPreference)
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                prefs.getString(preference.getKey(), ""));
        if (preference instanceof CheckBoxPreference)
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    prefs.getBoolean(preference.getKey(), true));

    }

    @Override
    public void onBackPressed() {
        if (isCredentialsSet(this)) {
            Intent intentResult = new Intent();
            setResult(Constants.RESULT_CANCEL, intentResult);
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
        bindPreferenceSummaryToValue(findPreference(Constants.PREF_USERNAME));
        bindPreferenceSummaryToValue(findPreference(Constants.PREF_PASSWORD));
        bindPreferenceSummaryToValue(findPreference(Constants.PREF_FONT));
        findPreference(Constants.PREF_RESET_CACHE).setOnPreferenceClickListener(this);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(Constants.PREF_RESET_CACHE)) {
            if (isCredentialsSet(this)) {
                Intent intentResult = new Intent();
                setResult(Constants.RESULT_NEED_REFRESH_DICTIONARIES_CACHE, intentResult);
                finish();
            }
        }
        return false;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Languages
        ListPreference lang = (ListPreference) findPreference("settings_language");
        if (lang != null) {
            String languageName = getResources().getConfiguration().locale.getDisplayName();
            lang.setSummary(languageName.substring(0, 1).toUpperCase(getResources().getConfiguration().locale)
                    + languageName.substring(1, languageName.length()));
            lang.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    Locale locale = new Locale(value.toString());
                    Configuration config = getResources().getConfiguration();

                    if (!config.locale.getCountry().equals(locale)) {
                        ClaimApplication.updateLanguage(instance, value.toString());
                        ClaimApplication.restartApp(instance);
                    }
                    return false;
                }
            });
        }

        // Changelog
        Preference changelog = findPreference("settings_changelog");
        if (changelog != null) {
            changelog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    new MaterialDialogBuilder(instance)
                            .customView(R.layout.activity_changelog, false)
                            .positiveText(android.R.string.ok)
                            .build().show();
                    return false;
                }
            });
            // Retrieval of installed app version to write it as summary
            PackageInfo pInfo;
            String versionString = "";
            try {
                pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
                versionString = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Ln.e("Error retrieving version", e);
            }
            changelog.setSummary(versionString);
        }
    }
}

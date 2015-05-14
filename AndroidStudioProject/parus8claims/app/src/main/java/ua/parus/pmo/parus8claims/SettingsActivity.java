package ua.parus.pmo.parus8claims;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.net.ConnectException;

import ua.parus.pmo.parus8claims.gui.ErrorPopup;
import ua.parus.pmo.parus8claims.objects.dicts.Applists;
import ua.parus.pmo.parus8claims.objects.dicts.Releases;
import ua.parus.pmo.parus8claims.objects.dicts.Units;

@SuppressWarnings({"deprecation", "BooleanMethodIsAlwaysInverted"})
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.activity_settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(PREF_USERNAME));
        bindPreferenceSummaryToValue(findPreference(PREF_PASSWORD));
        findPreference(PREF_RESET_CACHE).setOnPreferenceClickListener(this);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(PREF_RESET_CACHE)) {
            try {
                Releases.RefreshCache(this);
            } catch (ConnectException e) {
                ErrorPopup errorPopup = new ErrorPopup(this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                });
                errorPopup.showErrorDialog(getString(R.string.error_title), getString(R.string.server_unreachable));
            }
            ProgressDialog loadDialog = ProgressDialog.show(SettingsActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.loading_unitlist), true);

            Units.refreshCache(this);
            loadDialog.dismiss();
            loadDialog = ProgressDialog.show(SettingsActivity.this, getString(R.string.please_wait), getString(R.string.loading_applist), true);
            Applists.refreshCache(this);
            loadDialog.dismiss();
            ((ClaimApplication) this.getApplication()).setCacheRefreched();
        }
        return false;
    }
}

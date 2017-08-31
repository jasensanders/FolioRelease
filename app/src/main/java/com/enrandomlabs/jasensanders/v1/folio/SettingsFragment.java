package com.enrandomlabs.jasensanders.v1.folio;

import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.enrandomlabs.jasensanders.v1.folio.database.DataContract;
import com.google.firebase.analytics.FirebaseAnalytics;

import static com.enrandomlabs.jasensanders.v1.folio.Utility.logActionEvent;


/**
 * A simple {@link PreferenceFragment} subclass.
 *
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    public static final String PATH_SETTINGS = "settings";
    public static final Uri SETTINGS_URI = DataContract.BASE_CONTENT_URI.buildUpon().appendPath(PATH_SETTINGS).build();

    //Firebase
    private FirebaseAnalytics mFirebaseAnalytics;


    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {

        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        //Start Tracking events
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.main_view_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.sort_type_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.sort_order_key)));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Preference mRestoreDataLocallyButton = findPreference(getString(R.string.local_restore_backup_init_key));
        mRestoreDataLocallyButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                Utility.setPrefRestoreBackup(getActivity(), true);
                logActionEvent(LOG_TAG,"nav_restore_db", "action", mFirebaseAnalytics);
                //Launch snackbar to ask if user wants to restart now
                final String request = "Backup will be restored on restart";
                final Snackbar restartMessage = Snackbar.make(getView(), request, Snackbar.LENGTH_LONG );
                restartMessage.setAction("RESTART", new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Utility.reStart(getActivity(), 100, SplashActivity.class);
                    }
                });
                restartMessage.show();
                return true;
            }
        });
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();

        //We only need to set the summary on the ListPreferences.
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }

    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Set the preference summaries
        setPreferenceSummary(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        setPreferenceSummary(preference, newValue);
        return true;
    }
}

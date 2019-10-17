package de.danoeh.antennapod.fragment.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.SwitchPreference;
import android.text.Html;
import android.text.format.DateUtils;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.PreferenceActivity;
import de.danoeh.antennapod.core.preferences.InaudiblePreferences;
import de.danoeh.antennapod.core.service.InaudibleSyncService;
import de.danoeh.antennapod.dialog.InaudibleSetURLDialog;

public class InaudiblePreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_inaudible);
        setupScreen();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((PreferenceActivity) getActivity()).getSupportActionBar().setTitle(R.string.inaudible_main_label);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceScreen();
        InaudiblePreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        InaudiblePreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener listener =
            (sharedPreferences, key) -> {
                updatePreferenceScreen();
            };
    private void setupScreen() {
        final Activity activity = getActivity();

        findPreference(InaudiblePreferences.PREF_KEY_SYNC_NOW).
                setOnPreferenceClickListener(preference -> {
                    InaudibleSyncService.sendSyncIntent(getActivity().getApplicationContext());
                    Toast toast = Toast.makeText(getActivity(), R.string.pref_inaudible_sync_started,
                            Toast.LENGTH_SHORT);
                    toast.show();
                    return true;
                });
        findPreference(InaudiblePreferences.PREF_KEY_URL).setOnPreferenceClickListener(
                preference -> {
                    InaudibleSetURLDialog.createDialog(activity).setOnDismissListener(dialog -> updatePreferenceScreen());
                    return true;
                });

        findPreference(InaudiblePreferences.PREF_KEY_SYNC).setOnPreferenceClickListener(
                preference -> {
                    boolean checked = ((SwitchPreference) preference).isChecked();
                    InaudiblePreferences.setEnabled(checked);
                    updatePreferenceScreen();
                    return true;
                });
        findPreference(InaudiblePreferences.PREF_KEY_NOTIFICATIONS).setOnPreferenceClickListener(
                 preference -> {
                     boolean checked = ((SwitchPreference) preference).isChecked();
                    InaudiblePreferences.setNotifications(checked);
                    updatePreferenceScreen();
                    return true;
                });
    }

    private void updatePreferenceScreen() {
        findPreference(InaudiblePreferences.PREF_KEY_URL).setSummary(InaudiblePreferences.getURL());
        ((SwitchPreference) findPreference(InaudiblePreferences.PREF_KEY_SYNC)).setChecked(InaudiblePreferences.syncEnabled());
        ((SwitchPreference) findPreference(InaudiblePreferences.PREF_KEY_NOTIFICATIONS)).setChecked(InaudiblePreferences.notificationsEnabled());

    }
}

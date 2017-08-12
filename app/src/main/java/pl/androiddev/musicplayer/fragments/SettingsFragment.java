package pl.androiddev.musicplayer.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.ATECheckBoxPreference;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import pl.androiddev.musicplayer.R;
import pl.androiddev.musicplayer.activities.SettingsActivity;
import pl.androiddev.musicplayer.utils.PreferencesUtility;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String KEY_START_PAGE = "start_page_preference";

    ListPreference startPagePreference;
    PreferencesUtility mPreferences;
    private String mAteKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mPreferences = PreferencesUtility.getInstance(getActivity());
        startPagePreference = (ListPreference) findPreference(KEY_START_PAGE);
        setPreferenceClickListeners();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
    }

    private void setPreferenceClickListeners() {
        startPagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            switch ((String) newValue) {
                case "last_opened":
                    mPreferences.setLastOpenedAsStartPagePreference(true);
                    break;
                case "songs":
                    mPreferences.setLastOpenedAsStartPagePreference(false);
                    mPreferences.setStartPageIndex(0);
                    break;
                case "albums":
                    mPreferences.setLastOpenedAsStartPagePreference(false);
                    mPreferences.setStartPageIndex(1);
                    break;
                case "artists":
                    mPreferences.setLastOpenedAsStartPagePreference(false);
                    mPreferences.setStartPageIndex(2);
                    break;
            }
            return true;
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        invalidateSettings();
        ATE.apply(view, mAteKey);
    }

    public void invalidateSettings() {
        mAteKey = ((SettingsActivity) getActivity()).getATEKey();

        ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
        primaryColorPref.setColor(Config.primaryColor(getActivity(), mAteKey), Color.BLACK);
        primaryColorPref.setOnPreferenceClickListener(preference -> {
            new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_color)
                    .preselect(Config.primaryColor(getActivity(), mAteKey))
                    .show();
            return true;
        });

        ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
        accentColorPref.setColor(Config.accentColor(getActivity(), mAteKey), Color.BLACK);
        accentColorPref.setOnPreferenceClickListener(preference -> {
            new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.accent_color)
                    .preselect(Config.accentColor(getActivity(), mAteKey))
                    .show();
            return true;
        });

        final ATECheckBoxPreference statusBarPref = (ATECheckBoxPreference) findPreference("colored_status_bar");
        final ATECheckBoxPreference navBarPref = (ATECheckBoxPreference) findPreference("colored_nav_bar");
    }
}

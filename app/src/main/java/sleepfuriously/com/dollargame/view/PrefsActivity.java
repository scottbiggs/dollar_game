package sleepfuriously.com.dollargame.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Map;

import sleepfuriously.com.dollargame.R;

/**
 * todo:  describe class' purpose and context
 */
public class PrefsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    //------------------------
    //  constants
    //------------------------

    public static final boolean SHOW_HINTS_DEFAULT = true;

    public static final RandomizeButtonModes DEFAULT_RANDOMIZE_BUTTON_MODE
            = RandomizeButtonModes.ENTIRE_RANGE;

    /** All the different ways that the randomize button can work */
    enum RandomizeButtonModes {
        ENTIRE_RANGE,
        SOLVABLE_AND_ABOVE,
        EXACTLY_SOLVABLE,
        SOLVABLE_PLUS_ZERO_TO_ONE
    }


    //------------------------
    //  data
    //------------------------

    SharedPreferences mPrefs;

    //------------------------
    //  methods
    //------------------------

    // todo: change max and min dollar amount
    // todo: change color scheme???
    // todo: change what the randomize button does

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This does it directly instead of using a Fragment
        addPreferencesFromResource(R.xml.preference_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPrefs = getPreferences(MODE_PRIVATE);
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        Map<String, ?> prefsMap = mPrefs.getAll();
        for (Map.Entry<String, ?> prefEntry : prefsMap.entrySet()) {
            // iterate through the preference entries and update their
            // summary if they are an instance of EditTextPreference
            if (prefEntry instanceof EditTextPreference) {
                updateSummary((EditTextPreference) prefEntry);
            }
        }
    }


    @Override
    protected void onPause() {
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Map<String, ?> prefsMap = mPrefs.getAll();

        // get the preference that has been changed
        Object changedPref = prefsMap.get(key);

        // and if it's an instance of EditTextPreference class,
        // update its summary
        if (prefsMap.get(key) instanceof EditTextPreference) {
            updateSummary((EditTextPreference) changedPref);
        }
    }


    private void updateSummary(EditTextPreference pref) {
        // set the EditTextPreference's summary value to
        // its current text
        pref.setSummary(pref.getText());
    }

}

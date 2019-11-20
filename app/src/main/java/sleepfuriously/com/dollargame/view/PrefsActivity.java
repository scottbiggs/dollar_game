package sleepfuriously.com.dollargame.view;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import sleepfuriously.com.dollargame.R;

/**
 * todo:  describe class' purpose and context
 */
public class PrefsActivity extends PreferenceActivity {

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

    //------------------------
    //  methods
    //------------------------

    // todo: allow hints to disapper
    // todo: change max and min dollar amount
    // todo: change color scheme???
    // todo: change what the randomize button does

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This does it directly instead of using a Fragment
        addPreferencesFromResource(R.xml.preference_main);


    }


}

package sleepfuriously.com.dollargame.view;

import android.content.Context;
import android.util.AttributeSet;

import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton;
import com.fangxu.allangleexpandablebutton.ButtonData;

import java.util.ArrayList;
import java.util.List;

import sleepfuriously.com.dollargame.R;

/**
 * Created on 2019-08-06.
 */
public class NodeButton extends AllAngleExpandableButton {
    //---------------------
    //  constants
    //---------------------

    private static final String TAG = NodeButton.class.getSimpleName();

    /** width & height of button in dips */
    public static final float BUTTON_WIDTH = 60f,
                              BUTTON_HEIGHT = 60f;

    //---------------------
    //  data
    //---------------------

    /** Holds drawables for the buttons */
    private int[] mButtonDrawables = {
            R.drawable.circle,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
            };

    //---------------------
    //  methods
    //---------------------

    public NodeButton(Context context) {
        super(context);
        init(context, null);
    }

    public NodeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NodeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        // create the buttons
        final List<ButtonData> buttons = new ArrayList<>();

        for (int i = 0; i < mButtonDrawables.length; i++) {
            ButtonData aButton = ButtonData.buildIconButton(context, mButtonDrawables[i], 0);
            buttons.add(aButton);
        }
        setButtonDatas(buttons);


    }


}

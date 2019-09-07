package sleepfuriously.com.dollargame.view;

import android.content.Context;
import android.util.AttributeSet;

//import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton;
//import com.fangxu.allangleexpandablebutton.ButtonData;

import java.util.ArrayList;
import java.util.List;

import sleepfuriously.com.dollargame.R;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.AllAngleExpandableButton;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonData;

/**
 * Created on 2019-08-06.
 */
public class NodeButton extends AllAngleExpandableButton {

    //---------------------
    //  constants
    //---------------------

    private static final String TAG = NodeButton.class.getSimpleName();

    //---------------------
    //  data
    //---------------------

    private Context mCtx;

    /** Holds drawables for the buttons */
    private int[] mButtonDrawables = {
            R.drawable.black_circle,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
            };

    private int[] mButtonDisabledDrawables = {
            R.drawable.red_circle,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
    };


    //---------------------
    //  methods
    //---------------------

    public NodeButton(Context ctx) {
        super(ctx);
        init(ctx, null);
    }

    public NodeButton(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init(ctx, attrs);
    }

    public NodeButton(Context ctx, AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        init(ctx, attrs);
    }


    private void init(Context ctx, AttributeSet attrs) {

        mCtx = ctx;

        // create the buttons
        final List<ButtonData> buttons = setButtonImageData(ctx, false);

        // make the buttons go left/right
        setStartAngle(0);
        setEndAngle(180);

        // I'm pretty sure that most settings need to be done before
        // this is called (or in XML).
        setButtonDatas(buttons);
    }

    /**
     * Helper method to set the proper set of images for this button.
     * Should also be good for RESETTING the image data of an existing
     * button.
     *
     * It seems that the first button is the main button, ie the only one
     * visible at first.  All the other buttons that pop up are the ones
     * after.
     *
     * Note: I'm pretty sure that you need to call setButtonDatas() after
     * this for it to work.
     *
     * @param ctx
     *
     * @param disabled  When TRUE, use the disabled version of the button.
     *
     * todo:  This is inefficient if I'm just changing ONE drawable!  Take the time to do it right!
     */
    private List<ButtonData> setButtonImageData(Context ctx, boolean disabled) {

        List<ButtonData> buttons = new ArrayList<>();

        int[] drawablesArray = disabled ? mButtonDisabledDrawables : mButtonDrawables;

        for (int i = 0; i < drawablesArray.length; i++) {
            ButtonData aButton;
            if (i == 0) {
                // I'm adding a message to the first button
                aButton = ButtonData.buildIconAndTextButton(ctx, drawablesArray[0], 0, "yo");
//                // you can also change the message directly
//                aButton.setText("hi");
            }
            else {
                aButton = ButtonData.buildIconButton(ctx, drawablesArray[i], 0);
            }

            buttons.add(aButton);
        }

        return buttons;
    }


    /**
     * Returns if this button is currently disabled.
     * Disabled buttons do NOT do their fancy button animation. They may
     * move (if movement is enabled) and change highlight/color.
     */
    public boolean isDisabled() {
        return mDisabled;
    }

    /**
     * Disable or enable the button.  Only enabled buttons (default) will do
     * their fancy animations with secondary buttons.
     *
     * Disabled buttons still may move and do their highlights/color changes.
     *
     * @param   disabled    True means that this button will be DISABLED.
     *                      False enables it (of course).
     */
    public void setDisabled(boolean disabled) {
        // only do any work if we have to!
        if (mDisabled == disabled) {
            return;
        }

        mDisabled = disabled;

        List<ButtonData> buttons = setButtonImageData(mCtx, disabled);
        setButtonDatas(buttons);
        invalidate();
    }


}

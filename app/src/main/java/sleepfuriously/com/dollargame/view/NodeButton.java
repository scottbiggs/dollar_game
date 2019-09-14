package sleepfuriously.com.dollargame.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

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

    private int[] mButtonBuildNormalDrawables = {
            R.drawable.build_circle_normal,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
    };

    private int[] mButtonBuildHighlitDrawables = {
            R.drawable.red_circle,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
    };

    private int[] mButtonSolveNormalDrawables = {
            R.drawable.solve_circle_normal,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
    };

    /** current highlight state of this button */
    private boolean mHighlighted = false;


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
        final List<ButtonData> buttons = setButtonImageData(ctx, mButtonBuildNormalDrawables, "N");

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
     * @param ctx   always needed
     *
     * @param imageArray    An array of drawable ids. The first will be the main button.
     *
     * @param title     Text used for the first drawable (main button). Use null if no
     *                  text is needed.
     *
     * todo:  This is inefficient if I'm just changing ONE drawable!  Take the time to do it right!
     */
    private List<ButtonData> setButtonImageData(Context ctx, int[] imageArray, String title) {

        List<ButtonData> buttons = new ArrayList<>();

        for (int i = 0; i < imageArray.length; i++) {
            ButtonData aButton;
            if ((i == 0) && (title != null)) {
                aButton = ButtonData.buildIconAndTextButton(ctx, imageArray[i], 0, title);
            }
            else {
                aButton = ButtonData.buildIconButton(ctx, imageArray[i], 0);
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
        if (mDisabled == disabled) {
            Log.e(TAG, "setting disabled to " + disabled + " twice!");
            return;
        }
        mDisabled = disabled;
    }


    public boolean isHighlighted() {
        return mHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        if (mHighlighted == highlighted) {
            Log.e(TAG, "setting highlighted to " + highlighted + " twice!");
            return;
        }

        mHighlighted = highlighted;

        List<ButtonData> buttons = setButtonImageData(mCtx, mButtonBuildHighlitDrawables, "H");
        setButtonDatas(buttons);
        invalidate();
    }


}

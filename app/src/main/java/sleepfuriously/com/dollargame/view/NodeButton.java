package sleepfuriously.com.dollargame.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

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

    /** default for buttons: are they movable or not? */
    protected static final boolean DEFAULT_MOVABLE = false;

    /**
     * The amount of pixels a finger can slip around and
     * still be considered a click and not a move
     */
    private static final float CLICK_SLOP = 3f;

    /** Number of milliseconds between a click and a move. */
    private static final long CLICK_MILLIS_THRESHOLD = 100L;

    /** Holds drawables for the buttons */
    private static final
        int[] BUTTON_DRAWABLES_NORMAL = {
            R.drawable.black_circle,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
        },
        BUTTON_DRAWABLES_RED = {
            R.drawable.red_circle,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
        },
        BUTTON_DRAWABLES_BUILD_NORMAL = {
            R.drawable.build_circle_normal,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
        },
        BUTTON_DRAWABLES_BUILD_HIGHIGHT = {
            R.drawable.red_circle,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
        },
        BUTTON_DRAWABLES_SOLVE_NORMAL = {
            R.drawable.solve_circle_normal,
            R.drawable.ic_give_money,
            R.drawable.ic_take_money
        };


    //---------------------
    //  data
    //---------------------

    private Context mCtx;

    /** When TRUE, this button is in movable mode */
    private static boolean mMovable = false;

    /** Indicates if the button is actually in the process of being moved */
    private static boolean mMoving = false;
    /** screen coordinates of the beginning of a move */
    private float mMoveStartX, mMoveStartY;
    /** ??? difference between screen and view coords ??? */
    private float mMoveDiffX, mMoveDiffY;

    /** System time when a touch event starts on this button */
    private long mStartMillis = 0L;

    /** the disabled state of this button */
    protected boolean mDisabled = false;


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

    @Override
    protected void init(Context ctx, AttributeSet attrs) {

        super.init(ctx, attrs);
        mCtx = ctx;

        // create the buttons
        final List<ButtonData> buttons = setButtonImageData(ctx, BUTTON_DRAWABLES_BUILD_NORMAL, "N");

        // make the buttons go left/right
        setStartAngle(0);
        setEndAngle(180);

        // I'm pretty sure that most settings need to be done before
        // this is called (or in XML).
        setButtonDatas(buttons);

        @SuppressLint("CustomViewStyleable")
        TypedArray ta = ctx.obtainStyledAttributes(attrs, R.styleable.AllAngleExpandableButton);
        mMovable = ta.getBoolean(R.styleable.AllAngleExpandableButton_aebMovable, DEFAULT_MOVABLE);
        ta.recycle();
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

        List<ButtonData> buttons;
        if (highlighted) {
            buttons = setButtonImageData(mCtx, BUTTON_DRAWABLES_BUILD_HIGHIGHT, "H");
        }
        else {
            buttons = setButtonImageData(mCtx, BUTTON_DRAWABLES_BUILD_NORMAL, "N");
        }

        setButtonDatas(buttons);
        invalidate();
    }


    // Overrides AllAngleExpandable to handle movement of buttons during
    // build phase.
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mMovable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (checker.isQuick()) {
                        return false;
                    }
                    startMove(event);
                    return true;    // event consumed

                case MotionEvent.ACTION_MOVE:
                    continueMove(event);
                    return true;

                case MotionEvent.ACTION_UP:
                    if (mMovable && mMoving) {
                        finishMove(event);
                        return true;
                    }
                    if (mDisabled) {
                        // pass the event on to higher level
                        buttonEventListener.onDisabledClick();
                    }

                    break;
            }

        }
        return super.onTouchEvent(event);

    }


    /**
     * Begins a move of the button.  Assumes that the button CAN move!
     */
    private void startMove(MotionEvent event) {
        mMoving = true;
        mMoveStartX = event.getRawX();
        mMoveStartY = event.getRawY();
        mMoveDiffX = getX() - mMoveStartX;
        mMoveDiffY = getY() - mMoveStartY;
    }

    private void continueMove(MotionEvent event) {
        if (mMoving) {
            setX(event.getRawX() + mMoveDiffX);
            setY(event.getRawY() + mMoveDiffY);
        }
    }

    private void finishMove(MotionEvent event) {

        if (mMoving) {
            if (isRealMove(event)) {
                setX(event.getRawX() + mMoveDiffX);
                setY(event.getRawY() + mMoveDiffY);

                // reestablish our button's rectangle
                getGlobalVisibleRect(rawButtonRect);
                rawButtonRectF.set(rawButtonRect.left, rawButtonRect.top, rawButtonRect.right, rawButtonRect.bottom);
            }
            else {
                // Not enough movement to consider this a move. Let's treat this as
                // a button click.
                // todo: pass through click event
                buttonEventListener.onDisabledClick();

                // reset the button's location (just in case it moved a little)
                setX(mMoveStartX + mMoveDiffX);
                setY(mMoveStartY + mMoveDiffY);
            }
            mMoving = false;
        }
    }

    /**
     * Determines if the user's finger has moved enough to consider this a real
     * move event, or if this is just slight shuddering of a finger that happens
     * during a button click.
     *
     * @param event     The current event that finalized the move (should be an
     *                  ACTION_UP event).  We'll need the coordinates.
     *
     * @return  TRUE iff the current location is sufficiently far enough away from
     *          the original ACTION_DOWN event to qualify as a move.
     *
     * todo: may need to use the time as well (moves take a lot longer than clicks)
     */
    private boolean isRealMove(MotionEvent event) {

        float currentX = event.getRawX();
        float currentY = event.getRawY();

        // check not enough movement
        if ((Math.abs(currentX - mMoveStartX) < CLICK_SLOP) &&
                (Math.abs(currentY - mMoveStartY) < CLICK_SLOP)) {
            return false;
        }

        // check not enough time
        long currentMillis = System.currentTimeMillis();
        if (currentMillis - mStartMillis < CLICK_MILLIS_THRESHOLD) {
            return false;
        }

        return true;
    }


    public boolean isMovable() {
        return mMovable;
    }

    public void setMovable(boolean movable) {
        mMovable = movable;
    }




}

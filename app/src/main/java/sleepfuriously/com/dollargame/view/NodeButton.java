package sleepfuriously.com.dollargame.view;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import sleepfuriously.com.dollargame.R;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.AllAngleExpandableButton;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonData;

/**
 * Created on 2019-09-26.
 */
public class NodeButton extends AllAngleExpandableButton {

    //-------------------------------
    //  constants
    //-------------------------------

    private static final String TAG = NodeButton.class.getSimpleName();

    /** The different button modes. Lots of button features depend on the current Mode. */
    enum Modes {
        BUILD_MODE, SOLVE_MODE
    };

    /** Enumerates the different ways a button may be highlighted. */
    enum HighlightTypes {
        NORMAL, CONNECT, PROBLEM
    };

    /**
     * The amount of pixels a finger can slip around and
     * still be considered a click and not a move
     */
    private static final float CLICK_SLOP = 3f;

    /** Number of milliseconds between a click and a move. */
    private static final long CLICK_MILLIS_THRESHOLD = 100L;


    /** Holds drawables for the buttons */
    private static final int[]
        BUTTON_DRAWABLES_BUILD_NORMAL = {
        R.drawable.circle_build_normal,
                R.drawable.ic_give_money,   // todo: change because there's no need for these images?
                R.drawable.ic_take_money
        },
        BUTTON_DRAWABLES_BUILD_PROBLEM = {
                R.drawable.circle_build_problem,
                R.drawable.ic_give_money,   // todo: change because there's no need for these images?
                R.drawable.ic_take_money
        },
        BUTTON_DRAWABLES_BUILD_CONNECT = {
                R.drawable.circle_build_connect,
                R.drawable.ic_give_money,   // todo: change because there's no need for these images?
                R.drawable.ic_take_money
        };


    //-------------------------------
    //  widgets
    //-------------------------------

    //-------------------------------
    //  data
    //-------------------------------

    private Context mCtx;

    private Modes mCurrentMode;

    private HighlightTypes mCurrentHighlight;

    /** Indicates if the button is actually in the process of being moved */
    private boolean mMoving = false;

    /** screen coordinates of the beginning of a move */
    private float mMoveStartX, mMoveStartY;

    /** ??? difference between screen and view coords ??? */
    private float mMoveDiffX, mMoveDiffY;

    /** position of the last known coord for this button */
    private float mMoveLastX, mMoveLastY;

    /** System time when a touch event starts on this button */
    private long mStartMillis = 0L;


    //-------------------------------
    //  public methods
    //-------------------------------

    public NodeButton(Context ctx) {
        this(ctx, null);
    }

    public NodeButton(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        initNodeButton(ctx, attrs);
    }


    /**
     * The the mode of this button. A great many aspects of the button will
     * change based on its mode.<br>
     * <br>
     * side effects:<br>
     *      Too numerous to enumerate.
     *
     * @param newMode   Either BUILD or SOLVE.  Will generate an error
     *                  Logcat if newMode is the same as the current mode.
     */
    public void setMode(Modes newMode) {
        if (mCurrentMode == newMode) {
            Log.e(TAG, "error:  setMode( " + newMode + " ) is redundant.");
        }
        mCurrentMode = newMode;
    }

    public Modes getMode() {
        return mCurrentMode;
    }

    /** Sets the text of this button to the given String. */
    public void setText(String str) {
        // todo
    }

    /** Returns the text currently displayed by this button */
    public String getText() {
        // todo
        return null;
    }

    public void setHighlight(HighlightTypes newHighlightType) {

        // Check for error conditions
        if (mCurrentHighlight == newHighlightType) {
            Log.e(TAG, "Error in setHighlight( " + newHighlightType + " ) -- already using this type!");
            throw new IllegalArgumentException();
        }

        if ((mCurrentMode == Modes.SOLVE_MODE) && (newHighlightType == HighlightTypes.CONNECT)) {
            Log.e(TAG, "Non-sense highlight type in setHighlight()! Cannot do a CONNECT highlight in SOLVE_MODE!");
            throw new IllegalArgumentException();
        }

        // Set the new highlight and make the drawable changes.
        mCurrentHighlight = newHighlightType;
        List<ButtonData> buttonData = getButtonImages(mCtx);
        setButtonDatas(buttonData);
        invalidate();   // todo: test to see if this is necessary!
    }


    public HighlightTypes getHighlightType() {
        return mCurrentHighlight;
    }

    // Overrides AllAngleExpandable to handle movement of buttons during
    // build phase.
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mCurrentMode == Modes.BUILD_MODE) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
//                        if (checker.isQuick()) {  // todo: not sure this is needed, so I turned it off
//                            return false;
//                        }
                    startMove(event);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (mMoving) {
                        continueMove(event);
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (mMoving) {
                        finishMove(event);
                        return true;
                    }
            }
        }

        else if (mCurrentMode == Modes.SOLVE_MODE) {
            // pass the event on to higher level
            buttonEventListener.onDisabledClick();
        }

        else {
            Log.e(TAG, "Unhandled mode in NodeButton.onTouchEvent()!");
        }

//        return super.onTouchEvent(event);
        return true;    // todo: not sure about this
    }


    //-------------------------------
    //  protected methods
    //-------------------------------


    //-------------------------------
    //  private methods
    //-------------------------------

    private void initNodeButton(Context ctx, AttributeSet attrs) {

        mCtx = ctx;

        mCurrentMode = Modes.BUILD_MODE;
        mCurrentHighlight = HighlightTypes.NORMAL;

        mMoving = false;

        // create the buttons, based on the current state
        final List<ButtonData> buttons = getButtonImages(mCtx);

        // make the buttons go left/right
        setStartAngle(0);
        setEndAngle(180);

        // I'm pretty sure that most settings need to be done before
        // this is called (or in XML).
        setButtonDatas(buttons);

//        @SuppressLint("CustomViewStyleable")
//        TypedArray ta = ctx.obtainStyledAttributes(attrs, R.styleable.AllAngleExpandableButton);
//        mMovable = ta.getBoolean(R.styleable.AllAngleExpandableButton_aebMovable, DEFAULT_MOVABLE);
//        ta.recycle();

    }


    /**
     * Returns a list of button images appropriate the current state of this button.
     *
     * preconditions:
     *      mCurrentMode
     *      mCurrentHighlight
     *
     */
    private List<ButtonData> getButtonImages(Context ctx) {

        List<ButtonData> buttonData = null;
        switch (mCurrentMode) {

            case BUILD_MODE:
                switch (mCurrentHighlight) {
                    case NORMAL:
                        buttonData = setButtonImageData(ctx, BUTTON_DRAWABLES_BUILD_NORMAL, "N");
                        break;

                    case CONNECT:
                        buttonData = setButtonImageData(ctx,BUTTON_DRAWABLES_BUILD_CONNECT, "C");
                        break;

                    case PROBLEM:
                        buttonData = setButtonImageData(ctx,BUTTON_DRAWABLES_BUILD_PROBLEM, "E");
                        break;
                }
                break;

            case SOLVE_MODE:
                // todo
                break;

            default:
                Log.e(TAG, "unknown mode in getButtonImages--Aborting!");
                break;
        }

        return buttonData;
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
     * Begins a move of the button.  Assumes that the button CAN move!
     */
    private void startMove(MotionEvent event) {
        mMoving = true;
        mMoveStartX = event.getRawX();
        mMoveStartY = event.getRawY();

        mMoveLastX = getX();
        mMoveLastY = getY();

        mMoveDiffX = getX() - mMoveStartX;
        mMoveDiffY = getY() - mMoveStartY;

    }

    private void continueMove(MotionEvent event) {
        if (mMoving) {
            setX(event.getRawX() + mMoveDiffX);
            setY(event.getRawY() + mMoveDiffY);

            PointF lastPos = new PointF(mMoveLastX, mMoveLastY);
            PointF currentPos = new PointF(event.getX(), event.getY());
            buttonEventListener.onMoved(lastPos, currentPos);

            mMoveLastX = event.getX();
            mMoveLastY = event.getY();
        }
    }

    private void finishMove(MotionEvent event) {

        if (mMoving) {
            if (isRealMove(event)) {
                setX(event.getRawX() + mMoveDiffX);
                setY(event.getRawY() + mMoveDiffY);

                PointF lastPos = new PointF(mMoveLastX, mMoveLastY);
                PointF currentPos = new PointF(event.getX(), event.getY());
                buttonEventListener.onMoved(lastPos, currentPos);

                mMoveLastX = event.getX();
                mMoveLastY = event.getY();

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

}

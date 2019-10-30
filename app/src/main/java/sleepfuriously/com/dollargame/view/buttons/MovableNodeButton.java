package sleepfuriously.com.dollargame.view.buttons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import sleepfuriously.com.dollargame.R;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.AllAngleExpandableButton;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonData;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;


/**
 * This should be the final Button for this app.  It moves, it provides
 * expandable sub-buttons, it does everything!<br>
 * <br>
 * But note that in the current implementation, movable and expandable are mutually exclusive.
 * If you make the button movable, then it will NOT do expandable buttons (may change this later)
 * and vice-versa.<br>
 * <br>
 * You can make set the movability by calling {@link #setMovable(boolean)} or {@link #setExpandable(boolean)}.<br>
 * <br>
 * You need to register the {@link OnMoveListener} to get
 * movement events and register the {@link sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonEventListener}
 * to get click events.  It's pretty standard, but uses 2 listeners
 * instead of one.<br>
 * <br>
 * Aaaaannnd, while a button is movable (not expandable), only callbacks to {@link OnMoveListener}
 * are called.  And conversely while a button is expandable (not movable), only callbacks to
 * {@link sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonEventListener} occur.
 */
public class MovableNodeButton extends AllAngleExpandableButton {

    //-------------------------------
    //  constants
    //-------------------------------

    private static final String TAG = MovableNodeButton.class.getSimpleName();

    /** The amount that a touch must move to be considered a real move and not just an inadvertant wiggle */
    private static final float MOVE_THRESHOLD = 5f;

    /** The number of milliseconds before a click becomes a long click */
    private static final long MILLIS_FOR_LONG_CLICK = 1000L;

    //-------------------------------
    //  data
    //-------------------------------

    private Context mCtx;

    /** The start time in millis of a click */
    private long mClickStartMillis;

    /** raw coordinates of where a click starts */
    private float mStartRawX, mStartRawY;

    /** offsets from the button's view and raw coords */
    private float mOffsetX, mOffsetY;

    /** Indicates if this button is movable or not. Note that while movable, expanded buttons are disabled. */
    private boolean mMovable;

    /** true iff the button is in the process of moving */
    private boolean mMoving;

    private OnMoveListener moveListener;

    /** The actual color of the current highlight */
    private int mCurrentHighlightColor;


    //-------------------------------
    //  methods
    //-------------------------------

    public MovableNodeButton(Context ctx) {
        this(ctx, null);
    }

    public MovableNodeButton(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        initialize(ctx, attrs);
    }


    private void initialize(Context ctx, AttributeSet attrs) {
        mCtx = ctx;
        mMoving = false;
        mMovable = true;

        // setup the button itself
        setOutlineColor(R.color.button_build_border_normal);
    }

    /**
     * Returns whether or not this button is movable (true) or is locked-down (false).
     * Note that when a button is movable, it is NOT expandable and vice-versa.
     */
    public boolean getMovable() {
        return mMovable;
    }

    /**
     * Sets whether or not this button can be moved.  Use this to lock the button down.
     * Note that when a button is movable, it is NOT expandable and vice-versa.
     */
    public void setMovable(boolean movable) {
        mMovable = movable;
    }

    /**
     * Returns whether this button is expandable (true) or inhibits the expanding buttons (false).
     * Note that when a button is expandable, it is NOT movable and vice-versa.
     */
    public boolean getExpandable() {
        return !mMovable;
    }

    /**
     * Sets whether this button is expandable.  Use this to enable or disable expanding buttons.
     * Note that when a button is expandable, it is NOT movable and vice-versa.
     */
    public void setExpandable(boolean expandable) {
        mMovable = !expandable;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mMovable) {
            return processMovableTouchEvent(event);
        }

        else {
            return super.onTouchEvent(event);
        }

    }

    /**
     * Localizes the processing of a touch event when the button is MOVABLE.
     *
     * @param event     The original onTouch MotionEvent.
     *
     * @return  True - event completely consumed.
     *          False - continue processing this event down the UI chain.
     */
    private boolean processMovableTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case ACTION_DOWN:
                mClickStartMillis = System.currentTimeMillis();

                mStartRawX = event.getRawX();
                mStartRawY = event.getRawY();

                // save the offset from the click in the button's context vs the raw location
                mOffsetX = mStartRawX - getX();
                mOffsetY = mStartRawY - getY();
                break;

            case ACTION_MOVE:
                // only move if we're already movingTo AND the finger has moved enough to
                // be considered a move.
                if (mMoving || movedPastThreshold(event)) {
                    mMoving = true;

                    // calc the move differences
                    float diffX = event.getRawX() - mOffsetX;
                    float diffY = event.getRawY() - mOffsetY;

                    moveListener.movingTo(diffX, diffY);

                    animate()
                            .x(diffX)
                            .y(diffY)
                            .setDuration(0)
                            .start();
                }
                else {
                    return false;
                }
                break;

            case ACTION_UP:
                if (mMoving) {
                    mMoving = false;
                    moveListener.moveEnded();
                    // registered
                }
                else {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - mClickStartMillis < MILLIS_FOR_LONG_CLICK) {
                        moveListener.clicked();
                    }
                    else {
                        moveListener.longClicked();
                    }
                }
                break;
        }
        return true;    // event consumed
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean movedPastThreshold(MotionEvent event) {
        float currentX = event.getRawX();
        float currentY = event.getRawY();

        // if we're within the threshold, return false (we have NOT moved past the threshold)
        if ((Math.abs(currentX - mStartRawX) < MOVE_THRESHOLD) &&
                (Math.abs(currentY - mStartRawY) < MOVE_THRESHOLD)) {
            return false;
        }

        return true;
    }

    /**
     * Use this to set the callback when implementing the {@link OnMoveListener}
     * interface.
     *
     * @param listener  The instance that is implementing the interface.
     */
    public void setOnMoveListener(OnMoveListener listener) {
        moveListener = listener;
    }


    /**
     * Sets the color of the main button's outline.
     *
     * @param colorResource     Resource ID of the color to highlight
     *                          the primary button.
     */
    public void setOutlineColor(int colorResource) {
        mCurrentHighlightColor = getResources().getColor(colorResource);

        // make the buttons go left/right
        setStartAngle(0);
        setEndAngle(180);

        setButtonDatas(createButtonImages(mCurrentHighlightColor));

        Log.d(TAG, "setOutlineColor()");
    }

    /**
     * Helper method that creates a list of the appropriate button images
     * for the current state and outline color.
     *
     * @param highlightColor    Color to use for highlighting.
     *                          Use null to use the default (black).
     *
     * @return  A list of button images suitable for sending to
     *          <code>setButtonDatas()</code>.
     */
    private List<ButtonData> createButtonImages(int highlightColor) {
        // Create a drawable with the correct color
        Drawable highlightDrawable = AppCompatResources.getDrawable(mCtx, R.drawable.circle_black);

        // todo: change color!!!!

        List<ButtonData> buttonDataList = new ArrayList<>();

        ButtonData highlightButtonData = ButtonData.buildIconAndTextButton(mCtx, highlightDrawable, 0, "$");
        ButtonData firstPopup = ButtonData.buildIconButton(mCtx, R.drawable.ic_give_money, 0);
        ButtonData secondPopup = ButtonData.buildIconButton(mCtx, R.drawable.ic_take_money, 0);

        buttonDataList.add(highlightButtonData);
        buttonDataList.add(firstPopup);
        buttonDataList.add(secondPopup);

        return buttonDataList;
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  interfaces & classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public interface OnMoveListener {

        /**
         * Called when a move has been detected, but before any action has taken
         * place.  This will provide a way for the caller to know that a move is
         * happening and to prepare accordingly.
         *
         * This will be called many times during a single user's action--once
         * for each detected movement.  Essentially each time a MotionEvent.ACTION_MOVE
         * is initiated, this will be called.
         *
         * @param diffX     The difference between where the button currently is
         *                  and where it will be once the move is complete. X axis.
         *
         * @param diffY     Y axis
         */
        void movingTo(float diffX, float diffY);

        /**
         * Signals that a move has been completed with this button.
         *
         * Coincides with MotionEvent.ACTION_UP.
         */
        void moveEnded();

        /**
         * The user has clicked on this button, not moved it.
         */
        void clicked();

        /**
         * User has long-clicked (over a second) a button, but has not moved it.
         */
        void longClicked();
    }

}

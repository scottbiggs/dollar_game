package sleepfuriously.com.dollargame.view.buttons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

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
 * There are three modes ({@link Modes} which determine how the button behaves and the UI events it signals.
 * Depending on the mode, the button can be expandable, movable, or click-only.<br>
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

    /**
     * The modes of this button.  These modes completely determine the behavior of this
     * button and how it returns UI input to the implementer of the interfaces.<br>
     * <br>
     * The following modes are used:<br>
     * {@link #MOVABLE}<br>
     * {@link #EXPANDABLE}<br>
     * {@link #CLICKS_ONLY}
     */
    public enum Modes {
        /**
         * The button is movable. It registers movement, clicks, and long clicks.
         * No expanding buttons will display.
         */
        MOVABLE,
        /** Expanding buttons are displayed and will register. No movement will happen. */
        EXPANDABLE,
        /** Not movable nor will any expanding buttons will appear. Only registers clicks  and long clicks. */
        CLICKS_ONLY
    }

    /** size of the stroke in the main button background */
    private static final int STROKE_WIDTH = 3;

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

    /**
     * Indicates the current mode of this button.
     * @see Modes
     */
    private Modes mCurrentMode;

    /** true iff the button is in the process of moving */
    private boolean mMoving;

    private OnMoveListener moveListener;

    /** The actual color of the current highlight */
    private int mCurrentHighlightColor;

    /** background color of the main button */
    private int mCurrentBackgroundColor;

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
        mCurrentMode = Modes.MOVABLE;

        // make the buttons go left/right todo: is this necessary?
        setStartAngle(0);
        setEndAngle(180);

        setButtonDatas(createButtonImages());
    }

    /**
     * Returns the current {@link Modes} of this button (all-important!).
     */
    public Modes getMode() {
        return mCurrentMode;
    }

    /**
     * Sets the current mode of this button. SO IMPORTANT!
     * @see Modes
     */
    public void setMode(Modes newMode) {
        mCurrentMode = newMode;
    }



    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (mCurrentMode) {
            case MOVABLE:
                return processMovableTouchEvent(event);

            case EXPANDABLE:
                return super.onTouchEvent(event);

            case CLICKS_ONLY:
                return processClickTouchEvent(event);

            default:
                Log.e(TAG, "unhandled onTouchEvent!");
                return true;
        }
    }

    /**
     * Process UI events when in CLICKS_ONLY mode.
     *
     * @param event     The original onTouch MotionEvent.
     *
     * @return  True - event completely consumed.       todo: currently always returns true!!
     *          False - continue processing this event down the UI chain.
     */
    private boolean processClickTouchEvent(MotionEvent event) {

        Log.d(TAG, "processClickTouchEvent(), ACTION = " + event.getAction());

        if (event.getAction() == ACTION_DOWN) {
            mClickStartMillis = System.currentTimeMillis();
            mStartRawX = event.getRawX();
            mStartRawY = event.getRawY();
        }

        else if (event.getAction() == ACTION_UP) {

            long currentTime = System.currentTimeMillis();

            // Don't count this as a click if the user has slid their hand around.
            float currentRawX = event.getRawX();
            float currentRawY = event.getRawY();

            // Only count clicks that haven't moved around much.  Otherwise it's
            // not really a click (it's probably a slide or something, which we're
            // no interested in.
            if ((Math.abs(currentRawX - mStartRawX) < MOVE_THRESHOLD) &&
                    (Math.abs(currentRawY - mStartRawY) < MOVE_THRESHOLD)) {

                if (currentTime - mClickStartMillis < MILLIS_FOR_LONG_CLICK) {
                    moveListener.clicked();
                }
                else {
                    moveListener.longClicked();
                }
            }

            else {
                Log.d(TAG, "not really a click (probably a move)--ignored.");
            }
        }

        return true;    // consume all events that come here
    }


    /**
     * Localizes the processing of a touch event while in MOVEABLE mode.
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
     * Sets the background color of the main button.<br>
     * <br>
     * NOTE: You need to call invalidate() after.
     *
     * @param resid     The resource id of the color
     */
    public void setBackgroundColorResource(int resid) {
        mCurrentBackgroundColor = getResources().getColor(resid);

        List<ButtonData> buttons = getButtonDatas();
        ButtonData mainButtonData = buttons.get(0);

        mainButtonData.setBackgroundColor(mCurrentBackgroundColor);
    }

    /**
     * Sets the color of the main button's outline.
     *
     * @param colorResource     Resource ID of the color to highlight
     *                          the primary button.
     */

    public void setOutlineColor(int colorResource) {

        Toast.makeText(mCtx, "setOutlineColor() NOT implemented!", Toast.LENGTH_SHORT).show();

        // todo

//        GradientDrawable gradientDrawable = (GradientDrawable)getBackground();
//        if (gradientDrawable == null) {
//            Log.d(TAG, "gradientDrawable is NULL!");
//            return;
//        }
//        gradientDrawable.setStroke(STROKE_WIDTH, mCurrentHighlightColor);
    }

    /**
     * Helper method that creates a list of the appropriate button images.
     *
     * @return  A list of button images suitable for sending to
     *          <code>setButtonDatas()</code>.
     */
    private List<ButtonData> createButtonImages() {

        // Create a drawable with the correct color
        Drawable highlightDrawable = AppCompatResources.getDrawable(mCtx, R.drawable.circle_black);

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
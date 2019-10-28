package sleepfuriously.com.dollargame.view.buttons;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;


/**
 * This is a new UI thing--it's a button that moves when the user
 * drags it.  Registers a click in the normal fashion.
 */
public class MovableButton extends android.support.v7.widget.AppCompatButton {

    //---------------------------
    //  constants
    //---------------------------

    @SuppressWarnings("unused")
    private static final String TAG = MovableButton.class.getSimpleName();

    /** The amount that a touch must move to be considered a real move and not just an inadvertant wiggle */
    private static final float MOVE_THRESHOLD = 5f;


    //---------------------------
    //  data
    //---------------------------

    /** raw coordinates of where a click starts */
    private float startRawX, startRawY;

    /** offsets from the button's view and raw coords */
    private float offsetX, offsetY;

    private boolean mMoving = false;

    private OnMoveListener moveListener;

    //---------------------------
    //  methods
    //---------------------------


    public MovableButton(Context context) {
        super(context);
    }

    public MovableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case ACTION_DOWN:
                startRawX = event.getRawX();
                startRawY = event.getRawY();

                // save the offset from the click in the button's context vs the raw location
                offsetX = startRawX - getX();
                offsetY = startRawY - getY();
                break;

            case ACTION_MOVE:
                // only move if we're already movingTo AND the finger has moved enough to
                // be considered a move.
                if (mMoving || movedPastThreshold(event)) {
                    mMoving = true;

                    // calc the move differences
                    float diffX = event.getRawX() - offsetX;
                    float diffY = event.getRawY() - offsetY;

                    moveListener.movingTo(diffX, diffY);

                    animate()
                            .x(diffX)
                            .y(diffY)
                            .setDuration(0)
                            .start();

//                    animate()
//                            .x(event.getRawX() - offsetX)
//                            .y(event.getRawY() - offsetY)
//                            .setDuration(0)
//                            .start();
                }
                break;

            case ACTION_UP:
                if (mMoving) {
                    mMoving = false;
                    moveListener.moveEnded();
                    return true;    // consume this move so click is not
                                    // registered
                }
        }

        return super.onTouchEvent(event);
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean movedPastThreshold(MotionEvent event) {
        float currentX = event.getRawX();
        float currentY = event.getRawY();

        // if we're within the threshold, return false (we have NOT moved past the threshold)
        if ((Math.abs(currentX - startRawX) < MOVE_THRESHOLD) &&
            (Math.abs(currentY - startRawY) < MOVE_THRESHOLD)) {
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
    }

}

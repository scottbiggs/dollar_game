package sleepfuriously.com.dollargame.view;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import sleepfuriously.com.dollargame.R;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.AllAngleExpandableButton;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonData;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonEventListener;

/**
 * Another attempt at making a button for each node.
 *
 * This one is pretty dumb--it doesn't know anything about its
 * state or what it's doing.  All it knows is raw or button mode.
 *
 * In RAW mode, it doesn't do anything fancy. But it does return
 * onTouch() events to anything that implements the DumbNodeButtonListener.
 *
 * In BUTTON mode, this behaves like the AllAngleExpandableButton.  User
 * input is provided via the onClick() method of the DumbNodeButtonListener.
 *
 * Of course, the caller needs to tell this class that a listener exists
 * via
 */
public class DumbNodeButton extends AllAngleExpandableButton
        implements ButtonEventListener {

    //--------------------------
    //  constants
    //--------------------------

    /**
     * The two modes of operation for this button.<br>
     *<br>
     * BUTTON - What you'll primarily use.  This mode
     *          behaves exactly as the AllAngleExpandableButton.
     *          Button clicks respond through
     *          DumbNodeButtonListener.onClick(int).<br>
     *<br>
     * RAW - No fancy stuff--behaves just like a View, but passes
     *       the UI events through DumbNodeButtonListener.onTouch(event).
     */
    enum DumbNodeButtonModes {
        RAW, BUTTON
    }

    private static final String TAG = DumbNodeButton.class.getSimpleName();

    private static final int DEFAULT_OUTLINE_COLOR = R.color.button_build_border_normal;

    private static final DumbNodeButtonModes DEFAULT_MODE = DumbNodeButtonModes.RAW;

    //--------------------------
    //  data
    //--------------------------

    private Context mCtx;

    private DumbNodeButtonModes mCurrentMode;

    /** Reference to a color resource that is the currently used color */
//    private int mCurrentHighlightColorResource;

    /** The actual color of the current highlight */
    private int mCurrentHighlightColor;

    /** Listener to report user events */
    private DumbNodeButtonListener mDumbNodeButtonListener;


    //--------------------------
    //  methods
    //--------------------------

    public DumbNodeButton(Context ctx) {
        this(ctx, null);
    }

    public DumbNodeButton(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        initialize(ctx, attrs);
    }

    /**
     * The mode of a DumbNodeButton determines two things: whether the
     * animated buttons appear or not, and how the user's interactions
     * are relayed to the implementer of DumbNodeButtonListener.<br>
     * <br>
     * See {@link .DumbNodeButtonModes} for more info.
     *
     * @param newMode   The new node.  A warning logcat message is issued
     *                  if we try to do a redundant mode change.
     */
    public void setMode(DumbNodeButtonModes newMode) {
        if (newMode == mCurrentMode) {
            Log.d(TAG, "setMode(" + newMode + " ) warning: already in that mode.");
        }
        mCurrentMode = newMode;
    }

    public DumbNodeButtonModes getMode() {
        return mCurrentMode;
    }

    /**
     * Sets the color of the main button's outline.
     *
     * @param colorResource     Resource ID of the color to highlight
     *                          the primary button.
     */
    public void setOutlineColor(int colorResource) {
        mCurrentHighlightColor = getResources().getColor(colorResource);
        setButtonDatas(createButtonImages(mCurrentMode, mCurrentHighlightColor));

        Log.d(TAG, "setOutlineColor()");
    }

    /**
     * Set the buttons for the BUTTON mode
     */
    public void setButtons() {
        // todo:
    }

    /**
     * Use this method to tell the class that something is listening to this
     * button.  Standard drill.
     */
    public void setDumbNodeButtonListener(DumbNodeButtonListener listener) {
        mDumbNodeButtonListener = listener;
    }

    @Override
    public void onExpand() {
    }

    @Override
    public void onCollapse() {
    }

    /**
     * Called by AllAngleExpandableButton when one of the animated
     * buttons is pressed.  This intermediary routine translates it
     * as call to DumbNodeButtonListener.onPopupButtonClicked(index).
     *
     * Essentially, this means that the user clicked on one of the
     * popup buttons.  The param indicates which button.
     *
     * @param index     The button index, count from startAngle to endAngle,.
     *                  Value starts at 1.
     */
    public void onPopupButtonClicked(int index) {
        if (mCurrentMode != DumbNodeButtonModes.BUTTON) {
            Log.e(TAG, "onPopupButtonClicked() fired, but mode is " + mCurrentMode + " -- aborting!");
            return;
        }
        mDumbNodeButtonListener.onPopupButtonClicked(index);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mCurrentMode == DumbNodeButtonModes.RAW) {
            mDumbNodeButtonListener.onTouch(event);
            return true;    // event consumed
        }

        return super.onTouchEvent(event);
    }

    /**
     * Similar to <code>setXYCenter(x, y)</code>, except this uses
     * screen coordinates.
     *
     * @param screenLoc     The location for the center of this button
     *                      in screen coordinates.
     */
    public void setXYCenterRaw(PointF screenLoc) {
        PointF center = getCenter();
    }

    //---------------------------------
    //  protected methods
    //


    //---------------------------------
    //  private methods
    //

    private void initialize(Context ctx, AttributeSet attrs) {
        Log.d(TAG, "initialzie()");

        mCtx = ctx;mCurrentMode = DEFAULT_MODE;
        setOutlineColor(DEFAULT_OUTLINE_COLOR);

    }

    /**
     * Helper method that creates a list of the appropriate button images
     * for the current state and outline color.
     *
     * @param mode      The button mode to make the buttons for.
     *
     * @param highlightColor    Color to use for highlighting.
     *                          Use null to use the default (black).
     *
     * @return  A list of button images suitable for sending to
     *          <code>setButtonDatas()</code>.
     */
    private List<ButtonData> createButtonImages(DumbNodeButtonModes mode,
                                                int highlightColor) {
        // Create a drawable with the correct color
        Drawable highlightDrawable = AppCompatResources.getDrawable(mCtx, R.drawable.circle_black);

        // todo: change color!!!!

        List<ButtonData> buttonDataList = new ArrayList<>();

        if (mode == DumbNodeButtonModes.RAW) {
            // No sub-buttons, just one button in the list.
            ButtonData highlightRawButtonData = ButtonData.buildIconAndTextButton(mCtx, highlightDrawable, 0, "t");
            buttonDataList.add(highlightRawButtonData);
        }

        else {
            // BUTTON mode (SOLVE)
            ButtonData highlightButtonData = ButtonData.buildIconAndTextButton(mCtx, highlightDrawable, 0, "s");
            ButtonData firstPopup = ButtonData.buildIconButton(mCtx, R.drawable.ic_give_money, 0);
            ButtonData secondPopup = ButtonData.buildIconButton(mCtx, R.drawable.ic_take_money, 0);

            buttonDataList.add(highlightButtonData);
            buttonDataList.add(firstPopup);
            buttonDataList.add(secondPopup);
        }

        return buttonDataList;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  interfaces
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Implement this interface if you wish to get any user
     * feedback from the DumbNodeButton class!
     *
     * The two modes of the DumbNodeButton class stipulates
     * which of these two methods is called in response to
     * user input.
     *
     * BUTTON mode: This is the default and normal operation
     *              of this class.  The user selects a node
     *              and two (or more) sub-buttons pop up.
     *              When the user selects one of these popups,
     *              onPopupButtonClicked(int) is fired.
     *
     * RAW mode:    No popup buttons happen at all.  But the
     *              raw user events are passed along directly
     *              to the implementer of this interface.
     */
    public interface DumbNodeButtonListener {

        /**
         * When the user clicks on one of the popup buttons, this is fired.<br>
         * <br>
         * Note: The popup buttons are enabled ONLY if RAW mode is turned OFF.
         *
         * @param index     button index, count from startAngle to endAngle,
         *                  value is 1 to expandButtonCount
         */
        void onPopupButtonClicked(int index);

        /**
         * A touch event has happened with the button (actually, just passed
         * along from the View to here).  It's up to the caller to handle it.<br>
         * <br>
         * NOTE:  This will ONLY fire is RAW mode is turned ON.
         */
        void onTouch(MotionEvent event);
    }

}

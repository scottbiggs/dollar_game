package sleepfuriously.com.dollargame.view.AllAngleExpandableButton;

import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Created by dear33 on 2016/9/11.
 */
public interface ButtonEventListener {
    /**
     * @param index button index, count from startAngle to endAngle, value is 1 to expandButtonCount
     */
    void onPopupButtonClicked(int index);

    void onExpand();
    void onCollapse();

    /**
     * When DISABLED, this button will behave like a regular View.
     * This signals that the user touched AND released the button.
     */
    void onDisabledClick();

    /**
     * The button has been moved (only happens when the button Mode is BUILD_MODE
     * and the HightlightType is NORMAL).
     *
     * This is merely to let you know that it is at a new position.
     *
     * @param oldLoc    The old location of the button
     *
     * @param newLoc    The new location of the button
     */
    void onMoved(PointF oldLoc, PointF newLoc);

}

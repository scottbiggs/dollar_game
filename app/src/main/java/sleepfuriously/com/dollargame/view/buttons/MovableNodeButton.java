package sleepfuriously.com.dollargame.view.buttons;

import android.content.Context;
import android.util.AttributeSet;

import sleepfuriously.com.dollargame.view.buttons.DumbNodeButton;

/**
 * Using the {@link DumbNodeButton} as a parent, this button adds
 * mobility and its own interface.
 *
 * Yes, this button will handle its own movement!
 */
public class MovableNodeButton extends DumbNodeButton {
    public MovableNodeButton(Context ctx) {
        super(ctx);
    }

    public MovableNodeButton(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }
}

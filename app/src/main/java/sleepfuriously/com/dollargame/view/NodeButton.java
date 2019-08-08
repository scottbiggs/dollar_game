package sleepfuriously.com.dollargame.view;

import android.content.Context;
import android.graphics.Color;
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

    /** width & height of button in dips */
    public static final float BUTTON_WIDTH = 60f,
                              BUTTON_HEIGHT = 60f;

    //---------------------
    //  data
    //---------------------

    /** Holds drawables for the buttons */
    private int[] mButtonDrawables = {
            R.drawable.circle,
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
        // create the buttons
        final List<ButtonData> buttons = new ArrayList<>();

        for (int i = 0; i < mButtonDrawables.length; i++) {
            ButtonData aButton = ButtonData.buildIconButton(ctx, mButtonDrawables[i], 0);

            aButton.setText("hi");// todo: make this work

            aButton.setBackgroundColorId(ctx, android.R.color.transparent);

            buttons.add(aButton);
        }

        // make the buttons go left/right
        setStartAngle(0);
        setEndAngle(180);

        setMaskBackgroundColor(Color.argb(0, 0, 0, 0)); // todo: make this work


        // Not sure, but I think all settings need to be done before
        // this is called.
        setButtonDatas(buttons);



    }


}

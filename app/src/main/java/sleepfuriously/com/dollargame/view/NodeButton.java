package sleepfuriously.com.dollargame.view;

import android.content.Context;
import android.util.AttributeSet;

import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton;

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

    //---------------------
    //  methods
    //---------------------

    public NodeButton(Context context) {
        super(context);
        init(context, null);
    }

    public NodeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NodeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        // todo
    }


}

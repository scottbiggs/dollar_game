package sleepfuriously.com.dollargame.view;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

//import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton;
//import com.fangxu.allangleexpandablebutton.ButtonEventListener;

import java.util.List;

import sleepfuriously.com.dollargame.R;
import sleepfuriously.com.dollargame.model.Node;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.AllAngleExpandableButton;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonEventListener;


/**
 * Attempting to implement the "Dollar Game" discussed by Dr. Holly Kreiger
 * on the Numberphile channel.
 * https://www.youtube.com/watch?v=U33dsEcKgeQ
 */
public class MainActivity extends AppCompatActivity
        implements
            View.OnTouchListener,
            View.OnClickListener,
            ButtonEventListener {

    //------------------------
    //  constants
    //------------------------

    private static final String TAG = MainActivity.class.getSimpleName();

    //------------------------
    //  widgets
    //------------------------

    /** The play are of the game */
    private FrameLayout mPlayArea;

    /**
     * Displays hints at the bottom of the screen. User can touch this
     * area to toggle Android UI things.
     */
    private TextView mHelperTv;

    /** holds all the buttons */
    private List<AllAngleExpandableButton> mButtonList; // todo: make this work with GRaph class


    //------------------------
    //  data
    //------------------------

    //------------------------
    //  methods
    //------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // use my toolbar instead of default
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHelperTv = findViewById(R.id.bottom_hint_tv);

        mPlayArea = findViewById(R.id.play_area_fl);
        mPlayArea.setOnTouchListener(this);
//        mPlayArea.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isAndroidUiDisplaying()) {
//                    fullScreen(true);
//                }
//                else {
//                    fullScreen(false);
//                }
//            }
//        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        // turn off status and navigation bar (top and bottom)
        fullScreen(true);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mPlayArea) {
            playAreaTouched(event);
        }
        else if (v == mHelperTv) {
            helperTouched(event);
        }

        return true;    // event consumed
    }

    /**
     * Processes a touch event in the play area.
     *
     * @param event     MotionEvent
     */
    private void playAreaTouched(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // todo: check to see if this is over a button. If so, mark this button
                break;
            case MotionEvent.ACTION_UP:
                NodeButton button = newButton(event.getX(), event.getY());
                break;
        }
    }

    /**
     * User touched the bottom hint/helper area.  Just toggle the
     * Android UI stuff.
     *
     * @param event     Describes the touch event
     */
    private void helperTouched(MotionEvent event) {

        // when releasing their finger, toggle the UI
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isAndroidUiDisplaying()) {
                fullScreen(true);
            }
            else {
                fullScreen(false);
            }
        }

    }


    /**
     * Tells if the Android UI components of status bar and navbar are
     * currently displayed.
     *
     * @return  True - yes, they are displayed
     */
    private boolean isAndroidUiDisplaying() {
        // Toggle Android UI elements.  This is complicated!
        // I have to compare the actual screen size (getMetrics)
        // with the size returned by getSize.  If they are different,
        // then the navbar and stuff are displayed.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);

        boolean androidUiDisplaying = (metrics.heightPixels == p.y);
        if (androidUiDisplaying) {
            // check other axis (some devices keep the bars on the ends in landscape mode)
            androidUiDisplaying = (metrics.widthPixels == p.x);
        }

        return androidUiDisplaying;
    }

    /**
     * Turns the full-screen mode on or off.
     *
     * @param turnOn    When TRUE, turn ON fullscreen.  False turns it off.
     */
    private void fullScreen(boolean turnOn) {
        View decorView = getWindow().getDecorView();
        int uiOptions;

        if (turnOn) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        else {
            uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        }

        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.build_menu, menu);

        // nice simple font for menu
        Typeface tf = FontCache.get("fonts/roboto_med.ttf", this);

        // Change the fonts of the menu
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SpannableStringBuilder ssb = new SpannableStringBuilder(menuItem.getTitle());
            MyTypefaceSpan span = new MyTypefaceSpan(tf);
            ssb.setSpan(span, 0, ssb.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            menuItem.setTitle(ssb);
        }


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.build_about:
                break;

            case R.id.build_help:
                break;

            case R.id.build_load:
                break;

            case R.id.build_share:
                break;

            case R.id.build_solve:
                break;

            default:
                Toast.makeText(this, "unknown menu selection", Toast.LENGTH_LONG).show();
                break;
        }

        return true;   // end processing (consumed completely)
    }


    /**
     * Does all the UI for changing to Build mode.
     */
    private void buildMode() {
        // todo
    }

    /**
     * Does all the UI for Solve mode.
     */
    private void solveMode() {
        // todo
    }


    /**
     * Adds a button to the given coords.
     */
    private NodeButton newButton(float x, float y) {

        NodeButton button = new NodeButton(this);
        button.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(this);

        button.setXYCenter(x, y);

        mPlayArea.addView(button);

        return button;
    }


    @Override
    public void onClick(View v) {
        if (v instanceof AllAngleExpandableButton) {
            // todo
        }
    }


    @Override
    public void onButtonClicked(int i) {
        Log.d(TAG, "onButtonClicked ( " + i + " )");
    }

    @Override
    public void onExpand() {

    }

    @Override
    public void onCollapse() {

    }
}

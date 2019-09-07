package sleepfuriously.com.dollargame.view;

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
import android.widget.FrameLayout;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import sleepfuriously.com.dollargame.R;
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

    /** holds all the buttons */
    private List<NodeButton> mButtonList = new ArrayList<>(); // todo: make this work with GRaph class


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

        mPlayArea = findViewById(R.id.play_area_fl);
        mPlayArea.setOnTouchListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        // turn off status and navigation bar (top and bottom)
        fullScreenStickyImmersive();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mPlayArea) {
            playAreaTouched(event);
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
                mButtonList.add(button);
                break;

            case MotionEvent.ACTION_MOVE:
                // todo: draw lines if necessary
                break;
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
     * Sets the display to fullscreen sticky immersive mode.
     * @see  <a href="https://developer.android.com/training/system-ui/immersive#Options">google docs</a>
     */
    private void fullScreenStickyImmersive() {
        View decorView = getWindow().getDecorView();
        int uiOptions;

        uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
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
//        button.setOnClickListener(this);
        button.setButtonEventListener(this);

        button.setXYCenter(x, y);

        mPlayArea.addView(button);

        return button;
    }


    // NOTE:  THIS IS NOT USED for the fancy buttons!!!
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick()");

        if (v instanceof AllAngleExpandableButton) {
            // todo
        }
    }


    @Override
    public void onButtonClicked(int i) {
        // todo: move to anonymous class as there's no way to differentiate between the instances!
        Log.d(TAG, "onButtonClicked ( " + i + " )");
    }

    @Override
    public void onExpand() {
        Log.d(TAG, "onExpand()");
        // todo: move to anonymous class as there's no way to differentiate between the instances!

    }

    @Override
    public void onCollapse() {
        // todo: move to anonymous class as there's no way to differentiate between the instances!
        Log.d(TAG, "onCollapse()");
    }

}

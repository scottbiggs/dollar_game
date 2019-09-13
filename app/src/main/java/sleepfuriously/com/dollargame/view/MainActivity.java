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
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import sleepfuriously.com.dollargame.R;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonEventListener;


/**
 * Attempting to implement the "Dollar Game" discussed by Dr. Holly Kreiger
 * on the Numberphile channel.
 * https://www.youtube.com/watch?v=U33dsEcKgeQ
 */
public class MainActivity extends AppCompatActivity {


    //------------------------
    //  constants
    //------------------------

    private static final String TAG = MainActivity.class.getSimpleName();


    /** The different game modes. These are the highest level of logic for the game. */
    enum Modes {
        BUILD_MODE, SOLVE_MODE
    }

    /** start in build mode */
    private static final Modes DEFAULT_MODE = Modes.BUILD_MODE;


    //------------------------
    //  widgets
    //------------------------

    /** The play are of the game */
    private FrameLayout mPlayArea;

    /** holds all the buttons */
    private List<NodeButton> mButtonList = new ArrayList<>(); // todo: make this work with GRaph class

    // todo: just for testing!
//    ToggleButton mTestToggle;

    /** This switch toggles between build and play mode */
    private Switch mMainSwitch;

    /** Textviews that spell out BUILD or SOLVE at the top of the screen */
    private TextView mBuildTv, mSolveTv;

    /** displays hints to keep the user going */
    private TextView mHintTv;


    //------------------------
    //  data
    //------------------------

    /** true = solve mode, false = build mode */
    private Modes mMode = DEFAULT_MODE;


    //------------------------
    //  methods
    //------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        // setup ui and widgets
        //

        // use my toolbar instead of default
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMainSwitch = findViewById(R.id.main_switch);
        mMainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setMode(buttonView.isChecked() ? Modes.SOLVE_MODE : Modes.BUILD_MODE);
            }
        });

        mBuildTv = findViewById(R.id.build_tv);
        mSolveTv = findViewById(R.id.solve_tv);

//        // todo: for testing!
//        mTestToggle = findViewById(R.id.test_toggle);
//        mTestToggle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                disableAllButtons(mTestToggle.isChecked());
//                moveModeAllButtons(mTestToggle.isChecked());
//            }
//        });

        mHintTv = findViewById(R.id.bottom_hint_tv);

        mPlayArea = findViewById(R.id.play_area_fl);
        mPlayArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // todo: check to see if this is over a button. If so, mark this button
                        break;

                    case MotionEvent.ACTION_UP:
                        // only build a new button if we're in build mode
                        if (mMode == Modes.BUILD_MODE) {
                            newButton(event.getX(), event.getY());
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // todo: draw lines if necessary
                        break;
                }
                return true;
            }
        });

        //
        // todo: load any data
        //

        //
        // execute logic
        //
        setMode(DEFAULT_MODE);  // todo: may need changing based on loaded data in the future

    }

    @Override
    protected void onResume() {
        super.onResume();

        // turn off status and navigation bar (top and bottom)
        fullScreenStickyImmersive();
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

    /** Disables or enables ALL buttons */
    private void disableAllButtons(boolean disable) {
        for (NodeButton button : mButtonList) {
            button.setDisabled(disable);
        }
    }

    /** mostly for testing */
    private void moveModeAllButtons(boolean enableMoveMode) {
        for (NodeButton button : mButtonList) {
            button.setMovable(enableMoveMode);
        }
    }

    /**
     * Performs the logical operations for the new mode.
     * Also makes sure that the appropriate UI changes take place.
     *
     * side effects:
     *      mModes      Changed to newMode
     *
     * @param newMode   The new mode
     */
    private void setMode(Modes newMode) {

        mMode = newMode;

        // now do the ui
        switch (mMode) {
            case BUILD_MODE:
                disableAllButtons(true);
                moveModeAllButtons(true);
                buildModeUI();
                break;

            case SOLVE_MODE:
                disableAllButtons(false);
                moveModeAllButtons(false);
                solveModeUI();
                break;

            default:
                // get their attention!
                throw new EnumConstantNotPresentException(Modes.class, "Unknown Mode in setMode()");
        }

    }

    /**
     * Does all the UI for changing to Build mode. No logic is done.
     */
    private void buildModeUI() {
        // Only change the switch if it IS checked (in Solve Mode)
        if (mMainSwitch.isChecked()) {
            mMainSwitch.setChecked(false);
        }

        mBuildTv.setTextColor(getResources().getColor(R.color.textcolor_on));
        mSolveTv.setTextColor(getResources().getColor(R.color.textcolor_ghosted));

        mHintTv.setText(R.string.build_hint);
    }

    /**
     * Does all the UI for Solve mode.
     */
    private void solveModeUI() {
        // Only change the switch if it NOT checked (in Build Mode)
        if (!mMainSwitch.isChecked()) {
            mMainSwitch.setChecked(true);
        }

        mBuildTv.setTextColor(getResources().getColor(R.color.textcolor_ghosted));
        mSolveTv.setTextColor(getResources().getColor(R.color.textcolor_on));

        mHintTv.setText(R.string.solve_hint);
    }


    /**
     * Adds a button to the given coords.
     */
    private NodeButton newButton(float x, float y) {

        NodeButton button = new NodeButton(this);
        button.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                Log.d(TAG, "click! index = " + index);
            }

            @Override
            public void onExpand() {
                Log.d(TAG, "expanding...");
            }

            @Override
            public void onCollapse() {
                Log.d(TAG, "...collapsing");
            }
        });

        button.setXYCenter(x, y);

        switch (mMode) {
            case BUILD_MODE:
                button.setDisabled(true);
                button.setMovable(true);
                break;

            case SOLVE_MODE:
                button.setDisabled(false);
                button.setMovable(false);
                break;
        }

        mPlayArea.addView(button);
        mButtonList.add(button);

        return button;
    }




}

package sleepfuriously.com.dollargame.view;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Typeface;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.animation.PathInterpolatorCompat;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import sleepfuriously.com.dollargame.R;
import sleepfuriously.com.dollargame.model.Graph;
import sleepfuriously.com.dollargame.model.GraphNodeDuplicateIdException;
import sleepfuriously.com.dollargame.model.GraphNotConnectedException;
import sleepfuriously.com.dollargame.model.MyCombinationGenerator;
import sleepfuriously.com.dollargame.model.TheNewRandomSum;
import sleepfuriously.com.dollargame.view.SubButtonsBtn.ButtonEventListener;
import sleepfuriously.com.dollargame.view.buttons.MovableNodeButton;
import sleepfuriously.com.dollargame.view.dialogs.NodeEditDialog;
import sleepfuriously.com.dollargame.view.dialogs.RandomizingDialog;


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

    /** An id to identify the PrefsActivity in onActivityResult() */
    public static final int PREFS_ACTIVITY_ID = 2;

    /** number of milliseconds for a take animation */
    private static final int TAKE_MILLIS = 300;

    //------------------------
    //  widgets
    //------------------------

    /** The play are of the game */
    private PlayAreaFrameLayout mPlayArea;

    /** holds all the buttons and their connections */
    private Graph mGraph = new Graph<MovableNodeButton>(false);


    /**
     * This switch toggles between build and solve mode. Solve is true (on);
     * false means build (off).
     */
    private Switch mMainSwitch;

    /** Textviews that spell out BUILD or SOLVE at the top of the screen */
    private TextView mBuildTv, mSolveTv;

    /** displays hints to keep the user going */
    private TextView mHintTv;

    /**
     * Displays the connectivity of the Graph while building.
     * Displays the solved state of the puzzle while solving.
     */
    private ImageView mConnectedIV;

    /** Textiviews that deal with the genus, which is only meaningful when the graph is connected. */
    private TextView mGenusLabelTv, mGenusTv;

    /** TextViews that display the current count of the nodes */
    private TextView mCountLabelTv, mCountTv;

    /** allows the user to quickly randomize all the nodes at once */
    private Button mRandomizeAllButt;

    /** this is the view that moves between the nodes indicating a give or take */
    private Drawable mGiveTakeDrawable;


    //------------------------
    //  data
    //------------------------

    /** Reflects the current mode of the app: build or solve */
    private boolean mBuildMode;

    /** true => in the process of connecting two nodes */
    private boolean mConnecting;

    /** The id of the starting node in a connection or when moving */
    private int mStartNodeId;

    /** Used to indicate that a give action is taking place */
    private boolean mGiving;

    /** Used to indicate that a take action is occurring */
    private boolean mTaking;

    /** the size of an animation dot in current screen coordinates */
    private int mDotDimensionWidth, mDotDimensionHeight;

    /** only TRUE during the give/take animation. UI events need to wait until this is FALSE */
    private boolean mAnimatingGiveTake = false;

    /** Used to determine if this Activity is alive or has been destroyed (for AsyncTasks) */
    private volatile boolean mIsAlive = false;

    //------------------------
    //  methods
    //------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIsAlive = true;

        // determines if this was setup
        if (isStartingFromUser()) {
            userInitiatedOnCreate();
        }

        else {
            // There's some data to be loaded and processed.
            processIntent();
        }

    }

    @Override
    protected void onDestroy() {
        mIsAlive = false;       // signal that this Activity is no longer active
        super.onDestroy();
    }

    /**
     * Initialize the main switch widget that controls build/solve mode.
     * Works by side-effect.
     */
    private void setupMainSwitch() {
        mMainSwitch = findViewById(R.id.main_switch);
        mMainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == !mBuildMode) {
                    Log.e(TAG, "onCheckedChanged() is trying to change the mode to what it already is!");
                }
                setMode(!isChecked);
            }
        });
    }

    /**
     * Initializes the widgets that control the count display.
     * Works by side-effect.
     */
    private void setupCountWidgets() {
        mCountLabelTv = findViewById(R.id.count_label_tv);
        mCountLabelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimpleDialog(R.string.count_dialog_title, R.string.count_dialog_msg);
            }
        });

        mCountTv = findViewById(R.id.count_tv);
        mCountTv.setText(R.string.not_applicable);
        mCountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimpleDialog(R.string.count_dialog_title, R.string.count_dialog_msg);
            }
        });
    }

    /**
     * Initializes the widgets that display genus info.
     * Works by side-effect.
     */
    private void setupGenusWidgets() {
        mGenusLabelTv = findViewById(R.id.genus_label_tv);
        mGenusLabelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimpleDialog(R.string.genus_dialog_title, R.string.genus_dialog_msg);
            }
        });

        mGenusTv = findViewById(R.id.genus_tv);
        mGenusTv.setText(R.string.not_applicable);
        mGenusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimpleDialog(R.string.genus_dialog_title, R.string.genus_dialog_msg);
            }
        });
    }

    /**
     * initializes the play area.
     * Works by side-effect.
     */
    private void setupPlayArea() {
        mPlayArea = findViewById(R.id.play_area_fl);

        mPlayArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent playAreaEvent) {

                if (!mBuildMode) {
                    // Solve mode, pass along the events
                    return false;
                }

                // If we're in the middle of a connection, handle all events here.
                if (mConnecting) {

                    if (playAreaEvent.getAction() == MotionEvent.ACTION_UP) {
                        // finish the UI action and reset to non-connecting state
                        mConnecting = false;

                        // reset the start button
                        MovableNodeButton startButton = (MovableNodeButton) mGraph.getNodeData(mStartNodeId);
                        startButton.setBackgroundColorResource(getButtonStateColor(startButton));
                        startButton.invalidate();

                        setAllButtonsBuild();   // allows buttons to be moved again
                        buildModeUI();
                    }
                }

                // build a new button
                else if (playAreaEvent.getAction() == MotionEvent.ACTION_UP) {
                    PointF touchLoc = new PointF(playAreaEvent.getX(), playAreaEvent.getY());
                    newButton(touchLoc);
                }
                return true;    // event consumed
            }
        });
    }

    /**
     * Initializes the widgets that show if the graph is connected or not.
     * Works by side-effect.
     */
    private void setupConnectedWidgets() {
        mConnectedIV = findViewById(R.id.connected_iv);
        mConnectedIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toastStr;

                if (mBuildMode) {
                    boolean connected = (boolean) mConnectedIV.getTag();
                    toastStr = connected ?
                            getString(R.string.connected_toast) :
                            getString(R.string.not_connected_toast);
                }
                else {
                    // in solve mode
                    toastStr = isSolved() ?
                            getString(R.string.solved_toast) :
                            getString(R.string.not_solved_toast);
                }

                Toast.makeText(MainActivity.this, toastStr, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Initializes the randomize button.  This puts dollar amounts in all the
     * currently displayed buttons appropriate to the difficulty setting.  If
     * the graph is not connected, will set the count to 0.
     * Works by side-effect.
     */
    private void setupRandomizeButton() {
        mRandomizeAllButt = findViewById(R.id.random_all_butt);
        mRandomizeAllButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomizeAllNodes();
            }
        });
    }

    /**
     * Initializes all the main widgets for the game.
     * Works by side-effects (method data).
     */
    private void setupWidgets() {
        setupMainSwitch();
        setupCountWidgets();
        setupGenusWidgets();

        mBuildTv = findViewById(R.id.build_tv);
        mSolveTv = findViewById(R.id.solve_tv);
        mHintTv = findViewById(R.id.bottom_hint_tv);

        setupPlayArea();
        setupConnectedWidgets();
        setupRandomizeButton();

        mGiveTakeDrawable = AppCompatResources.getDrawable(this, R.drawable.circle_black_solid_small);

        // Need to figure out the size of the animation dots for later
        Drawable drawable = getResources().getDrawable(R.drawable.circle_black_solid_small);
        mDotDimensionWidth = drawable.getIntrinsicWidth();
        mDotDimensionHeight = drawable.getIntrinsicHeight();

        // Not animating currently, we just started the program!
        mAnimatingGiveTake = false;
    }

    /**
     * onCreate() if user initiated the Activity.  Does all the initializations
     * for the user to start the program from scratch.
     */
    private void userInitiatedOnCreate() {

        mBuildMode = true;   // start in build mode
        mConnecting = false;

        setupToolbar();
        setupWidgets();

    }

    /**
     * This is the initialization portion of the program for when it has been
     * started via a notification.  Takes the user straight to the solve mode
     * and will have a puzzle laid out for him.
     */
    private void notificationInitiatedOnCreate() {

        mBuildMode = false;   // start in solve mode
        mConnecting = false;

        setupToolbar();
        setupWidgets();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // turn off status and navigation bar (top and bottom)
        fullScreenStickyImmersive();

        // handles refreshing UI
        refreshPrefs();


//        int[][] result =
//                MyCombinationGenerator.getAllCombinations2(2, Arrays.asList(1, 2, 3));
//        for (int[] array : result) {
//            Log.d(TAG, Arrays.toString(array));
//        }

//        CombinationMaker.test();

//        TheNewRandomSum.test();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isStartingFromUser()) {
            Log.d(TAG, "detected a user initiated start from onNewIntent()--that's weird! aborting.");
            return;
        }

        processIntent();
    }


    /**
     * This is overridden so that I can control when touch events are handled
     * and when they are all ignored.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mAnimatingGiveTake) {
            return true;    // consume all touch events during animations
        }

        return super.dispatchTouchEvent(ev);
    }


    private void setupToolbar() {
        // use my toolbar instead of default
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);    // shows back arrow
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
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

        if (mBuildMode) {
            inflater.inflate(R.menu.build_menu, menu);
        }
        else {
            inflater.inflate(R.menu.solve_menu, menu);
        }

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
                doAbout();
                Log.d(TAG, "menu option: about");
                break;

            case R.id.build_help:
                // todo
                Log.d(TAG, "menu option: help");
                break;

            case R.id.build_load:
                // todo
                Log.d(TAG, "menu option: load");
                break;

            case R.id.build_share:
                // todo
                Log.d(TAG, "menu option: share");
                break;

            case R.id.build_settings:
                doOptions();
                break;

            default:
                Toast.makeText(this, "unknown menu selection", Toast.LENGTH_LONG).show();
                break;
        }

        return true;   // end processing (consumed completely)
    }


    /**
     * Determines if this Activity was started by the user or by some other
     * notification (which implies that the Intent will have some important
     * data!).
     *
     * @return  True - user started this Activity. Use normal startup.
     *          False - Activity was started from a notification Intent.
     */
    private boolean isStartingFromUser() {
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();

        if (appLinkAction.equals(Intent.ACTION_MAIN)) {
            return true;
        }
        return false;
    }

    /**
     * Does the work of processing the Intent that started this Activity.
     * The Intent holds any info that was sent to this Activity.
     *
     * This is primarily called after it's determined that this Activity was
     * NOT started by the user (and by elimination was called by a notification
     * Intent).  All the necessary data will be grabbed from the Intent here.
     *
     * Called by {@link #onCreate(Bundle)} and {@link #onNewIntent(Intent)}.
     *
     * side effects:
     *  todo
     *
     */
    private void processIntent() {

        Intent appLinkIntent = getIntent();
        String rawDataStr = appLinkIntent.getDataString();
        String appLinkAction = appLinkIntent.getAction();


        Uri appLinkData = appLinkIntent.getData();
        if (appLinkData == null) {
            Log.e(TAG, "no data found in the Intent in processIntent()!");
            return;
        }

        String host = appLinkData.getHost();
        String scheme = appLinkData.getScheme();
        Set queryParmNames = appLinkData.getQueryParameterNames();

        String lastPathSegment = appLinkData.getLastPathSegment();  // data could be here!

        // todo: process data sent

//        TextView dataTv = findViewById(R.id.data_tv);
//        String finalStr = "host: " + host + "\n" +
//                "scheme: " + scheme + "\n" +
//                "last path segment: " + lastPathSegment + "\n" +
//                "names: " + queryParmNames.toString();
//        dataTv.setText(finalStr);
    }


    /**
     * Changes the mode of all the buttons (nodes) to Build mode (MOVABLE).
     */
    private void setAllButtonsBuild() {
        for (Object button : mGraph) {
            ((MovableNodeButton)button).setMode(MovableNodeButton.Modes.MOVABLE);
        }
    }

    /**
     * Changes the nodes to Solve mode (EXPANDABLE).
     */
    private void setAllButtonsSolve() {
        for (Object button : mGraph) {
            ((MovableNodeButton)button).setMode(MovableNodeButton.Modes.EXPANDABLE);
        }
    }

    /**
     * Changes all the nodes to NOT movable (CLICKS_ONLY) mode.  Used when
     * making a connection.
     */
    private void setAllButtonsConnecting() {
        for (Object button :mGraph) {
            ((MovableNodeButton)button).setMode(MovableNodeButton.Modes.CLICKS_ONLY);
        }
    }


    private void showSimpleDialog(int titleId, int msgId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setCancelable(false)
                .setTitle(titleId)
                .setMessage(msgId)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }


    /**
     * Performs the logical operations for the new mode.
     * Also makes sure that the appropriate UI changes take place.<br>
     * <br>
     * Note that this doesn't take the CLICK_ONLY mode of the buttons into account.  That
     * is considered to be a sub-mode of BUILD mode.<br>
     * <br>
     * side effects:<br>
     *      mModes      Changed to newMode
     *
     * @param buildMode     The new mode. True means Build mode; false is Solve.
     */
    private void setMode(boolean buildMode) {

        if (mBuildMode == buildMode) {
            Log.e(TAG, "setMode(), trying to change to same mode! (mMode = " + mBuildMode + " )");
            return;
        }
        mBuildMode = buildMode;

        // now do the ui
        if (mBuildMode) {
            setAllButtonsBuild();
            buildModeUI();
        }
        else {
            setAllButtonsSolve();
            solveModeUI();
        }
    }

    /**
     * Does all the UI for Solve mode.
     */
    private void solveModeUI() {
        Log.d(TAG, "solveModeUI()");

        // Only change the switch if it NOT checked (in Build Mode)
        if (!mMainSwitch.isChecked()) {
            mMainSwitch.setChecked(true);
        }

        mBuildTv.setTextColor(getResources().getColor(R.color.textcolor_ghosted));
        mSolveTv.setTextColor(getResources().getColor(R.color.textcolor_on));

        // convert this widget to display the solvable state.
        if (isSolved()) {
            mConnectedIV.setImageResource(R.drawable.ic_solved);
        }
        else {
            mConnectedIV.setImageResource(R.drawable.ic_unsolved);
        }

        mHintTv.setText(R.string.solve_hint);

        mRandomizeAllButt.setVisibility(View.GONE);
    }

    /**
     * Does all the UI for changing to Build mode. No logic is done.
     */
    private void buildModeUI() {
        Log.d(TAG, "buildmodeUI()");

        // Only change the switch if it IS checked (in Solve Mode)
        if (mMainSwitch.isChecked()) {
            mMainSwitch.setChecked(false);
        }

        mBuildTv.setTextColor(getResources().getColor(R.color.textcolor_on));
        mSolveTv.setTextColor(getResources().getColor(R.color.textcolor_ghosted));

        if (mGraph.isConnected()) {
            mConnectedIV.setImageResource(R.drawable.ic_connected);
        }
        else {
            mConnectedIV.setImageResource(R.drawable.ic_not_connected);
        }

        mHintTv.setText(R.string.build_hint);

        mRandomizeAllButt.setVisibility(View.VISIBLE);
        mRandomizeAllButt.setEnabled(true);
    }

    /**
     * Does the UI for making a connection.
     * Should ONLY be used while in Build mode.
     */
    private void connectUI() {
        Log.d(TAG, "connectUI()");
        mHintTv.setText(R.string.connect_hint);

        mRandomizeAllButt.setEnabled(false);
    }


    /**
     * Does all the genus UI.  If the graph is NOT connected, the genus doesn't make sense,
     * so the UI will reflect it.
     *
     * preconditions
     *      - genus widgets are initialized
     *      - mGraph contains all the correct info about the graph
     */
    private void setGenusUI() {
        try {
            int genus = mGraph.getGenus();
            mGenusTv.setText(String.valueOf(genus));
        }
        catch (GraphNotConnectedException e) {
            // This is not really an error, just a convenient way to see that the
            // graph is not connected.
            mGenusTv.setText(R.string.not_applicable);
        }
    }

    private void hideGenusUI() {
        mGenusTv.setVisibility(View.GONE);
        mGenusLabelTv.setVisibility(View.GONE);
    }

    private void showGenusUI() {
        mGenusTv.setVisibility(View.VISIBLE);
        mGenusLabelTv.setVisibility(View.VISIBLE);
    }


    /**
     * Does all the UI for displaying the current count.  The count is simply the
     * sum of all the dollar amounts in all the nodes.  This is displayed whether
     * or not the graph is connected.
     *
     * If there are no nodes, then the count doesn't make sense and "not applicable"
     * will display.
     */
    private void setCountUI() {
        if (mGraph.numNodes() == 0) {
            mCountTv.setText(R.string.not_applicable);
        }
        else {
            int count = 0;
            for (int i = 0; i < mGraph.numNodes(); i++) {
                MovableNodeButton node = (MovableNodeButton) mGraph.getNodeData(i);
                if (node != null) {
                    count += node.getAmount();
                }
            }
            mCountTv.setText(String.valueOf(count));
        }
    }

    private void hideCountUI() {
        mCountTv.setVisibility(View.GONE);
        mCountLabelTv.setVisibility(View.GONE);
    }

    private void showCountUI() {
        mCountTv.setVisibility(View.VISIBLE);
        mCountLabelTv.setVisibility(View.VISIBLE);
    }


    /**
     * Adds a button to the given coords.  Should only be called
     * when in Build mode.
     *
     * side effects:
     *  mGraph      Will have this button added to it
     *
     *  UI          Will have a new button drawn at the given location
     *
     * @param   relativeToParentLoc The location to center the button around.  This'll probably
     *                              be where the user touched the screen.
     *                              NOTE: this uses RELATIVE COORDINATES (to the parent)!
     */
    private void newButton(PointF relativeToParentLoc) {

        final MovableNodeButton button = new MovableNodeButton(this);
        button.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT));

        final int buttonId = mGraph.getUniqueNodeId();
        button.setId(buttonId);

        button.setXYCenter(relativeToParentLoc.x, relativeToParentLoc.y);
        button.setBackgroundColorResource(R.color.button_bg_color_build_disconnected);

        button.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onPopupButtonClicked(int index) {
                switch (index) {
                    case 1:
                        // indicate that a take was chosen.  Don't actually do anything
                        // until the animation is complete.
                        mTaking = true;
                        break;

                    case 2:
                        mGiving = true;
                        break;
                }
            }

            @Override
            public void onExpand() {
                resetPopupButtons();
            }

            @Override
            public void onCollapse() {
            }

            @Override
            public void onCollapseFinished() {
                if (mTaking || mGiving) {
                    startGiveTake(buttonId, button);
                }
            }
        });

        button.setOnMoveListener(new MovableNodeButton.OnMoveListener() {
            @Override
            public void movingTo(float diffX, float diffY) {
                continueMove(button, diffX, diffY);
//                Log.d(TAG, "moving to " + diffX + ", " + diffY);
            }

            @Override
            public void moveEnded(float diffX, float diffY) {
                continueMove(button, diffX, diffY);
//                Log.d(TAG, "move ended");
            }

            @Override
            public void clicked() {
                if (mConnecting) {
                    // process the connection, remembering that this is the 2nd button
                    // of the connection and you can't connect to yourself.
                    int endId = button.getId();

                    if (endId == mStartNodeId) {
                        // Can't connect to yourself!
                        Log.d(TAG, "clicking on yourself");
                        button.setBackgroundColorResource(getButtonStateColor(button));
                        button.invalidate();
                    }
                    else {
                        if (mGraph.isAdjacent(mStartNodeId, endId)) {
                            // remove this connection
                            disconnectButtons(mStartNodeId, endId);
                        }
                        else {
                            // add this connection
                            connectButtons(mStartNodeId, endId);
                        }
                    }
                    mConnecting = false;
                    setAllButtonsBuild();
                    buildModeUI();
                }
                else {
                    // Only start a connection if there's a button to connect to!
                    if (mGraph.numNodes() > 1) {
                        startConnection(button);
                    }
                }
            }

            @Override
            public void longClicked() {
                showMoneyDialog(button);
            }
        });

        mPlayArea.addView(button);

        try {
            //noinspection unchecked
            mGraph.addNode(buttonId, button);
        }
        catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        resetConnectedUI();
    }


    private void resetPopupButtons() {
        mGiving = false;
        mTaking = false;
    }


    /**
     * Begins the animation of a give or take.  This involves considerable
     * setup.  Once the animation is complete {@link #giveTakeAnimFinished(List, MovableNodeButton)}
     * is called via a listener.
     *
     * preconditions:
     *  mGiving and mTaking should be properly set.
     *
     * @param mainButtId    The graph id of the button doing the taking
     *
     * @param mainButton    The button that we're sending the money to
     *                      (the button that's taking the money).
     */
    private void startGiveTake(int mainButtId, final MovableNodeButton mainButton) {

        // sanity check
        if ((mGiving == false) && (mTaking == false)) {
            Log.e(TAG, "no action to do in startGiveTake()");
            return;
        }

        // set a flag to prevent any UI events during this animation
        mAnimatingGiveTake = true;

        float xAdjust = 0, yAdjust = 0;

        //noinspection unchecked
        List<Integer> adjacentList = mGraph.getAllAdjacentTo(mainButtId);

        // create the little moving dots that will traverse the edges
        final List<ImageView> dots = new ArrayList<>();
        for (int adjacentId: adjacentList) {
            ImageView newDot = new ImageView(this);
            newDot.setLayoutParams(new LinearLayout.LayoutParams(
                   LinearLayout.LayoutParams.WRAP_CONTENT,
                   LinearLayout.LayoutParams.WRAP_CONTENT));

            newDot.setImageDrawable(mGiveTakeDrawable);

            MovableNodeButton adjacentButt = (MovableNodeButton) mGraph.getNodeData(adjacentId);
            newDot.setTag(adjacentButt);  // we'll need this data later

            // set the initial location of this dot on the main button or the adjacent button
            // depending on whether this is a give or a take.  The locations of the dots must
            // take the size of the dot into account.
            xAdjust = ((float) mDotDimensionWidth) / 2f;
            yAdjust = ((float) mDotDimensionHeight) / 2f;


            if (mTaking) {
                newDot.setX(adjacentButt.getCenterX() - xAdjust);
                newDot.setY(adjacentButt.getCenterY() - yAdjust);
            }
            else {
                newDot.setX(mainButton.getCenterX() - xAdjust);
                newDot.setY(mainButton.getCenterY() - yAdjust);
            }


            dots.add(newDot);
            mPlayArea.addView(newDot);
        }

        // Animate the dots
        for (int i = 0; i < dots.size(); i++) {
            ImageView dot = dots.get(i);
            MovableNodeButton adjacentButt = (MovableNodeButton) dot.getTag();

            ViewPropertyAnimator animator = dot.animate();
            animator.setDuration(TAKE_MILLIS);

            // custom interpolator. Tool at https://matthewlein.com/tools/ceaser
            Interpolator interpolator = PathInterpolatorCompat.create(0.485f, 0.005f, 0.085f, 1f);
            animator.setInterpolator(interpolator);

            if (mTaking) {
                animator.translationX(mainButton.getCenterX() - xAdjust)
                        .translationY(mainButton.getCenterY() - yAdjust);
            }
            else {
                animator.translationX(adjacentButt.getCenterX() - xAdjust)
                        .translationY(adjacentButt.getCenterY() - yAdjust);
            }

            if (i + 1 == dots.size()) {
                // if this is the last one, set a listener
                // to fire when the animations ends.
                animator
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) { }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                giveTakeAnimFinished(dots, mainButton);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) { }

                            @Override
                            public void onAnimationRepeat(Animator animation) { }
                        });
            }
        }

    }


    /**
     * Called once a give or take animation is complete.  This finishes
     * the UI and the logic of a give or a take.<br>
     *<br>
     * preconditions:<br>
     *  mGiving and mTaking are properly set (should have been checked at
     *  a higher level).
     *
     * @param animViews List of all the dot Views that were animating
     *
     * @param mainButton    The button that is the center of the animation.
     */
    private void giveTakeAnimFinished(List<ImageView> animViews,
                                      MovableNodeButton mainButton) {

        for (ImageView v : animViews) {
            // update the dollar amount connected to this view
            MovableNodeButton button = (MovableNodeButton) v.getTag();
            if (mGiving) {
                button.incrementAmount();
            }
            else {
                button.decrementAmount();
            }

            // remove this view from the play area
            mPlayArea.removeView(v);
        }

        // update the main button
        int currAmount = mainButton.getAmount();
        int changeAmount = animViews.size();
        if (mGiving) {
            mainButton.setAmount(currAmount - changeAmount);
        }
        else {
            mainButton.setAmount(currAmount + changeAmount);
        }

        // re-check solved state
        if (isSolved()) {
            mConnectedIV.setImageResource(R.drawable.ic_solved);
        }
        else {
            mConnectedIV.setImageResource(R.drawable.ic_unsolved);
        }

        mAnimatingGiveTake = false; // no longer animating

    }

    /**
     * Checks the current state of mGraph and determines if we're in a solved
     * state or not.  If any node has less than 0 dollars, then the puzzle
     * is not solved.
     *
     * preconditions:
     *      mGraph      Ready for inspection
     */
    private boolean isSolved() {

        for (int i = 0; i < mGraph.numNodes(); i++) {
            MovableNodeButton node = (MovableNodeButton) mGraph.getNodeData(i);
            if (node.getAmount() < 0) {
                return false;
            }
        }
        return true;
    }


    /**
     * Throws up a dialog that allows the user to edit the money amount within a node.
     *
     * @param button    The node/button in question.
     */
    private void showMoneyDialog(final MovableNodeButton button) {

        NodeEditDialog dialog = new NodeEditDialog();
        dialog.setOnNodeEditDialogDoneListener(new NodeEditDialog.OnNodeEditDialogDoneListener() {
            @Override
            public void result(boolean cancelled, int dollarAmount, boolean delete) {
                if (cancelled) {
                    return; // do nothing
                }

                if (delete) {
                    deleteNode(button);
                    return;
                }

                // set the button to the dollar amount.
                button.setAmount(dollarAmount);
            }
        });
        dialog.show(this, button.getAmount());
    }


    private void deleteNode(MovableNodeButton nodeToDelete) {

        @SuppressWarnings("unchecked")
        int nodeId = mGraph.getNodeId(nodeToDelete);

        mPlayArea.removeView(nodeToDelete);

        // remove the edges associated with this node, and then the node
        mGraph.removeEdgesWithNode(nodeId);
        mGraph.removeNode(nodeId);

        resetAllButtonStateColors();

        rebuildPlayAreaLines();
        resetConnectedUI();
    }

    /**
     * Call this to execute the logic and the UI of a node button being moved.
     * This won't move the button (which can take care of itself), but will do
     * everything else.
     *
     * @param button    The node that is moved.
     *
     * @param diffX     The delta between the previous coords and the new coords.
     *                  In other words, the x-axis will move this many pixels.
     *
     * @param diffY     Similar for y-axis
     */
    private void continueMove(MovableNodeButton button, float diffX, float diffY) {
        // todo: this would be more efficient--make updateLines() method work
//        PointF diffPoint = new PointF(diffX, diffY);
//        mPlayArea.updateLines(button.getCenter(), diffPoint);

        rebuildPlayAreaLines();
        mPlayArea.invalidate();
    }

    /**
     * Helper method that clears all the lines from the play area and reconstructs
     * them according to mGraph.
     */
    private void rebuildPlayAreaLines() {
        mPlayArea.removeAllLines();

        for (int i = 0; i < mGraph.numEdges(); i++) {
            Graph.Edge edge = mGraph.getEdge(i);

            MovableNodeButton startbutton = (MovableNodeButton) mGraph.getNodeData(edge.startNodeId);
            PointF startp = startbutton.getCenter();

            MovableNodeButton endButton = (MovableNodeButton) mGraph.getNodeData(edge.endNodeId);
            PointF endp = endButton.getCenter();

            mPlayArea.addLine(startp, endp);
        }
    }

    /**
     * Call this to initiate a node connection.
     *
     * @param button    The button that starts the connection.  This will
     *                  be highlighted.
     */
    private void startConnection(MovableNodeButton button) {

        mConnecting = true;
        mStartNodeId = button.getId();

        setAllButtonsConnecting();
        connectUI();

        button.setBackgroundColorResource(R.color.button_bg_color_build_connect);
        button.invalidate();
    }


    /**
     * Does the logic and graphics of removing the connection between two buttons.
     *
     * @param startButtonId     Id of the start button. This is the button that the
     *                          user first selected when making the dis-connection
     *                          and thus is currently highlighted.
     *
     * @param endButtonId   The id of the other button.
     */
    private void disconnectButtons(int startButtonId, int endButtonId) {

        Log.d(TAG, "disconnectButtons:  start = " + startButtonId + ", end = " + endButtonId);

        // cannot disconnect yourself!
        if (startButtonId == endButtonId) {
            Log.v(TAG, "Attempting to disconnect a button to itself--aborted.");
            return;
        }

        // check to make sure these buttons actually ARE connected
        if (!mGraph.isAdjacent(startButtonId, endButtonId)) {
            Log.e(TAG, "attempting to disconnected two nodes that are not connected! Aborting!");
            return;
        }

//        setAllButtonsBuild();   // this can only happen during the build mode

        MovableNodeButton startButton = (MovableNodeButton) mGraph.getNodeData(startButtonId);
        MovableNodeButton endButton = (MovableNodeButton) mGraph.getNodeData(endButtonId);

        PointF start = startButton.getCenter();
        PointF end = endButton.getCenter();

        // remove from graph and play area
        mGraph.removeEdge(startButtonId, endButtonId);
        mPlayArea.removeLine(start, end);
        mPlayArea.invalidate();

        startButton.setBackgroundColorResource(getButtonStateColor(startButton));
        endButton.setBackgroundColorResource(getButtonStateColor(endButton));

        startButton.setMode(MovableNodeButton.Modes.MOVABLE);
        endButton.setMode(MovableNodeButton.Modes.MOVABLE);

        startButton.invalidate();
        endButton.invalidate();

        resetConnectedUI();
    }

    /**
     * Does the logic and graphics of connecting two buttons.  Presumes that they are NOT
     * already connected.  Use {@link Graph#isAdjacent(int, int)} to determine if
     * the two nodes/buttons are already connected.
     *
     * side effects:
     *      mGraph      Will reflect the new connection
     *
     * @param startButtonId     The beginning button (node). Should be highlighted.
     *
     * @param endButtonId       Destination button (node)
     */
    private void connectButtons(int startButtonId, int endButtonId) {

        Log.d(TAG, "connectButtons:  start = " + startButtonId + ", end = " + endButtonId);

        // cannot connect to yourself!
        if (startButtonId == endButtonId) {
            Log.v(TAG, "Attempting to connect a button to itself--aborted.");
            return;
        }

        // check to make sure these buttons aren't already connected
        if (mGraph.isAdjacent(startButtonId, endButtonId)) {
            Log.e(TAG, "Attempting to connect nodes that are already connected! Aborting!");
            return;
        }

        setAllButtonsBuild();   // this can only happen during the build mode

        MovableNodeButton startButton = (MovableNodeButton) mGraph.getNodeData(startButtonId);
        MovableNodeButton endButton = (MovableNodeButton) mGraph.getNodeData(endButtonId);

        PointF start = startButton.getCenter();
        PointF end = endButton.getCenter();

        // add this new line to the graph and the play area
        mGraph.addEdge(startButtonId, endButtonId);
        mPlayArea.addLine(start, end);
        mPlayArea.invalidate();

        startButton.setBackgroundColorResource(getButtonStateColor(startButton));
        endButton.setBackgroundColorResource(getButtonStateColor(endButton));

        startButton.invalidate();
        endButton.invalidate();

        resetConnectedUI();
    }


    /**
     * Goes through each node/button and checks it's state, making sure
     * that it is displaying the correct color.
     */
    private void resetAllButtonStateColors() {

        @SuppressWarnings("unchecked")
        List<Integer> nodeList = mGraph.getAllNodeIds();

        for (int nodeId : nodeList) {
            MovableNodeButton nodeButton = (MovableNodeButton) mGraph.getNodeData(nodeId);
            int color = getButtonStateColor(nodeButton);
            nodeButton.setBackgroundColorResource(color);
            nodeButton.invalidate();
        }
    }


    /**
     * Figures out the appropriate color for this button based on its current state.
     */
    private int getButtonStateColor(MovableNodeButton button) {

        // if the node is connected to any other node, then use the connected color
        List<Integer> connectedNodes = mGraph.getAllAdjacentTo(button.getId());
        if (connectedNodes.size() > 0) {
//            Log.d(TAG, "getButtonStateColor(), returning CONNECTED");
            return R.color.button_bg_color_build_connected;
        }
        else {
//            Log.d(TAG, "getButtonStateColor(), returning DISconnected");
            return R.color.button_bg_color_build_disconnected;
        }
    }

    /**
     * Sets the drawing in the connection ImageView according to the current
     * state of the graph.
     *
     * side effects:
     *  mConnectedIV    may have its source image changed
     *
     *  mMainSwitch     Will be enabled/disabled depending on the state of the Graph
     */
    private void resetConnectedUI() {
        if (mGraph.isConnected()) {
            mConnectedIV.setImageResource(R.drawable.ic_connected);
            mConnectedIV.setTag(true);  // indicates that it is displaying a connected graphic
            mMainSwitch.setEnabled(true);
        }
        else {
            mConnectedIV.setImageResource(R.drawable.ic_not_connected);
            mConnectedIV.setTag(false); // displaying not connected graphic
            mMainSwitch.setEnabled(false);
        }
        mConnectedIV.invalidate();

        setCountUI();
        setGenusUI();

        mRandomizeAllButt.setVisibility(View.VISIBLE);
        mRandomizeAllButt.setEnabled(true);
    }


    /**
     * Does the logic and UI of randomizing the contents of all the nodes.
     */
    private void randomizeAllNodes() {

        RandomizeAsyncTask asyncTask = new RandomizeAsyncTask();
        asyncTask.execute();
    }


    /**
     * Checks the shared prefs to find the current difficulty setting.
     *
     * @return  An int representing how many dollars should be adjusted
     *          above (or below if negative) the genus according to the
     *          current user's preference.
     */
    private int getCurrentDifficulty() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String diffKey = getString(R.string.pref_gameplay_difficulty_key);

        String diffVal = prefs.getString(diffKey, null);
        if (diffVal == null) {
            Log.e(TAG, "could not find difficulty key in getCurrentDifficulty()!");
            return 0;
        }

        int retVal = 0;
        switch (diffVal) {
            case "1":     // very easy
                retVal = 2;
                break;

            case "2":     // easy
                retVal = 1;
                break;

            case "3":     // challenging
                retVal = 0;
                break;

            case "4":     // not always possible
                retVal = -1;
                break;

            default:
                Log.e(TAG, "unable to figure out diffVal in getCurrentDifficulty()!");
        }

        return retVal;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PREFS_ACTIVITY_ID:
                // refresh prefs
                refreshPrefs();
                break;

            default:
                Log.e(TAG, "unknown requestCode of " + requestCode + " in onActivityResult()");
                break;
        }
    }

    /**
     * Simple dialog that gives basic info about this app.
     */
    private void doAbout() {
        Intent itt = new Intent(this, AboutActivity.class);
        startActivity(itt);
    }


    /**
     * Throws up the options dialog and all that entails.
     */
    private void doOptions() {

        // Using an Activity for prefs because I'm lazy and haven't
        // setup the UI for Fragments. uh yeah.
        Intent itt = new Intent(this, PrefsActivity.class);
        startActivityForResult(itt, PREFS_ACTIVITY_ID);
    }

    /**
     * Updates variables and resets UI according to a fresh loading of the preferences.
     *
     * preconditions:
     *      - All the preference globals are initialized.
     *      - All UI elements that can change via prefs are ready to roll.
     *
     * side effects:
     *      mHintTV - visibility changes
     */
    private void refreshPrefs() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // hints
        boolean showHint = prefs.getBoolean(getResources().getString(R.string.pref_hints_cb_key), true);
        mHintTv.setVisibility(showHint ? View.VISIBLE : View.GONE);

        // difficulty
//        mDifficulty = prefs.getInt(getResources().getString(R.string.pref_gameplay_difficulty_key), 1);

        // construct the UI according to the current mode
        if (mBuildMode) {
            buildModeUI();
        }
        else {
            solveModeUI();
        }
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Use this to get all the random numbers for the dollar amounts.
     * Also handles the UI as well.
     *
     * preconditions:
     *  mGraph      Needs to hold the correct graph that is displaying
     *
     * side-effects:
     */
    class RandomizeAsyncTask extends AsyncTask<Void, Void, Void> {

        RandomizingDialog dialog = null;

        String toastStr = null;

        /** list of all the node IDs that we are randomizing */
        List<Integer> nodeIds;

        /** The final list of dollar amounts */
//        List<Integer> comboList;

        /** Final array to hold the dollar amount */
        int[] comboArray;

        //-------------------------------------

        private synchronized void doPreExecute() {

            dialog = new RandomizingDialog();
            dialog.show(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            doPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            // first, get all the nodes and find out how many there are
            //noinspection unchecked
            nodeIds = mGraph.getAllNodeIds();
            int numNodes = nodeIds.size();

            // now get the maximum and minimum values for each node
            int ceiling = getResources().getInteger(R.integer.MAX_DOLLAR_AMOUNT);
            int floor = getResources().getInteger(R.integer.MIN_DOLLAR_AMOUNT);

            // use the settings to figure out what the sum of all the nodes'
            // dollar amount should be.
            int targetSum = getCurrentDifficulty();
            try {
                targetSum += mGraph.getGenus();
            }
            catch (GraphNotConnectedException e) {
                Log.v(TAG, "Randomizing nodes before graph is connected. No big deal.");
            }

            // THIS IS IT!!!
            comboArray = TheNewRandomSum.getRandomSum(numNodes, targetSum, floor, ceiling);



            // here's the big calculation, find all the possible combinations
//            List<List<Integer>> summedCombos = MyCombinationGenerator.getSums(numNodes, targetSum, floor, ceiling);
//        Log.d(TAG, "summedCombos = " + summedCombos.toString());

            // Check to see if no possible summation exists.  This shouldn't be possible, but
            // it doesn't hurt to check.
//            if (summedCombos.size() < 1) {
//                toastStr = getString(R.string.impossible_randomize_settings);
//                return null;
//            }
//
//            // pick a random list from summedCombos (it's a list of lists)
//            Random rand = new Random();
//            comboList = summedCombos.get(rand.nextInt(summedCombos.size()));

            // sanity check: the comboList and the nodeIds list should be the same size
//            if (comboList.size() != nodeIds.size()) {
//                Log.e(TAG, "error! comboList.size() = " + comboList.size() +
//                        " whereas nodeIds.size() = " + nodeIds.size());
//                return null;
//            }

            // randomize the list (since each item in the list appears in a certain
            // order)
//            Collections.shuffle(comboList);
            return null;
        }


        private synchronized void doPostExecute() {
            if (!mIsAlive) {
                return;     // Activity has died, abort.
            }

            // go through all the nodes and assign them to the dollar amounts from
            // our list.
            for (int i = 0; i < nodeIds.size(); i++) {
                int nodeId = nodeIds.get(i);
                MovableNodeButton node = (MovableNodeButton) mGraph.getNodeData(nodeId);
                node.setAmount(comboArray[i]);
            }

            dialog.dismiss();
            dialog = null;

            setGenusUI();
            setCountUI();

            if (toastStr != null) {
                // display error message
                Toast.makeText(MainActivity.this, toastStr, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            doPostExecute();
            super.onPostExecute(aVoid);
        }

    } // class RandomizeAsyncTask

}

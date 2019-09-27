package sleepfuriously.com.dollargame.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.PointF;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;


import sleepfuriously.com.dollargame.R;
import sleepfuriously.com.dollargame.model.Graph;
import sleepfuriously.com.dollargame.model.GraphNodeDuplicateIdException;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonEventListener;
import sleepfuriously.com.dollargame.view.NodeButton.Modes;


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


    /** start in build mode */
    private static final NodeButton.Modes DEFAULT_MODE = NodeButton.Modes.BUILD_MODE;

    /**
     * Min and max times allowed for a proper double click.
     * The 2nd click must be within this window.
     */
    private static final long
            DOUBLE_CLICK_MIN_TIME = 25,
            DOUBLE_CLICK_MAX_TIME = 750;


    //------------------------
    //  widgets
    //------------------------

    /** The play are of the game */
    private PlayAreaFrameLayout mPlayArea;

    /** holds all the buttons and their connections */
    private Graph mGraph = new Graph<NodeButton>(false);

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

    /** true => in the process of connecting two nodes */
    private boolean mConnecting = false;

    /** The id of the starting node in a connection */
    private int mStartNodeId;

    /** System time of the last known disabled click. Used to determine double clicks. */
    private long mDoubleClickStart = 0L;

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

        // todo: for testing!
//        mTestToggle = findViewById(R.id.test_toggle);
//        mTestToggle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                setAllButtonModes(mTestToggle.isChecked());
////                moveModeAllButtons(mTestToggle.isChecked());
//                mPlayArea.setDrawLines(!mPlayArea.getDrawLines());
//                mPlayArea.invalidate();
//            }
//        });

        mHintTv = findViewById(R.id.bottom_hint_tv);

        mPlayArea = findViewById(R.id.play_area_fl);
        mPlayArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // only build a new button if we're in build mode and not currently connecting
                if ((event.getAction() == MotionEvent.ACTION_UP) &&
                    (mMode == Modes.BUILD_MODE) &&
                    (!mConnecting)) {
                        newButton(event.getX(), event.getY());
                    }
                return true;    // event consumed
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
                // todo
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

            case R.id.build_solve:
                // todo
                Log.d(TAG, "menu option: solve");
                break;

            default:
                Toast.makeText(this, "unknown menu selection", Toast.LENGTH_LONG).show();
                break;
        }

        return true;   // end processing (consumed completely)
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
        for (Object button : mGraph) {
            ((NodeButton)button).setMode(mMode);
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
    @SuppressWarnings("UnusedReturnValue")
    private NodeButton newButton(float x, float y) {

        final NodeButton button = new NodeButton(this);
        button.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT));

        final int graphId = mGraph.getUniqueNodeId();

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

            // A connection click
            @Override
            public void onDisabledClick(int clickedId) {

                long millisSinceLastClick = System.currentTimeMillis() - mDoubleClickStart;
                Log.d(TAG, "onDisabledClick(), millis = " + millisSinceLastClick);

                if ((millisSinceLastClick <= DOUBLE_CLICK_MAX_TIME) &&
                    (millisSinceLastClick >= DOUBLE_CLICK_MIN_TIME) &&
                    (graphId == clickedId)) {
                    // reset click settings and start the dialog
                    Log.d(TAG, "double click!");
                    mConnecting = false;
                    mDoubleClickStart = 0L;
                    startDollarAmountDialog(button);
                    button.setHighlighted(false);
                    button.invalidate();
                }

                else if (mConnecting) {
                    Log.d(TAG, "...attempting to finish a connection");
                    // completing a connection
                    connectButtons(mStartNodeId, graphId);

                    // reset to prevent double clicks
                    mConnecting = false;
                    mDoubleClickStart = 0L;
                }

                else {
                    // beginning a connection
                    Log.d(TAG, "beginning a connection...");
                    mConnecting = true;
                    mStartNodeId = graphId;
                    button.setHighlighted(true);
                    button.invalidate(); // todo: redundant--setHighlighted calls invalidate()
                    mDoubleClickStart = System.currentTimeMillis(); // for detecting double clicks
                }
            }

            @Override
            public void onMoved(PointF oldLoc, PointF newLoc) {
//                mPlayArea.updateLines(oldLoc, newLoc);
                // remove and rebuild the line data
                mPlayArea.removeAllLines();
                for (int i = 0; i < mGraph.numEdges(); i++) {
                    Graph.Edge edge = mGraph.getEdge(i);
                    NodeButton startNode = (NodeButton) mGraph.getNodeData(edge.startNodeId);
                    NodeButton endNode = (NodeButton) mGraph.getNodeData(edge.endNodeId);

                    PointF startp = startNode.getCenter();
                    PointF endp = endNode.getCenter();
                    mPlayArea.addLine(startp, endp);
                }
                mPlayArea.invalidate();
            }
        });

        button.setXYCenter(x, y);
        button.setId(graphId);  // essential to distinguish this button

        button.setMode(mMode);
        mPlayArea.addView(button);

        try {
            //noinspection unchecked
            mGraph.addNode(graphId, button);
        }
        catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        return button;
    }

    /**
     * Throws up a dialog for the user to enter a dollar amount for a node.
     */
    private void startDollarAmountDialog(final NodeButton button) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dollar_dialog_title);
        builder.setMessage(R.string.dollar_dialog_msg);

        // add an EditText to the dialog for input
        final EditText et = new EditText(this);
        builder.setView(et);

        builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Change the displayed amount in the button
                String str = et.getText().toString();
                button.setText(str);
            }
        });
    }


    /**
     * Does the logic and graphics of connecting two buttons.
     *
     * NOTE: Trying to make a connection that already exists will REMOVE
     * that connection.
     *
     * side effects:
     *      mGraph      Will reflect the new connection
     *
     * @param startButtonId     The beginning button (node)
     *
     * @param endButtonId       Desitnation button (node)
     */
    private void connectButtons(int startButtonId, int endButtonId) {
        Log.d(TAG, "connectButtons:  start = " + startButtonId + ", end = " + endButtonId);

        NodeButton startButton = (NodeButton) mGraph.getNodeData(startButtonId);
        NodeButton endButton = (NodeButton) mGraph.getNodeData(endButtonId);

        startButton.setHighlighted(false);

        // cannot connect to yourself!
        if (startButtonId == endButtonId) {
            Log.v(TAG, "Attempting to connect a button to itself--aborted.");
            startButton.setHighlighted(false);
            return;
        }

        // Draw locations for the two buttons
        PointF start = startButton.getCenter();
        PointF end = endButton.getCenter();

        // Adding or removing a connection?
        if (mGraph.isAdjacent(startButtonId, endButtonId)) {
            // remove the connection
            mGraph.removeEdge(startButtonId, endButtonId);
            mPlayArea.removeLine(start, end);
        }
        else {
            mGraph.addEdge(startButtonId, endButtonId);
            mPlayArea.addLine(start, end);
        }

        // tell the playarea to redraw
        mPlayArea.invalidate();
    }


}

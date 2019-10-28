package sleepfuriously.com.dollargame.view;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
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


import sleepfuriously.com.dollargame.R;
import sleepfuriously.com.dollargame.model.Graph;
import sleepfuriously.com.dollargame.model.GraphNodeDuplicateIdException;
//import sleepfuriously.com.dollargame.view.NodeButton.Modes;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonEventListener;


/**
 * Attempting to implement the "Dollar Game" discussed by Dr. Holly Kreiger
 * on the Numberphile channel.
 * https://www.youtube.com/watch?v=U33dsEcKgeQ
 */
public class MainActivity extends AppCompatActivity {

    //============================================
    // general todo & other ideas
    //      have a button to randomly assign numbers to the buttsons
    //      have an icon for if the graph is connected or not
    //
    //============================================

    //------------------------
    //  constants
    //------------------------

    private static final String TAG = MainActivity.class.getSimpleName();

    /** start in build (raw) mode */
    private static final DumbNodeButton.DumbNodeButtonModes DEFAULT_MODE
            = DumbNodeButton.DumbNodeButtonModes.RAW;

    /** Number of milliseconds between a click and a move. */
    private static final long CLICK_MILLIS_THRESHOLD = 100L;

    /** The amount of pixels a finger can slip around and still be considered a click and not a move */
    private static final float CLICK_SLOP = 5f;

    //------------------------
    //  widgets
    //------------------------

    /** The play are of the game */
    private PlayAreaFrameLayout mPlayArea;

    /** holds all the buttons and their connections */
    private Graph mGraph = new Graph<DumbNodeButton>(false);

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

    /** current mode (raw = build mode, button = solve) */
    private DumbNodeButton.DumbNodeButtonModes mMode = DEFAULT_MODE;

    /** true => in the process of connecting two nodes */
    private boolean mConnecting = false;

    /** The id of the starting node in a connection or when moving */
    private int mStartNodeId;

    /** true means that we're in the process of moving a button */
    private boolean mMoving = false;

    /**
     * The previous location of a moving node in coords relative to play area.
     * Only valid when mMoving = true.
     */
    private PointF mMovingLastPos;
    private PointF mMovingLastPlayAreaPos;

    /**
     * The original location of the button when a move is started.
     * In play area coords.
     */
    private PointF mMovingStartPos;
    private PointF mMovingStartPlayAreaPos;

    /** the timestamp of when the move started. needed to differentiate moves from clicks */
    private long mMovingStartTime;

    /** difference calculation between relative and screen coords. needed for some moving calculations */
    private float mMoveDiffX, mMoveDiffY;

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
                setMode(buttonView.isChecked()
                        ? DumbNodeButton.DumbNodeButtonModes.BUTTON
                        : DumbNodeButton.DumbNodeButtonModes.RAW);
            }
        });

        if (savedInstanceState == null) {
            mMainSwitch.setChecked(mMode == DumbNodeButton.DumbNodeButtonModes.BUTTON
                                        ? true : false);
        }
        else {
            // Use the saved state of the main switch to correctly set
            // the mode!
            mMode = mMainSwitch.isChecked()
                    ? DumbNodeButton.DumbNodeButtonModes.BUTTON
                    : DumbNodeButton.DumbNodeButtonModes.RAW;
        }


        mBuildTv = findViewById(R.id.build_tv);
        mSolveTv = findViewById(R.id.solve_tv);

        mHintTv = findViewById(R.id.bottom_hint_tv);

        mPlayArea = findViewById(R.id.play_area_fl);
        mPlayArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent playAreaEvent) {

                // only build a new button if we're in build (raw) mode and not currently connecting
                if ((playAreaEvent.getAction() == MotionEvent.ACTION_UP) &&
                    (mMode == DumbNodeButton.DumbNodeButtonModes.RAW) &&
                    !mConnecting) {
                        PointF touchLoc = new PointF(playAreaEvent.getX(), playAreaEvent.getY());

                        // todo: testing
                        PointF rawLoc = new PointF(playAreaEvent.getRawX(), playAreaEvent.getRawY());
                        float testX = rawLoc.x + mPlayArea.getLeft();
                        float testY = rawLoc.y + mPlayArea.getTop();
                        Log.d (TAG, "relative = " + touchLoc + ", raw = " + rawLoc +
                                ", and calculated = " + testX + ", " + testY);

                        newButton(touchLoc);
                    }
                return true;    // event consumed
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();

        // turn off status and navigation bar (top and bottom)
        fullScreenStickyImmersive();

        // Make sure the UI is properly set for the current mode.
        if (mMode == DumbNodeButton.DumbNodeButtonModes.RAW) {
            buildModeUI();
        }
        else {
            solveModeUI();
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

//    /** Disables or enables ALL buttons */
//    private void disableAllButtons(boolean disable) {
//        for (Object button : mGraph) {
//            ((NodeButtonOLD)button).setDisabled(disable);
//        }
//    }

//    /** mostly for testing */
//    private void moveModeAllButtons(boolean enableMoveMode) {
//        for (Object button : mGraph) {
//            ((NodeButtonOLD)button).setMovable(enableMoveMode);
//        }
//    }

    private void setAllButtonsBuild() {
        for (Object button : mGraph) {
            ((DumbNodeButton)button).setMode(DumbNodeButton.DumbNodeButtonModes.RAW);
        }
    }

    private void setAllButtonsSolve() {
        for (Object button : mGraph) {
            ((DumbNodeButton)button).setMode(DumbNodeButton.DumbNodeButtonModes.BUTTON);
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
    private void setMode(DumbNodeButton.DumbNodeButtonModes newMode) {

        if (mMode == newMode) {
            Log.e(TAG, "setMode(), trying to change to same mode! (mMode = " + mMode + " )");
            return;
        }
        mMode = newMode;

        // now do the ui
        switch (mMode) {
            case RAW:   // build mode
                setAllButtonsBuild();
                buildModeUI();
                break;

            case BUTTON:    // solve
                setAllButtonsSolve();
                solveModeUI();
                break;

            default:
                // get their attention!
                throw new EnumConstantNotPresentException(DumbNodeButton.DumbNodeButtonModes.class, "Unknown Mode in setMode()");
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

        final DumbNodeButton button = new DumbNodeButton(this);
        button.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT));

        final int id = mGraph.getUniqueNodeId();
        button.setXYCenter(relativeToParentLoc.x, relativeToParentLoc.y);

        button.setDumbNodeButtonListener(new DumbNodeButton.DumbNodeButtonListener() {
            @Override
            public void onPopupButtonClicked(int index) {
                Log.d(TAG, "click! index = " + index);
            }

            @Override
            public void onTouch(MotionEvent buttonEvent) {
                int action = buttonEvent.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "action down");

//                        startMove(buttonEvent, button);

                        // convert to play area coords.
                        float x = buttonEvent.getRawX() - mPlayArea.getLeft();
                        float y = buttonEvent.getRawY() - mPlayArea.getTop();
                        PointF playAreaPos = new PointF(x, y);
                        startMovePlayAreaPos(playAreaPos, button);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        continueMove(buttonEvent, button);
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "action up");
                        if (isRealMove(buttonEvent)) {
                            finishMove(buttonEvent, button);
                        }
                        else {
                            // not a move, it's a click. start connecting
                            startConnection(buttonEvent, button);
                        }
                        break;
                }
            }
        });
/*
        button.setButtonEventListener(new ButtonEventListener() {
//            @Override
//            public void onPopupButtonClicked(int index) {
//                Log.d(TAG, "click! index = " + index);
//            }

//            @Override
//            public void onExpand() {
//                Log.d(TAG, "expanding...");
//            }
//
//            @Override
//            public void onCollapse() {
//                Log.d(TAG, "...collapsing");
//            }

            // A connection click
            @Override
            public void onDisabledClick() {
                if (mConnecting) {
                    // completing a connection
                    connectButtons(mStartNodeId, id);
                    mConnecting = false;
                }

                else {
                    // beginning a connection
                    mConnecting = true;
                    mStartNodeId = id;
                    button.setHighlight(NodeButton.HighlightTypes.CONNECT);
//                    button.setHighlighted(true);
                    button.invalidate();    // todo: is this necessary?
                }
            }

            @Override
            public void onMoved(PointF oldLoc, PointF newLoc) {
//                mPlayArea.updateLines(oldLoc, newLoc);
                // remove and rebuild the line data
                mPlayArea.removeAllLines();
                for (int i = 0; i < mGraph.numEdges(); i++) {
                    Graph.Edge edge = mGraph.getEdge(i);
//                    NodeButtonOLD startNode = (NodeButtonOLD) mGraph.getNodeData(edge.startNodeId);
                    NodeButton startNode = (NodeButton) mGraph.getNodeData(edge.startNodeId);
//                    NodeButtonOLD endNode = (NodeButtonOLD) mGraph.getNodeData(edge.endNodeId);
                    NodeButton endNode = (NodeButton) mGraph.getNodeData(edge.endNodeId);

                    PointF startp = startNode.getCenter();
                    PointF endp = endNode.getCenter();
                    mPlayArea.addLine(startp, endp);
                }
                mPlayArea.invalidate();
            }
        });
*/

        mPlayArea.addView(button);

        try {
            //noinspection unchecked
            mGraph.addNode(id, button);
        }
        catch (GraphNodeDuplicateIdException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


    /**
     * Does the preparations for moving a button.  Additional prep
     * needs to be done as this could be just a click instead of
     * a move.
     *
     * @param buttonEvent   The event that started the move.  Note that this means
     *                      the positions are relative to the button's top left.
     *
     * @param button    The button that signaled the event.
      */
    private void startMove(MotionEvent buttonEvent, DumbNodeButton button) {
        mMoving = true;
        mMovingLastPos = new PointF(buttonEvent.getX(), buttonEvent.getY());
        mMovingStartPos = new PointF(mMovingLastPos.x, mMovingLastPos.y);

        // needed to convert coordinate systems
        mMoveDiffX = button.getX() - mMovingStartPos.x;
        mMoveDiffY = button.getY() - mMovingStartPos.y;

        mMovingStartTime = System.currentTimeMillis();
        mStartNodeId = button.getId();
    }

    private void startMovePlayAreaPos(PointF playAreaPos, DumbNodeButton button) {
        mMoving = true;
        mMovingLastPlayAreaPos = new PointF(playAreaPos.x, playAreaPos.y);
        mMovingStartPlayAreaPos = new PointF(playAreaPos.x, playAreaPos.y);

        mMovingStartTime = System.currentTimeMillis();
        mStartNodeId = button.getId();
    }

    /**
     * Call this to when an ACTION_MOVE event takes place for a button.  The app
     * should already be in a moving-a-button state (by calling {@link #startMove(MotionEvent, DumbNodeButton)}).
     *
     * @param buttonEvent   The MOVE event for the button in question. Note that this
     *                      means the coordinates will be relative to the Button.
     *
     * @param button    The DumbNodeButton that is being moved.
     */
    private void continueMove(MotionEvent buttonEvent, DumbNodeButton button) {
        if (!mMoving) {
            Log.e(TAG, "continueMove() while mMoving == false! Aborting!");
            return;
        }

        // Only bother if there has been an actual move
        if (isRealMove(buttonEvent)) {
            // Note that newLoc maintains its relationship to the PART of the
            // node that was touched. So if we press on the bottom left corner,
            // we'll continue to move the button by the bottom left corner.
            PointF newLoc = new PointF(buttonEvent.getRawX() + mMoveDiffX,
                                       buttonEvent.getRawY() + mMoveDiffY);
            Log.d(TAG, "newLoc = " + newLoc);

            // for calculating the bounding box, we need to get the entire box
            // of the node's view and make sure that THAT is within the play area.

            // Check to make sure we're in bounds
            Rect playAreaRect = new Rect();
            mPlayArea.getDrawingRect(playAreaRect);

            Rect nodeRect = new Rect();
            button.getDrawingRect(nodeRect);

            // convert the nodeRect to play area coords
            nodeRect.offset((int)newLoc.x, (int)newLoc.y);

            if (playAreaRect.contains(nodeRect)) {
                // todo: undraw the last lines

                // todo: draw the new lines

                // todo: draw the button

                button.setX(newLoc.x);
                button.setY(newLoc.y);

                mMovingLastPos = newLoc;

            }
            else {
                Log.d(TAG, "nope");
            }
        }
    }



    private void finishMove(MotionEvent buttonEvent, DumbNodeButton button) {
        if (!mMoving) {
            Log.e(TAG, "finishMove() while mMoving == false! Aborting!");
            return;
        }

//        PointF newLoc = new PointF(button.getX(), button.getY());   // gets point relative to button (left center of button???)
        PointF newLoc = new PointF(buttonEvent.getX(), buttonEvent.getY());

        // todo: undraw the last lines

        // todo: draw the new lines

        // todo: draw the button in its new location

        mMoving = false;
        mMovingLastPos = null;
    }

    /**
     * Call this to initiate a node connection.
     *
     * @param buttonEvent     The event that caused this connection to start
     *                        (presumably an ACTION_UP).
     *
     * @param button    The button that starts the connection.  This will
     *                  be highlighted.
     */
    private void startConnection(MotionEvent buttonEvent, DumbNodeButton button) {
        Log.d(TAG, "startConnection()");

        mConnecting = true;
        mStartNodeId = button.getId();

        button.setOutlineColor(R.color.button_build_border_connect);
        button.invalidate();    // todo: is this necessary?
    }

    /**
     * Does the logic and graphics of connecting two buttons.
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

        // cannot connect to yourself!
        if (startButtonId == endButtonId) {
            Log.v(TAG, "Attempting to connect a button to itself--aborted.");
            return;
        }

//        NodeButtonOLD startButton = (NodeButtonOLD) mGraph.getNodeData(startButtonId);
        NodeButton startButton = (NodeButton) mGraph.getNodeData(startButtonId);
//        NodeButtonOLD endButton = (NodeButtonOLD) mGraph.getNodeData(endButtonId);
        NodeButton endButton = (NodeButton) mGraph.getNodeData(endButtonId);

//        startButton.setHighlighted(false);
        startButton.setHighlight(NodeButton.HighlightTypes.NORMAL);

        PointF start = startButton.getCenter();
        PointF end = endButton.getCenter();

        // Does this node already exist?  If so, remove it.
        if (mGraph.getEdgeIndex(startButtonId, endButtonId) != -1) {
            Log.v(TAG, "Removing edge ( " + startButtonId + " - " + endButtonId + " )");

            // remove from graph
            mGraph.removeEdge(startButtonId, endButtonId);

            // remove from play area
            mPlayArea.removeLine(start, end);
            mPlayArea.invalidate();
        }
        else {
            // add this new line to the graph and the play area
            mGraph.addEdge(startButtonId, endButtonId);

            mPlayArea.addLine(start, end);
            mPlayArea.invalidate();
        }
    }


    /**
     * Determines if the user's finger has moved enough to consider this a real
     * move event, or if this is just slight shuddering of a finger that happens
     * during a button click.
     *
     * @param event     The current event that finalized the move (should be an
     *                  ACTION_UP event).  We'll need the coordinates.
     *
     * @return  TRUE iff the current location is sufficiently far enough away from
     *          the original ACTION_DOWN event to qualify as a move.
     */
    private boolean isRealMove(MotionEvent event) {

        boolean retval = true;
//        PointF currentPos = new PointF(event.getRawX(), event.getRawY());   // gets point relative to SCREEN!
        PointF currentPos = new PointF(event.getX(), event.getY());

        // check not enough movement
        if ((Math.abs(currentPos.x - mMovingLastPos.x) < CLICK_SLOP) &&
                (Math.abs(currentPos.y - mMovingLastPos.y) < CLICK_SLOP)) {
            retval = false;
        }

        // check not enough time
        long currentMillis = System.currentTimeMillis();
        if (currentMillis - mMovingStartTime < CLICK_MILLIS_THRESHOLD) {
            retval = false;
        }

        return retval;
    }

}

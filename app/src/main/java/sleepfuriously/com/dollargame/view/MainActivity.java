package sleepfuriously.com.dollargame.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Typeface;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.widget.Toolbar;


import java.util.List;
import java.util.Random;

import sleepfuriously.com.dollargame.R;
import sleepfuriously.com.dollargame.model.Graph;
import sleepfuriously.com.dollargame.model.GraphNodeDuplicateIdException;
import sleepfuriously.com.dollargame.model.GraphNotConnectedException;
import sleepfuriously.com.dollargame.model.Node;
import sleepfuriously.com.dollargame.view.AllAngleExpandableButton.ButtonEventListener;
import sleepfuriously.com.dollargame.view.buttons.MovableNodeButton;


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

    /** The node/button that is doing the acting (either a give or a take) */
    private MovableNodeButton mActingButton;

    //------------------------
    //  methods
    //------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBuildMode = true;   // start in build mode
        mConnecting = false;

        //
        // setup ui and widgets
        //

        // use my toolbar instead of default
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);    // shows back arrow
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

        mBuildTv = findViewById(R.id.build_tv);
        mSolveTv = findViewById(R.id.solve_tv);

        mHintTv = findViewById(R.id.bottom_hint_tv);

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

                    // todo: testing
//                    PointF rawLoc = new PointF(playAreaEvent.getRawX(), playAreaEvent.getRawY());
//                    float testX = rawLoc.x + mPlayArea.getLeft();
//                    float testY = rawLoc.y + mPlayArea.getTop();
//                    Log.d (TAG, "relative = " + touchLoc + ", raw = " + rawLoc +
//                            ", and calculated = " + testX + ", " + testY);

                    newButton(touchLoc);
                }
                return true;    // event consumed
            }
        });

        mConnectedIV = findViewById(R.id.connected_iv);
        mConnectedIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toastStr;

                boolean connected = (boolean) mConnectedIV.getTag();
                if (connected) {
                    toastStr = getString(R.string.connected_toast);
                }
                else {
                    toastStr = getString(R.string.not_connected_toast);
                }

                Toast.makeText(MainActivity.this, toastStr, Toast.LENGTH_LONG).show();
            }
        });

        mRandomizeAllButt = findViewById(R.id.random_all_butt);
        mRandomizeAllButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomizeAllNodes();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();

        // turn off status and navigation bar (top and bottom)
        fullScreenStickyImmersive();

        // handles refreshing UI
        refreshPrefs();
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

        final int id = mGraph.getUniqueNodeId();
        button.setId(id);

        button.setXYCenter(relativeToParentLoc.x, relativeToParentLoc.y);
        button.setBackgroundColorResource(R.color.button_bg_color_build_disconnected);

        button.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onPopupButtonClicked(int index) {
                switch (index) {
                    case 1:
                        startGive(button);
                        break;

                    case 2:
                        startTake(button);
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
                finishGiveTake(button);
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
            mGraph.addNode(id, button);
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

    private void startGive(MovableNodeButton button) {
        mGiving = true;
        mActingButton = button;
    }

    private void startTake(MovableNodeButton button) {
        mTaking = true;
        mActingButton = button;
    }

    /**
     * User has indicated that this node will give to each of its neighbors.
     * This handles the logic and UI for that move.
     *
     * @param button    The button that is doing the giving
     */
    private void finishGiveTake(MovableNodeButton button) {
        @SuppressWarnings("unchecked")
        int actorId = mGraph.getNodeId(button); // actor is the button that's doing the giving or taking

        @SuppressWarnings("unchecked")
        List<Integer> adjacentList = mGraph.getAllAdjacentTo(actorId);

        for (int adjacentNodeId : adjacentList) {
            MovableNodeButton adjacentNode = (MovableNodeButton) mGraph.getNodeData(adjacentNodeId);

            if (mGiving) {
                adjacentNode.incrementAmount();
                button.decrementAmount();

                adjacentNode.invalidate();
                button.invalidate();
            }

            if (mTaking) {
                adjacentNode.decrementAmount();
                button.incrementAmount();

                adjacentNode.invalidate();
                button.invalidate();
            }

        }

        if (isSolved()) {
            mConnectedIV.setImageResource(R.drawable.ic_solved);
        }
        else {
            mConnectedIV.setImageResource(R.drawable.ic_unsolved);
        }

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

        // figure out this constant that will be used throughout
        final int seekbarOffset = getResources().getInteger(R.integer.DOLLAR_AMOUNT_SEEKBAR_OFFSET);

        LayoutInflater inflater = getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.money_delete_dialog, null);

        final TextView dialogAmountTv = inflatedView.findViewById(R.id.dialog_amount);

        final SeekBar dialogSeekBar = inflatedView.findViewById(R.id.dialog_seekbar);
        dialogSeekBar.setProgress(button.getAmount() + seekbarOffset);

        dialogSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String dollarStr = getString(R.string.dollar_number, progress - seekbarOffset);
                dialogAmountTv.setText(dollarStr);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        final Button dialogRandomButt = inflatedView.findViewById(R.id.dialog_rand_butt);
        dialogRandomButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                random.setSeed(System.currentTimeMillis());

                int randInt = random.nextInt((getResources().getInteger(R.integer.MAX_DOLLAR_AMOUNT_NON_NEGATIVE)) + 1);
                dialogSeekBar.setProgress(randInt);
            }
        });

        final ToggleButton nodeToggleButt = inflatedView.findViewById(R.id.dialog_delete_butt);

        String initialDollarStr = getString(R.string.dollar_number,
                dialogSeekBar.getProgress() - seekbarOffset);
        dialogAmountTv.setText(initialDollarStr);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(inflatedView)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (nodeToggleButt.isChecked()) {
                            deleteNode(button);
                        }
                        else {
                            button.setAmount(dialogSeekBar.getProgress() - seekbarOffset);
                            setCountUI();
                        }
                    }
                });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
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

        // todo: depending on options, this may need to ensure that the puzzle is genus-solvable.

        Random random = new Random();

        @SuppressWarnings("unchecked")
        List<Integer> nodeIds = mGraph.getAllNodeIds();

        for (int nodeId : nodeIds) {
            MovableNodeButton node = (MovableNodeButton) mGraph.getNodeData(nodeId);

            // gets a random number [0, MAX_DOLLAR_AMOUNT_NON_NEGATIVE)
            int positive_max = getResources().getInteger(R.integer.MAX_DOLLAR_AMOUNT_NON_NEGATIVE) + 1;
            int offset = positive_max / 2;

            int randInt = random.nextInt(positive_max);
            node.setAmount(randInt - offset);

            node.invalidate();
        }

        setGenusUI();
        setCountUI();
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

        boolean showHint = prefs.getBoolean(getResources().getString(R.string.pref_hints_cb_key), true);
        mHintTv.setVisibility(showHint ? View.VISIBLE : View.GONE);

        if (mBuildMode) {
            buildModeUI();
        }
        else {
            solveModeUI();
        }
    }
}

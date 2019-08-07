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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import sleepfuriously.com.dollargame.R;


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

    //------------------------
    //  widgets
    //------------------------

    /** The play are of the game */
    FrameLayout mPlayArea;


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
        mPlayArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle Android UI elements.  This is complicated!
                // I have to compare the actual screen size (getMetrics)
                // with the size returned by getSize.  If they are different,
                // then the navbar and stuff are displayed.
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                Point p = new Point();
                getWindowManager().getDefaultDisplay().getSize(p);

//                Log.d(TAG, "metrics.height = " + metrics.heightPixels + ", p.y = " + p.y);
                boolean androidUiDisplaying = (metrics.heightPixels == p.y);
                if (androidUiDisplaying) {
                    // check other axis (some devices keep the bars on the ends in landscape mode)
                    androidUiDisplaying = (metrics.widthPixels == p.x);
                }

                if (androidUiDisplaying) {
                    fullScreen(true);
                }
                else {
                    fullScreen(false);
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        // turn off status and navigation bar (top and bottom)
        fullScreen(true);
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



}

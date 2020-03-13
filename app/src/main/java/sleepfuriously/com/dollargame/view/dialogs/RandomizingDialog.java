package sleepfuriously.com.dollargame.view.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;

import java.util.Random;

import sleepfuriously.com.dollargame.R;

/**
 * Shows the dialog while waiting for the node randomizing
 * occurs.  If there are a lot of nodes, this could take
 * several seconds.
 */
public class RandomizingDialog {

    //---------------------------
    //  constants
    //---------------------------

    //---------------------------
    //  data
    //---------------------------

    /** reference to the dialog that is showing */
    private AlertDialog mDialog;

    //---------------------------
    //  methods
    //---------------------------


    /**
     * Begins displaying the dialog.
     *
     * @param ctx   needed to get resources
     */
    public void show(final Context ctx) {

        LayoutInflater inflater = LayoutInflater.from(ctx);
        @SuppressLint("InflateParams")
        View inflatedView = inflater.inflate(R.layout.randomize_wait_dialog, null);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);
        dialogBuilder.setView(inflatedView)
                .setCancelable(false);

        mDialog = dialogBuilder.create();
        mDialog.show();
    }

    /**
     * Ends display of this dialog.
     */
    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

}

package sleepfuriously.com.biggsdollargame.view;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import sleepfuriously.com.biggsdollargame.R;

/**
 * Displays info about this app
 */
public class AboutActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credits_layout);

        Button done_button = findViewById(R.id.done_butt);
        done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // these enable links in the text strings to work
        TextView aboutTv = findViewById(R.id.inspired_tv);
        aboutTv.setMovementMethod(LinkMovementMethod.getInstance());

        TextView moreTv = findViewById(R.id.more_tv);
        moreTv.setMovementMethod(LinkMovementMethod.getInstance());

        TextView versionTv = findViewById(R.id.version_tv);
        String versionName;

        try {
            versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = getString(R.string.version_error);
        }
        versionTv.setText(versionName);

    }


}

package edu.ksu.wheatgenetics.verify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.Cap;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import static edu.ksu.wheatgenetics.verify.VerifyConstants.CAMERA_INTENT_REQ;

/**
 * Created by chaneylc on 6/22/2017.
 */

public class CaptureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanner);

        ((Button) findViewById(R.id.zxing_back_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CaptureActivity.this.finish();
            }
        });
        final IntentIntegrator scanner = new IntentIntegrator(this);

        scanner.setCaptureLayout(R.layout.activity_scanner);

        scanner.initiateScan();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            final String code = result.getContents();
            if (code != null && !code.isEmpty()) {
                final Intent data = new Intent();
                data.putExtra(VerifyConstants.CAMERA_RETURN_ID, code);
                setResult(RESULT_OK, data);
            }
        }

        //this activity starts an inner intent that fills the parent activity
        //must call super.finish as well to close both activities.
        CaptureActivity.this.finish();
        CaptureActivity.super.finish();
    }
}
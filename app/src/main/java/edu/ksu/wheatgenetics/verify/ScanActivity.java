package edu.ksu.wheatgenetics.verify;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

import static edu.ksu.wheatgenetics.verify.VerifyConstants.CAMERA_INTENT_REQ;

/**
 * Created by chaneylc on 6/22/2017.
 */

public class ScanActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScannerView;
    private String lastText;

    private BarcodeCallback callback = new BarcodeCallback() {

        @Override
        public void barcodeResult(BarcodeResult result) {

            if (result.getText() == null || result.getText().equals(lastText)) return;

            lastText = result.getText();
            barcodeScannerView.setStatusText(result.getText());

            ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.GREEN));

            final Intent i = new Intent();
            i.putExtra(VerifyConstants.CAMERA_RETURN_ID, result.getText());

            setResult(RESULT_OK, i);
            finish();

        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.capture_activity);
        barcodeScannerView = (DecoratedBarcodeView)
                findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.getBarcodeView().getCameraSettings().setContinuousFocusEnabled(true);
        barcodeScannerView.getBarcodeView().getCameraSettings().setBarcodeSceneModeEnabled(true);
        barcodeScannerView.decodeContinuous(callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_back_menu, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_home:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeScannerView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
package edu.ksu.cis.verify;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import edu.ksu.cis.mobilevisbarcodechecker.R;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private final static int OPEN_CSV_FILE = 100;
    private SurfaceView _cameraView;
    private CameraSource _camera;
    private BarcodeDetector _detector;
    private Context _ctx;
    private SparseArray<String> _ids;
    private String _prevValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);

        _ctx = this;

        _ids = new SparseArray<String>();
        _cameraView = (SurfaceView) findViewById(R.id.cameraView);
        _detector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        final Intent calledIntent = getIntent();
        if (calledIntent.hasExtra(VerifyConstants.KEY_ARRAY)) {
            final String[] keys = calledIntent.getStringArrayExtra(VerifyConstants.KEY_ARRAY);
            final int keys_count = keys.length;
            for (int i = 0; i < keys_count; i = i + 1) {
                _ids.append(_ids.size(), keys[i]);
            }
        }

        _detector.setProcessor(new Detector.Processor<Barcode>() {

            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0 && !barcodes.valueAt(0).rawValue.equals(_prevValue)) {
                    _prevValue = barcodes.valueAt(0).rawValue;
                    new AsyncValueUpdate().execute(_prevValue);
                } else _prevValue = "";
            }
        });

        _camera = new CameraSource.Builder(this, _detector)
                //.setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .setRequestedFps(30)
                .build();

        _cameraView.getHolder().addCallback(this);

        ((Button) findViewById(R.id.clearButton))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _ids.clear();
                    }
                });

        ((Button) findViewById(R.id.openButton))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

        if (savedInstanceState != null) {
            final String[] savedIds = savedInstanceState.getStringArray(VerifyConstants.KEY_ARRAY);

            if (savedIds != null) {
                for (int i = 0; i < savedIds.length; i = i + 1) {
                    _ids.append(_ids.size(), savedIds[i]);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);

        // final Button openButton = ((Button) findViewById(R.id.openButton));
        // final Button clearButton = ((Button) findViewById(R.id.clearButton));
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            buildCamera();
            //openButton.setVisibility(View.INVISIBLE);
            //clearButton.setVisibility(View.INVISIBLE);
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            buildCamera();
            // openButton.setVisibility(View.VISIBLE);
            //clearButton.setVisibility(View.VISIBLE);
        }
    }

    private void buildCamera() {
        _camera.stop();
        _camera = new CameraSource.Builder(this, _detector)
                //.setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .setRequestedFps(30)
                .build();
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            _camera.start(_cameraView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final String[] ids = new String[_ids.size()];
        for (int i = 0; i < _ids.size(); i = i + 1) {
            ids[i] = _ids.get(_ids.keyAt(i));
        }
        outState.putStringArray(VerifyConstants.KEY_ARRAY, ids);
    }

    /**
     * This is called immediately after the surface is first created.
     * Implementations of this should start up whatever rendering code
     * they desire.  Note that only one thread can ever draw into
     * a surface, so you should not draw into the Surface here
     * if your normal rendering will be in another thread.
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (ActivityCompat.checkSelfPermission(_ctx, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (holder.equals(_cameraView.getHolder()))
                _camera.start(_cameraView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is called immediately after any structural changes (format or
     * size) have been made to the surface.  You should at this point update
     * the imagery in the surface.  This method is always called at least
     * once, after {@link #surfaceCreated}.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width  The new width of the surface.
     * @param height The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * This is called immediately before a surface is being destroyed. After
     * returning from this call, you should no longer try to access this
     * surface.  If you have a rendering thread that directly accesses
     * the surface, you must ensure that thread is no longer touching the
     * Surface before returning from this function.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        _camera.stop();
    }

    private class AsyncValueUpdate extends AsyncTask<String, Void, Pair<Boolean, String>> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Pair<Boolean, String> doInBackground(String... params) {
            return checkId(params[0]);
        }

        @Override
        protected void onPostExecute(Pair<Boolean, String> result) {

            if (result.first) {
                Toast.makeText(_ctx, "ID Found! " + result.second, Toast.LENGTH_SHORT).show();
                final Intent resIntent = new Intent();
                resIntent.putExtra(VerifyConstants.PREV_ID_LOOKUP, result.second);
                setResult(RESULT_OK, resIntent);
                finish();
            } else {
                if (result.second != null) {
                    Toast.makeText(_ctx, "ID not in list: " + result.second, Toast.LENGTH_SHORT).show();
                }
            }

        }

        private Pair<Boolean, String> checkId(String value) {

            if (value != null) {
                for (int i = 0; i < _ids.size(); i = i + 1) {
                    final String s = _ids.get(_ids.keyAt(i));
                    if (s.equals(value)) {
                        return new Pair<>(true, value);
                    }
                }
            }
            return new Pair<>(false, value);
        }
    }

}

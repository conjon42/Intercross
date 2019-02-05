package org.phenoapps.intercross

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

import com.google.zxing.ResultPoint

import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import org.phenoapps.intercross.IntercrossActivity.Companion.CAMERA_RETURN_ID

class ScanActivity : AppCompatActivity() {

    private var barcodeScannerView: DecoratedBarcodeView? = null
    private var lastText: String? = null

    private val callback = object : BarcodeCallback {

        override fun barcodeResult(result: BarcodeResult) {

            if (result.text == null || result.text == lastText) return

            lastText = result.text
            barcodeScannerView!!.setStatusText(result.text)

            val imageView = findViewById(org.phenoapps.intercross.R.id.barcodePreview) as ImageView
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.GREEN))

            val i = Intent()
            i.putExtra(CAMERA_RETURN_ID, result.text)

            setResult(Activity.RESULT_OK, i)
            finish()

        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(org.phenoapps.intercross.R.layout.activity_capture)
        barcodeScannerView = findViewById(org.phenoapps.intercross.R.id.zxing_barcode_scanner) as DecoratedBarcodeView
        barcodeScannerView!!.barcodeView.cameraSettings.isContinuousFocusEnabled = true
        barcodeScannerView!!.barcodeView.cameraSettings.isBarcodeSceneModeEnabled = true
        barcodeScannerView!!.decodeContinuous(callback)

        if (supportActionBar != null) {
            supportActionBar!!.title = null
            supportActionBar!!.themedContext
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        barcodeScannerView!!.resume()
    }

    override fun onPause() {
        super.onPause()

        barcodeScannerView!!.pause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        return barcodeScannerView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
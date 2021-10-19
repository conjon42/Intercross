package org.phenoapps.intercross.util

import android.graphics.Bitmap
import android.graphics.Color

import android.os.AsyncTask
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel


//TODO: chaneylc rendered barcodes are not whats printed

class AsyncLoadBarcode(val imageView: ImageView, val TAG: String) : AsyncTask<String?, Void?, Bitmap?>() {

    override fun doInBackground(vararg codes: String?): Bitmap? {

        val code = codes.first()

        if (code?.isNotBlank() != false) {

            val bitmatrix = QRCodeWriter()
                    .encode(code, BarcodeFormat.QR_CODE, 256, 256)

            val w = bitmatrix.width
            val h = bitmatrix.height
            val pixels = IntArray(w*h)
            for (y in 0 until h) {
                val offset = y*w
                for (x in 0 until w) {
                    pixels[offset+x] = if (bitmatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
            bmp.setPixels(pixels, 0, 256, 0, 0, w, h)

            return bmp

        }

        return null

    }

    override fun onPostExecute(bitmap: Bitmap?) {

        if (imageView.tag == TAG) {

            bitmap?.let {

                imageView.setImageBitmap(it)

            }
        }
    }
}
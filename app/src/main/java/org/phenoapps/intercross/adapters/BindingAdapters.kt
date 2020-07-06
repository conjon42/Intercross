package org.phenoapps.intercross.adapters

import android.graphics.Bitmap
import android.graphics.Color
import android.widget.EditText
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.coroutineScope
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.CrossType
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Settings
import java.util.*

@BindingAdapter("setImageByCrossType")
fun bindCrossTypeImage(view: ImageView, event: Event?) {

    if (event == null) {

        view.setImageResource(R.drawable.ic_cross_unknown)

    } else {

        view.setImageResource(when(event.type) {

            CrossType.BIPARENTAL -> R.drawable.ic_cross_biparental

            CrossType.POLY -> R.drawable.ic_cross_poly

            CrossType.SELF -> R.drawable.ic_cross_self

            CrossType.OPEN -> R.drawable.ic_cross_open

            CrossType.UNKNOWN -> R.drawable.ic_cross_unknown

        })
    }
}

@BindingAdapter("setQRCode")
fun bindQRCodeImage(view: ImageView, code: String?) {

    if (!code.isNullOrEmpty()) {

        val bitmatrix = QRCodeWriter()
                .encode(code, BarcodeFormat.QR_CODE, 256, 256,
                        mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L))


        val bmp = Bitmap.createBitmap(bitmatrix.width, bitmatrix.height, Bitmap.Config.RGB_565)

        for (x in 0 until bitmatrix.width) {

            for (y in 0 until bitmatrix.height) {

                bmp.setPixel(x, y, if (bitmatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }

        view.setImageBitmap(bmp)
    }

}

@BindingAdapter("setCrossId")
fun bindCrossId(view: EditText, settings: Settings?) {

    settings?.let {

        with (it) {

            when {

                isPattern -> {

                    view.setText(pattern)

                }

                isUUID-> {

                    view.setText(UUID.randomUUID().toString())

                }
            }
        }
    }
}
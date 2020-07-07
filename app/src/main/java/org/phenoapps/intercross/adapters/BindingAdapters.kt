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
import org.phenoapps.intercross.util.AsyncLoadBarcode
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

    code?.let {

        view.tag = code

        AsyncLoadBarcode(view, code).execute(code)
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
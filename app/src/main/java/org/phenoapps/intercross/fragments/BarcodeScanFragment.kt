package org.phenoapps.intercross.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Handler
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import org.phenoapps.intercross.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.PollenGroup
import org.phenoapps.intercross.data.models.Settings
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.data.viewmodels.CrossSharedViewModel
import org.phenoapps.intercross.data.viewmodels.EventProducer
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.databinding.FragmentBarcodeScanBinding
import org.phenoapps.intercross.util.FileUtil
import java.util.*

class BarcodeScanFragment: IntercrossBaseFragment<FragmentBarcodeScanBinding>(R.layout.fragment_barcode_scan) {


    //TODO update with new view models

    private lateinit var mEventStore: EventProducer
    private lateinit var mSettingsStore: SettingsViewModel
    private lateinit var mWishlistStore: WishlistViewModel
    private lateinit var mSharedViewModel: CrossSharedViewModel

    private var mSettings = Settings()
    private lateinit var mBarcodeScanner: DecoratedBarcodeView

    private lateinit var mCallback: BarcodeCallback

    private lateinit var mWishlist: List<Wishlist>

    private lateinit var mGroups: List<PollenGroup>

    private var mEvents = ArrayList<Event>()

    private var lastText: String? = null

    private fun isCameraAllowed(): Boolean {

        if (checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) return true

        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), MainActivity.REQ_CAMERA)

        return false
    }

    override fun FragmentBarcodeScanBinding.afterCreateView() {

        isCameraAllowed()

        arguments?.let {
            zxingBarcodeScanner.setStatusText(
                when (it.getString("mode")) {
                    "search" -> "Search by barcode"
                    "continuous" -> "Scan a sequence of barcodes"
                    else -> "Scan a single barcode"
                })
        }

        mCallback = object : BarcodeCallback {

            override fun barcodeResult(result: BarcodeResult) {

                if (result.text == null) return // || result.text == lastText) return

                lastText = result.text
                zxingBarcodeScanner.statusView.text = "Single Mode"

                arguments?.let {
                    when(it.getString("mode")) {
                        "single" -> {
                            zxingBarcodeScanner.setStatusText("Single")
                            //mSharedViewModel.lastScan.value = result.text.toString()
                            findNavController().popBackStack()
                        }
                        "search" -> {
                            zxingBarcodeScanner.setStatusText("Search Mode")

                            //mSharedViewModel.lastScan.value = result.text.toString()
//                            mEvents.forEach { event ->
//                                if (event.eventDbId == result.text.toString()) {
//                                    findNavController().navigate(BarcodeScanFragmentDirections
//                                            .globalActionToEventFragment(event))
//                                }
//                            }
                        }
                        "continuous" -> {
                            zxingBarcodeScanner.setStatusText("Continuous Mode")

                            val order = PreferenceManager.getDefaultSharedPreferences(requireContext())
                                    .getString(SettingsFragment.ORDER, "0")
                            when (order) {
                                "0" -> when {
                                    (mSharedViewModel.female.value ?: "").isEmpty() -> {
                                        mSharedViewModel.female.value = result.text.toString()
                                        female.setImageBitmap(result.getBitmapWithResultPoints(Color.RED))
                                        Handler().postDelayed({
                                            mBarcodeScanner.barcodeView.decodeSingle(mCallback)
                                        }, 2000)
                                    }
                                    ((mSharedViewModel.male.value ?: "").isEmpty()) -> {
                                        mSharedViewModel.male.value = result.text.toString()
                                        male.setImageBitmap(result.getBitmapWithResultPoints(Color.BLUE))
                                        if (mSettings.isUUID || mSettings.isPattern) {
                                            FileUtil(requireContext()).ringNotification(true)
                                            submitCross()
                                        }
                                        else Handler().postDelayed({
                                            mBarcodeScanner.barcodeView.decodeSingle(mCallback)
                                        }, 2000)

                                    }
                                    ((mSharedViewModel.name.value ?: "").isEmpty() && !(mSettings.isUUID || mSettings.isPattern)) -> {
                                        mSharedViewModel.name.value = result.text.toString()
                                        cross.setImageBitmap(result.getBitmapWithResultPoints(Color.GREEN))
                                        FileUtil(requireContext()).ringNotification(true)
                                        submitCross()
                                    }
                                }
                                "1" -> when {
                                    (mSharedViewModel.male.value ?: "").isEmpty() -> {
                                        mSharedViewModel.male.value = result.text.toString()
                                        male.setImageBitmap(result.getBitmapWithResultPoints(Color.BLUE))
                                        Handler().postDelayed({
                                            mBarcodeScanner.barcodeView.decodeSingle(mCallback)
                                        }, 2000)
                                    }
                                    (mSharedViewModel.female.value ?: "").isEmpty() -> {
                                        mSharedViewModel.female.value = result.text.toString()
                                        female.setImageBitmap(result.getBitmapWithResultPoints(Color.RED))
                                        if (mSettings.isUUID || mSettings.isPattern) {
                                            FileUtil(requireContext()).ringNotification(true)
                                            submitCross()
                                        }
                                        else Handler().postDelayed({
                                            mBarcodeScanner.barcodeView.decodeSingle(mCallback)
                                        }, 2000)

                                    }
                                    (mSharedViewModel.name.value ?: "").isEmpty() && !(mSettings.isUUID || mSettings.isPattern) -> {
                                        mSharedViewModel.name.value = result.text.toString()
                                        cross.setImageBitmap(result.getBitmapWithResultPoints(Color.GREEN))
                                        FileUtil(requireContext()).ringNotification(true)
                                        submitCross()
                                    }
                                }

                            }
                            ""
                        }
                        else -> ""
                    }
                }
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {

            }

        }

        mBarcodeScanner = zxingBarcodeScanner

        mBarcodeScanner.barcodeView.apply {

            cameraSettings.isContinuousFocusEnabled = true

            cameraSettings.isAutoTorchEnabled = true

            cameraSettings.isAutoFocusEnabled = true

            cameraSettings.isBarcodeSceneModeEnabled = true

            decodeSingle(mCallback)
        }


        mEventStore = EventProducer(EventsRepository.getInstance(db.eventsDao()))
        mSettingsStore = SettingsViewModel(SettingsRepository.getInstance(db.settingsDao()))
        mWishlistStore = WishlistViewModel(WishlistRepository.getInstance(db.wishlistDao()))
        mSharedViewModel = CrossSharedViewModel()

        startObservers()
    }

    private fun startObservers() {

        mWishlistStore.wishlist.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                mWishlist = it
            }
        })

        mEventStore.events.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                mEvents = ArrayList(it)
            }
        })


//        mSettingsStore.settings.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//            it?.let {
//                mSettings = it
//            }
//        })
        mSharedViewModel.name.value = ""
        mSharedViewModel.female.value = ""
        mSharedViewModel.male.value = ""
    }

    private fun checkWishlist(f: String, m: String, x: String) {

        var isOnList = false
        var min = 0
        var current = 0

        mWishlist.forEach {
            if (it.femaleDbId == f && it.maleDbId == m) {
                isOnList = true
                min = it.wishMin
                current++
            }
        }
        if (isOnList) {
            mEvents.forEach {
                if (it.femaleObsUnitDbId == f && it.maleObsUnitDbId == m) {
                    current++
                }
            }
        }

        if (current >= min && min != 0) {
            FileUtil(requireContext()).ringNotification(true)
            Snackbar.make(mBinding.root, "Wishlist complete for $f and $m : $current/$min", Snackbar.LENGTH_LONG).show()
        } else Snackbar.make(mBinding.root,
                "New Cross Event! $x added.", Snackbar.LENGTH_SHORT).show()
    }

    fun submitCross() {

        val cross =
        when {
            mSettings.isPattern -> {
                val n = mSettings.number
                mSettings.number += 1
                mSettingsStore.update(mSettings)
                "${mSettings.prefix}${n.toString().padStart(mSettings.pad, '0')}${mSettings.suffix}"
            }
            mSettings.isUUID -> {
                UUID.randomUUID().toString()
            }
            else -> mSharedViewModel.name.value ?: String()
        }
        mBarcodeScanner.barcodeView.stopDecoding()

        var male = mSharedViewModel.male.value ?: String()
        if (male.isEmpty()) male = "blank"

        checkWishlist(mSharedViewModel.female.value ?: String(),
                male,
                cross)

        val experiment = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("org.phenoapps.intercross.EXPERIMENT", "")

        val person = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("org.phenoapps.intercross.PERSON", "")

//        val e = Event(cross,
//                mSharedViewModel.female.value ?: String(),
//                male,
//                "",
//                DateUtil().getTime(), person ?: "?", experiment ?: "?")

//        mGroups.let { it ->
//            val groups = it.map { it.eid }
//            if (e.maleObsUnitDbId in groups) {
//                e.isPoly = true
//            }
//        }

        //mEventStore.addCrossEvent(e)

        mSharedViewModel.name.value = ""
        mSharedViewModel.female.value = ""
        mSharedViewModel.male.value = ""

        Handler().postDelayed({
            mBinding.cross.setImageResource(0)
            mBinding.female.setImageResource(0)
            mBinding.male.setImageResource(0)
            mBarcodeScanner.barcodeView.decodeSingle(mCallback)
        }, 2000)

    }

    override fun onResume() {
        super.onResume()

        mBarcodeScanner.resume()
    }

    override fun onPause() {
        super.onPause()

        mBarcodeScanner.pause()
    }

}
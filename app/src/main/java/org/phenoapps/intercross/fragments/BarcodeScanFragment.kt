package org.phenoapps.intercross.fragments

import android.Manifest
import android.graphics.Color
import android.os.Handler
import android.preference.PreferenceManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.phenoapps.intercross.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.Settings
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.data.viewmodels.CrossSharedViewModel
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.SettingsViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.FragmentBarcodeScanBinding
import org.phenoapps.intercross.util.CrossUtil
import org.phenoapps.intercross.util.FileUtil
import java.util.*

class BarcodeScanFragment: IntercrossBaseFragment<FragmentBarcodeScanBinding>(R.layout.fragment_barcode_scan) {

    private companion object {

        val SINGLE = 0

        val SEARCH = 1

        val CONTINUOUS = 2
    }

    private val viewModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private val parentsModel: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    private val settingsModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository.getInstance(db.settingsDao()))
    }

    private val wishModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val mSharedViewModel: CrossSharedViewModel by activityViewModels()

    private var mSettings = Settings()

    private lateinit var mBarcodeScanner: DecoratedBarcodeView

    private lateinit var mCallback: BarcodeCallback

    private var mWishlist: List<WishlistView> = ArrayList()

    private var mEvents = ArrayList<Event>()

    private var mParents = ArrayList<Parent>()

    private var lastText: String? = null

    private val checkCamPermissions by lazy {

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

            if (granted) {

                mBinding.setupBarcodeScanner()

            }
        }
    }

    private fun FragmentBarcodeScanBinding.setupBarcodeScanner() {

        mCallback = object : BarcodeCallback {

            override fun barcodeResult(result: BarcodeResult) {

                if (result.text == null) return // || result.text == lastText) return

                lastText = result.text

                zxingBarcodeScanner.statusView.text = getString(R.string.zxing_scan_mode_single)

                arguments?.let {

                    when(it.getInt("mode")) {

                        SINGLE -> {

                            zxingBarcodeScanner.setStatusText(getString(R.string.zxing_status_single))

                            mSharedViewModel.lastScan.postValue(result.text.toString())

                            findNavController().popBackStack()

                        }
                        SEARCH -> {

                            zxingBarcodeScanner.setStatusText(getString(R.string.zxing_scan_mode_search))

                            val scannedEvent = mEvents.find { event -> event.eventDbId == result.text.toString().toLowerCase(Locale.ROOT) }

                            findNavController().navigate(BarcodeScanFragmentDirections
                                    .actionToEventFragmentFromScan(scannedEvent?.id ?: -1L))

                        }
                        CONTINUOUS -> {
                            zxingBarcodeScanner.setStatusText(getString(R.string.zxing_scan_mode_continuous))

                            val maleFirst = PreferenceManager.getDefaultSharedPreferences(requireContext())
                                    .getBoolean(SettingsFragment.ORDER, false)

                            when (maleFirst) {

                                false -> when {

                                    (mSharedViewModel.female.value ?: "").isEmpty() -> {
                                        mSharedViewModel.female.value = result.text.toString()
                                        female.setImageBitmap(result.getBitmapWithResultPoints(Color.RED))
                                        Handler().postDelayed({
                                            mBarcodeScanner.barcodeView.decodeSingle(mCallback) }, 2000)
                                    }
                                    ((mSharedViewModel.male.value ?: "").isEmpty()) -> {
                                        mSharedViewModel.male.value = result.text.toString()
                                        male.setImageBitmap(result.getBitmapWithResultPoints(Color.BLUE))
                                        if (mSettings.isUUID || mSettings.isPattern) {
                                            FileUtil(requireContext()).ringNotification(true)
                                            submitCross()
                                        }
                                        else Handler().postDelayed({
                                            mBarcodeScanner.barcodeView.decodeSingle(mCallback) }, 2000)

                                    }
                                    ((mSharedViewModel.name.value ?: "").isEmpty() && !(mSettings.isUUID || mSettings.isPattern)) -> {
                                        mSharedViewModel.name.value = result.text.toString()
                                        cross.setImageBitmap(result.getBitmapWithResultPoints(Color.GREEN))
                                        FileUtil(requireContext()).ringNotification(true)
                                        submitCross()
                                    }
                                }

                                true -> when {

                                    (mSharedViewModel.male.value ?: "").isEmpty() -> {
                                        mSharedViewModel.male.value = result.text.toString()
                                        male.setImageBitmap(result.getBitmapWithResultPoints(Color.BLUE))
                                        Handler().postDelayed({
                                            mBarcodeScanner.barcodeView.decodeSingle(mCallback) }, 2000)
                                    }
                                    (mSharedViewModel.female.value ?: "").isEmpty() -> {
                                        mSharedViewModel.female.value = result.text.toString()
                                        female.setImageBitmap(result.getBitmapWithResultPoints(Color.RED))
                                        if (mSettings.isUUID || mSettings.isPattern) {
                                            FileUtil(requireContext()).ringNotification(true)
                                            submitCross()
                                        }
                                        else {
                                            Handler().postDelayed({ mBarcodeScanner.barcodeView.decodeSingle(mCallback) }, 2000)
                                        }

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
    }


    override fun FragmentBarcodeScanBinding.afterCreateView() {

        checkCamPermissions.launch(Manifest.permission.CAMERA)

        val searchBarcodeString = getString(R.string.search_barcode_description)

        val singleScanString = getString(R.string.single_scan_description)

        val continuousScanString = getString(R.string.continuous_scan_description)

        arguments?.let {

            zxingBarcodeScanner.setStatusText(

                when (it.getInt("mode")) {

                    SEARCH -> searchBarcodeString

                    CONTINUOUS -> continuousScanString

                    else -> singleScanString

                })
        }

        (activity as MainActivity).supportActionBar?.hide()

        setupBarcodeScanner()

        startObservers()
    }

    private fun startObservers() {

        val isCommutative = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("org.phenoapps.intercross.COMMUTATIVE_CROSSING", false)

        if (isCommutative) {

            wishModel.commutativeWishes.observe(viewLifecycleOwner, {
                it?.let {
                    mWishlist = it.filter { wish -> wish.wishType == "cross" }
                }
            })

        } else {

            wishModel.wishes.observe(viewLifecycleOwner, {
                it?.let {
                    mWishlist = it.filter { wish -> wish.wishType == "cross" }
                }
            })

        }

        viewModel.events.observe(viewLifecycleOwner, {
            it?.let {
                mEvents = ArrayList(it)
            }
        })

        settingsModel.settings.observe(viewLifecycleOwner, {
            it?.let {
                mSettings = it
            }
        })

        mSharedViewModel.name.value = ""
        mSharedViewModel.female.value = ""
        mSharedViewModel.male.value = ""
    }

    fun submitCross() {

        mBarcodeScanner.barcodeView.stopDecoding()

        val female = mSharedViewModel.female.value ?: String()

        var male = mSharedViewModel.male.value ?: String()

        val cross = mSharedViewModel.name.value ?: String()

        if (male.isEmpty()) male = "blank"

        scope.launch {

            context?.let { ctx ->

                with(CrossUtil(ctx)) {

                    val eid =
                        withContext(Dispatchers.Default) {
                            submitCrossEvent(
                                mBinding.root,
                                female,
                                male,
                                cross,
                                mSettings,
                                settingsModel,
                                viewModel,
                                mParents,
                                parentsModel,
                                mWishlist)
                    }

                    activity?.runOnUiThread {

                        mSharedViewModel.name.value = ""
                        mSharedViewModel.female.value = ""
                        mSharedViewModel.male.value = ""

                        Handler().postDelayed({
                            mBinding.cross.setImageResource(0)
                            mBinding.female.setImageResource(0)
                            mBinding.male.setImageResource(0)
                            mBarcodeScanner.barcodeView.decodeSingle(mCallback)
                        }, 2000)

                        checkPrefToOpenCrossEvent(findNavController(),
                            BarcodeScanFragmentDirections.actionToEventFragmentFromScan(eid))
                    }
                }
            }
        }
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
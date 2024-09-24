package org.phenoapps.intercross.fragments

import android.Manifest
import android.graphics.Color
import android.os.Handler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.*
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Meta
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.Settings
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.data.viewmodels.*
import org.phenoapps.intercross.data.viewmodels.factory.*
import org.phenoapps.intercross.databinding.FragmentBarcodeScanBinding
import org.phenoapps.intercross.util.CrossUtil
import org.phenoapps.intercross.util.Dialogs
import org.phenoapps.intercross.util.FileUtil
import org.phenoapps.intercross.util.KeyUtil
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

    private val metaValuesViewModel: MetaValuesViewModel by viewModels {
        MetaValuesViewModelFactory(MetaValuesRepository.getInstance(db.metaValuesDao()))
    }

    private val metadataViewModel: MetadataViewModel by viewModels {
        MetadataViewModelFactory(MetadataRepository.getInstance(db.metadataDao()))
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val mSharedViewModel: CrossSharedViewModel by activityViewModels()

    private var mSettings = Settings()

    private lateinit var mBarcodeScanner: DecoratedBarcodeView

    private lateinit var mCallback: BarcodeCallback

    private var mWishlist: List<WishlistView> = ArrayList()

    private var mEvents = ArrayList<Event>()

    private var mParents = ArrayList<Parent>()

    private var mMetadata = ArrayList<Meta>()

    private var lastText: String? = null

    private val mPrefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

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

                            if (scannedEvent == null) {

                                mParents.find { parent -> parent.codeId == result.text.toString().toLowerCase(Locale.ROOT) }?.let { parent ->

                                    context?.let { ctx ->

                                        val children = mEvents.filter { event ->
                                            event.femaleObsUnitDbId == parent.codeId || event.maleObsUnitDbId == parent.codeId
                                        }

                                        mBarcodeScanner.pause()

                                        Dialogs.list(
                                            AlertDialog.Builder(ctx),
                                            getString(R.string.click_item_for_child_details),
                                            getString(R.string.no_child_exists),
                                            children, { id ->

                                                findNavController()
                                                    .navigate(BarcodeScanFragmentDirections
                                                        .actionFromScanToEventDetail(id))
                                            }) {

                                            mBarcodeScanner.resume()
                                        }
                                    }
                                }

                            } else {

                                findNavController().navigate(BarcodeScanFragmentDirections
                                    .actionToEventFragmentFromScan(scannedEvent.id ?: -1L))
                            }
                        }
                        CONTINUOUS -> {
                            zxingBarcodeScanner.setStatusText(getString(R.string.zxing_scan_mode_continuous))

                            val maleFirst = mPrefs.getBoolean(mKeyUtil.nameCrossOrderKey, false)

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

            arguments?.let {

                when (it.getInt("mode")) {

                    SEARCH -> decodeContinuous(mCallback)

                    else -> decodeSingle(mCallback)
                }
            }
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

        val isCommutative = mPrefs.getBoolean(mKeyUtil.workCommutativeKey, false)

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

        metadataViewModel.metadata.observe(viewLifecycleOwner) {
            mMetadata = ArrayList(it)
        }

        viewModel.events.observe(viewLifecycleOwner, {
            it?.let {
                mEvents = ArrayList(it)
            }
        })

        parentsModel.parents.observe(viewLifecycleOwner) {
            mParents = ArrayList(it)
        }

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
                                activity,
                                female,
                                male,
                                cross,
                                mSettings,
                                settingsModel,
                                viewModel,
                                mParents,
                                parentsModel,
                                mWishlist,
                                mMetadata,
                                metaValuesViewModel)
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

        (activity as MainActivity).supportActionBar?.show()
    }

    override fun onPause() {
        super.onPause()

        mBarcodeScanner.pause()
    }

}
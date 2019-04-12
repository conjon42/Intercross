package org.phenoapps.intercross

import android.content.ContentValues
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.*

class GroupPrintActivity : AppCompatActivity() {

    private val mDbHelper: IntercrossDbHelper = IntercrossDbHelper(this)

    private val imageView: ImageView by lazy {
        findViewById<ImageView>(R.id.imageView)
    }
    private val mAddButton: Button by lazy {
        findViewById<Button>(R.id.addButton)
    }
    private val mRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.recyclerView)
    }
    private val mEditText: EditText by lazy {
        findViewById<EditText>(R.id.editText3)
    }
    private lateinit var mGroup: String

    private var animationEnd = true

    private lateinit var mNavView: NavigationView

    private val mEntries = ArrayList<AdapterEntry>()

    private val mAdapter: ViewAdapter<AdapterEntry> = object : ViewAdapter<AdapterEntry>(mEntries) {

        override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
            return R.layout.simple_row
        }

        override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(view)
        }
    }

    inner class ViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView), ViewAdapter.Binder<AdapterEntry> {

        private var firstText: TextView = itemView.findViewById(R.id.maleTextView) as TextView

        override fun bind(data: AdapterEntry) {

            firstText.text = data.first
        }
    }

    override fun onStart() {
        super.onStart()

        imageView.setOnClickListener {
            val names = mDbHelper.getNamesByGroup(mGroup)

            //TODO test printing
            //send print job to bluetooth device
            BluetoothUtil()
                    .variablePrint(this@GroupPrintActivity, "^XA"
                            + "^MNA"
                            + "^MMT,N"
                            + "^DFR:DEFAULT_INTERCROSS_SAMPLE.GRF^FS"
                            + "^FWR"
                            + "^FO100,25^A0,25,20^FN1^FS"
                            + "^FO200,25^A0N,25,20"
                            + "^BQ,2,6" +
                            "^FN2^FS"
                            + "^FO450,25^A0,25,20^FN3^FS^XZ",
                            "^XA"
                                    + "^XFR:DEFAULT_INTERCROSS_SAMPLE.GRF"
                                    + "^FN1^FD$names^FS"
                                    + "^FN2^FDQA,$names^FS^XZ")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        title = "Pollen Manager"

        setContentView(R.layout.activity_add_group_ids)

        supportActionBar?.let {
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        mNavView = findViewById(R.id.nvView)

        // Setup drawer view
        setupDrawer()

        if (intent.hasExtra("group")) {
            mGroup = intent.getStringExtra("group")
        }

        mRecyclerView.adapter = mAdapter

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mAddButton.setOnClickListener {
            val value = mEditText.text.toString()

            if (value.isNotEmpty() && (mEntries.filter { it.first == value }).isEmpty()) {
                var ids = ArrayList<String>()
                mEntries.forEach { entry -> ids.add(entry.first) }
                ids.add(value)
                mDbHelper.insertFauxId(mGroup, ContentValues().apply {
                    put("names", ids.joinToString(","))
                })
                mEntries.clear()

                val names = mDbHelper.getNamesByGroup(mGroup)
                if (names.contains(",")) {
                    names.split(",").forEach {
                        mEntries.add(AdapterEntry(it))
                    }
                } else if (names.isNotBlank()) mEntries.add(AdapterEntry(names))

                updateBitmap(names)

                mEditText.text.clear()

                mAdapter.notifyDataSetChanged()
            }
        }

        val names = mDbHelper.getNamesByGroup(mGroup)
        if (names.contains(",")) {
            names.split(",").forEach {
                if (it.isNotBlank()) mEntries.add(AdapterEntry(it))
            }
        } else if (names.isNotBlank()) mEntries.add(AdapterEntry(names))
        updateBitmap(names)

        mAdapter.notifyDataSetChanged()
    }

    private fun updateBitmap(names: String) {
        try {

            val text = names
            if (text.isNotBlank()) {
                val bitMatrix = when {
                    imageView.width == 0 -> {
                        MultiFormatWriter().encode(text,
                                BarcodeFormat.QR_CODE, 100, 100)
                    }
                    else -> MultiFormatWriter().encode(text,
                            BarcodeFormat.QR_CODE, imageView.width, imageView.height)
                }
                val bitmap = BarcodeEncoder().createBitmap(bitMatrix)
                imageView.setImageBitmap(bitmap)
            }
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun setupDrawer() {
        val dl = findViewById<DrawerLayout>(org.phenoapps.intercross.R.id.drawer_layout)
        dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            //else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

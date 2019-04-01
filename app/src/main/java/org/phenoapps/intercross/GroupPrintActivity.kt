package org.phenoapps.intercross

import android.animation.Animator
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import java.util.*

class GroupPrintActivity : AppCompatActivity() {

    private val mDbHelper: IntercrossDbHelper = IntercrossDbHelper(this)

    private val imageView: ImageView by lazy {
        findViewById<ImageView>(R.id.imageView)
    }
    private val msgImageView: ImageView by lazy {
        findViewById<ImageView>(R.id.msgImageView)
    }
    private val mPrintButton: Button by lazy {
        findViewById<Button>(R.id.printButton)
    }
    private val mAddButton: Button by lazy {
        findViewById<Button>(R.id.addButton)
    }
    private val mRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.recyclerView)
    }

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

        private var firstText: TextView = itemView.findViewById(R.id.crossTextView) as TextView

        override fun bind(data: AdapterEntry) {

            firstText.text = data.first
        }
    }

    private fun askUserForName() {

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val builder = AlertDialog.Builder(this).apply {

            setView(input)

            setPositiveButton("OK") { _, _ ->
                val value = input.text.toString()
                if (value.isNotEmpty()) {
                    mEntries.add(AdapterEntry(value))
                    mAdapter.notifyDataSetChanged()
                    mDbHelper.insertFauxId(ContentValues().apply {
                        put("group", value)
                    })
                }
            }
        }

        builder.setTitle("Add a name for group pollen id.")
        builder.show()

    }

    override fun onStart() {
        super.onStart()

        /*mEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                try {
                    val text = mEditText.text.toString()
                    if (text.isNotBlank()) {
                        val bitMatrix = MultiFormatWriter().encode(text,
                                BarcodeFormat.QR_CODE, imageView.measuredWidth, imageView.measuredHeight)
                        val bitmap = BarcodeEncoder().createBitmap(bitMatrix)
                        imageView.setImageBitmap(bitmap)
                    }
                } catch (e: WriterException) {
                    e.printStackTrace()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })*/

        mPrintButton.setOnClickListener {

            if (mAdapter.listItems.isNotEmpty()) {

                //start animation to fling a letter
                if (animationEnd && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    animationEnd = false

                    msgImageView.visibility = View.VISIBLE

                    (msgImageView).animate()
                            .translationXBy(200.0f)
                            .rotationBy(360f)
                            .scaleX(2.0f)
                            .scaleY(2.0f)
                            .setDuration(500)
                            .alpha(0f)
                            .setListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(p0: Animator?) {
                                }

                                override fun onAnimationEnd(p0: Animator?) {
                                    msgImageView.visibility = View.INVISIBLE
                                    (msgImageView).animate()
                                            .translationXBy(-200.0f)
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(0)
                                            .setListener(object : Animator.AnimatorListener {
                                                override fun onAnimationRepeat(p0: Animator?) {
                                                }

                                                override fun onAnimationEnd(p0: Animator?) {
                                                    animationEnd = true
                                                }

                                                override fun onAnimationCancel(p0: Animator?) {
                                                }

                                                override fun onAnimationStart(p0: Animator?) {
                                                }

                                            })
                                            .alpha(1f)

                                }

                                override fun onAnimationCancel(p0: Animator?) {
                                }

                                override fun onAnimationStart(p0: Animator?) {
                                    //msgImageView.visibility = View.VISIBLE
                                }

                            })
                }

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
                                        + "^FN1^FD" + "A" + "^FS"
                                        + "^FN2^FDQA," + "B" + "^FS^XZ")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        title = "Pollen Manager"

        setContentView(R.layout.activity_simple_print_old)

        supportActionBar?.let {
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        mNavView = findViewById(R.id.nvView)

        // Setup drawer view
        setupDrawer()

        mRecyclerView.adapter = mAdapter

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mAddButton.setOnClickListener {
            askUserForName()
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

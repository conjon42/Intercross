package org.phenoapps.intercross

import android.content.ContentValues
import android.content.Intent
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

class PollenManager : AppCompatActivity() {

    private val mDbHelper: IntercrossDbHelper = IntercrossDbHelper(this)

    private val mAddButton: Button by lazy {
        findViewById<Button>(R.id.addButton)
    }
    private val mRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.recyclerView)
    }
    private val mEditText: EditText by lazy {
        findViewById<EditText>(R.id.editText)
    }
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

        init {
            itemView.setOnClickListener {
                startActivity(Intent(this@PollenManager, GroupPrintActivity::class.java).apply {
                    putExtra("group", firstText.text.toString())
                })
            }
        }

        override fun bind(data: AdapterEntry) {

            firstText.text = data.first
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        title = "Pollen Manager"

        setContentView(R.layout.activity_manage_pollen)

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
            val value = mEditText.text.toString()
            if (value.isNotEmpty()) {
                //mEntries.add(AdapterEntry(value))
                //mAdapter.notifyDataSetChanged()
                mDbHelper.insertFauxId(value, ContentValues().apply {
                    put("g", value)
                })
                mEntries.clear()
                mDbHelper.getGroups().forEach {
                    mEntries.add(AdapterEntry(it))
                }
                mAdapter.notifyDataSetChanged()
                mEditText.text.clear()
            }
        }

        mDbHelper.getGroups().forEach {
            mEntries.add(AdapterEntry(it))
        }

        mAdapter.notifyDataSetChanged()
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

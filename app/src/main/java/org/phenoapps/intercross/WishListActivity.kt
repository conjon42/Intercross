package org.phenoapps.intercross

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import org.phenoapps.intercross.IntercrossActivity.Companion.CROSS_ID
import java.util.*

class WishListActivity : AppCompatActivity() {

    private lateinit var mRecyclerView: RecyclerView

    private val mEntries = ArrayList<AdapterEntry>()

    private val mAdapter = object : ViewAdapter<AdapterEntry>(mEntries) {

        override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
            return R.layout.wish_cross_row
        }

        override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(view)
        }

    }

    private val mDbHelper = IntercrossDbHelper(this)

    private lateinit var mNavView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Wish List"

        setContentView(R.layout.activity_count)

        supportActionBar?.let {
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        mNavView = findViewById(R.id.nvView)

        // Setup drawer view
        setupDrawer()

        mRecyclerView = findViewById(R.id.recyclerView)

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mRecyclerView.adapter = mAdapter


        val result = mDbHelper.getParentCounts()
        //sort
        result.sortBy { it.third }
        //remove dups
        //result.distinctBy {it.first + it.second}.
        result.asReversed().forEach {
            var unique = true
            mEntries.forEach { entry ->
                if (entry.first == it.first && entry.second == it.second) {
                    unique = false
                }
            }
            if (unique) mEntries.add(it)
        }
    }

    inner class InnerViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
            ViewAdapter.Binder<AdapterEntry> {

        private var firstText: TextView = itemView.findViewById(R.id.maleTextView) as TextView

        init {
            itemView.setOnClickListener {
                val crossName = firstText.text.toString()
                startActivity(Intent(this@WishListActivity, CrossActivity::class.java).apply {
                    putExtra(CROSS_ID, crossName)
                })
            }
        }

        override fun bind(data: AdapterEntry) {
            firstText.text = data.first
        }
    }

    private fun setupDrawer() {
        val dl = findViewById<DrawerLayout>(R.id.drawer_layout)
        dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            //else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
            ViewAdapter.Binder<AdapterEntry>, View.OnClickListener {

        private var firstText: TextView = itemView.findViewById(R.id.maleTextView) as TextView
        private var secondText: TextView = itemView.findViewById(R.id.femaleTextView) as TextView
        private var countText: TextView = itemView.findViewById(R.id.thirdTextView) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(data: AdapterEntry) {

            firstText.text = data.first
            secondText.text = data.second
            countText.text = data.third
        }

        override fun onClick(v: View) {

            val builder = AlertDialog.Builder(this@WishListActivity)
            builder.setTitle("Crosses")
            val layout = RecyclerView(this@WishListActivity)
            builder.setView(layout)
            val crosses = mDbHelper.getCrosses(firstText.text.toString(),
                    secondText.text.toString())
            val entries = ArrayList<AdapterEntry>()
            crosses.forEach {
                entries.add(AdapterEntry(it, ""))
            }
            val adapter: ViewAdapter<AdapterEntry> = object : ViewAdapter<AdapterEntry>(entries) {
                override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
                    return R.layout.simple_row
                }

                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                    return InnerViewHolder(view)
                }
            }
            layout.adapter = adapter
            layout.layoutManager = LinearLayoutManager(this@WishListActivity)
            builder.show()
        }
    }
}
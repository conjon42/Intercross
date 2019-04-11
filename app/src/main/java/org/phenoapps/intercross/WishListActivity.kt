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

    private val mWishCountTextView by lazy { findViewById<TextView>(R.id.textView) }

    private val mEntries = ArrayList<AdapterEntry>()

    private val mAdapter = object : ViewAdapter<AdapterEntry>(mEntries) {

        override fun getLayoutId(position: Int, obj: AdapterEntry): Int = when {

            obj.first.isNotEmpty() && obj.second.isNotEmpty() && obj.third.isNotEmpty() -> {
                R.layout.wish_cross_complete
            }
            else -> {
                R.layout.wish_cross_incomplete
            }
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

        setContentView(R.layout.activity_wish_list)

        supportActionBar?.let {
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        //mNavView = findViewById(R.id.nvView)

        // Setup drawer view
        //setupDrawer()

        mRecyclerView = findViewById(R.id.recyclerView)

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mRecyclerView.adapter = mAdapter


        val result = mDbHelper.getWishList()
        var wishCount = 0
        var completedCount = 0
        result.forEach { wishcross ->
            if (wishcross.size == 5) {
                val femaleID = wishcross[0] as String
                val femaleName = wishcross[1] as String
                val maleID = wishcross[2] as String
                val maleName = wishcross[3] as String
                val numCrosses = wishcross[4] as Int
                val crosses = mDbHelper.getCrosses(femaleID, maleID)
                wishCount += numCrosses
                //if (crosses.isNotEmpty()) {
                //    wishCount++
                completedCount += crosses.size % numCrosses //TODO check if modular div is what we want
                mEntries.add(AdapterEntry(femaleName, maleName, "${crosses.size}/$numCrosses"))
                //} else {
                //    mEntries.add(AdapterEntry(wishcross[1], wishcross[3]))
                //}
            }
        }
        mWishCountTextView.text = "${completedCount}/${wishCount}crosses"
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
            ViewAdapter.Binder<AdapterEntry> {


        override fun bind(data: AdapterEntry) {

            if (data.first.isNotEmpty() && data.second.isNotEmpty()) {
                itemView.findViewById<TextView>(R.id.textView).text = data.first
                itemView.findViewById<TextView>(R.id.textView1).text = data.second
            }
            if (data.third.isNotEmpty()) {
                itemView.findViewById<TextView>(R.id.textView2).text = data.third
            }
        }
    }
}
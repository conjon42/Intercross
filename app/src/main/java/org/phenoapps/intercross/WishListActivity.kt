package org.phenoapps.intercross

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import org.phenoapps.intercross.IntercrossActivity.Companion.CROSS_ID
import java.util.*

class WishListActivity : AppCompatActivity() {

    private lateinit var mRecyclerView: RecyclerView

    private val mWishCountTextView by lazy { findViewById<TextView>(R.id.textView) }

    private val mMaleEditText by lazy { findViewById<TextView>(R.id.editText2) }

    private val mFemaleEditText by lazy { findViewById<TextView>(R.id.editText3) }

    private val mEntries = ArrayList<AdapterEntry>()

    private val mAdapter = object : ViewAdapter<AdapterEntry>(mEntries) {

        override fun getLayoutId(position: Int, obj: AdapterEntry): Int = R.layout.wish_cross_complete




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


        loadOverview()

        arrayOf(mFemaleEditText, mMaleEditText).forEach {
            it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val f = mFemaleEditText.text.toString()
                    val m = mMaleEditText.text.toString()

                    //Toast.makeText(this@WishListActivity, "Not working yet :)", Toast.LENGTH_LONG).show()
                    val crosses = mDbHelper.getCrosses(f,m)
                    if (crosses.isEmpty()) {
                        loadOverview()
                    } else {
                        val size = mEntries.size
                        mEntries.clear()
                        mAdapter.notifyItemRangeRemoved(0,size)
                        crosses.forEach {entry ->
                            mEntries.add(AdapterEntry(entry))
                            mAdapter.notifyItemInserted(mEntries.size - 1)
                        }
                    }
                }
            })
        }
    }

    private fun loadOverview() {
        mEntries.clear()
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
                val crosses = mDbHelper.getCrosses(femaleName, maleName)
                wishCount += numCrosses
                //if (crosses.isNotEmpty()) {
                //    wishCount++
                completedCount += Math.min(crosses.size, numCrosses)
                mEntries.add(AdapterEntry(femaleName, maleName, "${crosses.size}/$numCrosses"))
                //} else {
                //    mEntries.add(AdapterEntry(wishcross[1], wishcross[3]))
                //}
            }
        }
        mAdapter.notifyDataSetChanged()
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
            val x = mDbHelper.getRowId(data.first)
            //mDbHelper.getTimestampById(x)

            /*itemView.findViewById<ImageView>(R.id.crossTypeImageView)
                    .setImageDrawable(when (mDbHelper.getPollinationType(x)) {
                        "Self-Pollinated" -> ContextCompat.getDrawable(this@IntercrossActivity,
                                R.drawable.ic_cross_self)
                        "Biparental" -> ContextCompat.getDrawable(this@IntercrossActivity,
                                R.drawable.ic_cross_biparental)
                        else -> ContextCompat.getDrawable(this@IntercrossActivity,
                                R.drawable.ic_cross_open_pollinated)
                    })*/
            itemView.findViewById<TextView>(R.id.textView).text = ""
            itemView.findViewById<TextView>(R.id.textView1).text = ""
            itemView.findViewById<TextView>(R.id.textView2).text = ""

            if (data.first.isNotEmpty()) {
                itemView.findViewById<TextView>(R.id.textView).text = data.first
            }
            if (data.second.isNotEmpty()) {
                itemView.findViewById<TextView>(R.id.textView1).text = data.second
            }
            if (data.third.isNotEmpty()) {
                itemView.findViewById<TextView>(R.id.textView2).text = data.third
            }
        }
    }
}
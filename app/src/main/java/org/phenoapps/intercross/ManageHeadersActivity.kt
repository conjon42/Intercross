package org.phenoapps.intercross

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import java.util.*

class ManageHeadersActivity : AppCompatActivity() {

    private lateinit var mButton: Button
    private lateinit var mEditText: EditText
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mSubmitButton: Button

    private val mEntries = ArrayList<String>()

    private val mAdapter: ViewAdapter<String> = object : ViewAdapter<String>(mEntries) {

        override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(view)
        }

        override fun getLayoutId(position: Int, obj: String): Int {
            return R.layout.row
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_headers)

        val dbHelper = IdEntryDbHelper(this)

        val userHeaders = dbHelper.getColumns() - IdEntryContract.IdEntry.COLUMNS

        dbHelper.close()

        userHeaders.forEach { header ->
            mEntries.add(header)
        }

        mSubmitButton = findViewById(R.id.submitButton) as Button
        mButton = findViewById(R.id.addHeaderButton) as Button
        mEditText = findViewById(R.id.editTextHeader) as EditText
        mRecyclerView = findViewById(R.id.recyclerView) as RecyclerView

        mRecyclerView.adapter = mAdapter

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mButton.setOnClickListener {

            val newHeaderName = mEditText.text.toString()

            if (!mEntries.contains(newHeaderName)) {

                mEntries.add(newHeaderName)

                mRecyclerView.adapter.notifyDataSetChanged()

                mEditText.setText("")

            } else {
                Toast.makeText(this@ManageHeadersActivity,
                        "Header already exists in table.", Toast.LENGTH_LONG).show()
            }
        }

        mSubmitButton.setOnClickListener {

            val intent = Intent()
            intent.putExtra(IntercrossConstants.HEADERS, mEntries)
            setResult(RESULT_OK, intent)
            finish()
        }

    }

    inner class ViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView), ViewAdapter.Binder<String>,
            View.OnClickListener {

        private var firstText: TextView = itemView.findViewById(R.id.firstTextView) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(data: String) {
            firstText.text = data
        }

        override fun onClick(v: View?) {

            val removedCol = ((v as LinearLayout).getChildAt(0) as TextView).text

            val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

            val headerSet = prefs.getStringSet(SettingsActivity.HEADER_SET, HashSet())

            headerSet.remove(removedCol)

            val editor = prefs.edit()

            editor.putStringSet(SettingsActivity.HEADER_SET, headerSet)

            editor.apply()

            mEntries.remove(removedCol)

            mAdapter.notifyDataSetChanged()

        }
    }
}

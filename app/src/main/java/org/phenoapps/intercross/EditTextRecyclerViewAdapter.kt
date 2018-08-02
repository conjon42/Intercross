package org.phenoapps.intercross

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView

import java.util.ArrayList
import java.util.HashSet

class EditTextRecyclerViewAdapter internal constructor(private val mContext: Context, private val mData: List<EditTextAdapterEntry>) : RecyclerView.Adapter<EditTextRecyclerViewAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater
    private val db: SQLiteDatabase

    init {
        this.mInflater = LayoutInflater.from(mContext)
        this.db = IdEntryDbHelper(mContext).writableDatabase
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = mInflater.inflate(R.layout.value_input_row, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        //EditTextAdapterEntry entry = mData.get(position);

        holder.editText.tag = position

        holder.editText.setText(mData[position].editText.text.toString())

        holder.headerValue.text = mData[position].headerTextValue
    }

    override fun getItemCount(): Int {
        return mData.size
    }


    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var editText: EditText
        internal var headerValue: TextView
        internal var crossId: String? = null

        init {
            headerValue = itemView.findViewById(R.id.headerTextValue) as TextView
            editText = itemView.findViewById(R.id.headerValueEditText) as EditText
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    if (editText.tag != null) {
                        mData[editText.tag as Int].editText.setText(charSequence.toString())
                        //mData.set((int)editText.getTag(), charSequence.toString());
                    }
                }

                override fun afterTextChanged(editable: Editable) {

                }
            })
        }
    }

    companion object {

        private val mEditTextValues = ArrayList<EditTextAdapterEntry>()
    }
}
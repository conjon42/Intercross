package org.phenoapps.intercross

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class HeaderRecyclerViewAdapter internal constructor(context: Context, private val mData: List<String>) : RecyclerView.Adapter<HeaderRecyclerViewAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    init {
        this.mInflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.header_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = mData[position]
        holder.headerId.text = entry
    }

    override fun getItemCount(): Int {
        return mData.size
    }


    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var headerId: TextView

        init {
            headerId = itemView.findViewById(R.id.headerId) as TextView
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
            Log.d("CLICK", "C")

        }
    }

    // convenience method for getting data at click position
    internal fun getItem(id: Int): String {
        return mData[id]
    }

    // allows clicks events to be caught
    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
        fun onLongItemClick(v: View, position: Int)
    }
}
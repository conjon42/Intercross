package org.phenoapps.intercross

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

abstract class ViewAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    var listItems: List<T>

    constructor(listItems: List<T>) {
        this.listItems = listItems
    }

    fun setItems(listItems: List<T>) {
        this.listItems = listItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return getViewHolder(LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)
                , viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as Binder<T>).bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutId(position, listItems[position])
    }

    protected abstract fun getLayoutId(position: Int, obj: T): Int

    abstract fun getViewHolder(view: View, viewType: Int):RecyclerView.ViewHolder

    internal interface Binder<T> {
        fun bind(data: T)
    }
}
package org.phenoapps.intercross

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleObserver
import java.io.File

class ImportZPL : AppCompatActivity(), LifecycleObserver {

    private val mCodeTextView: TextView by lazy {
        findViewById<TextView>(R.id.codeTextView)
    }

    private val mImportButton: Button by lazy {
        findViewById<Button>(R.id.importButton)
    }

    override fun onStart() {

        super.onStart()

        supportActionBar?.let {
            it.title = ""
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        //import a file when button is pressed
        mImportButton.setOnClickListener {
            startActivityForResult(
                    Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                    }, "Choose file to import."), IntercrossConstants.IMPORT_ZPL)
        }

        //set preview text to imported zpl code
        val code = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("ZPL_CODE", "")

        if (code.isNotBlank()) mCodeTextView.text = code
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_import_zpl)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //finish activity when back button is pressed
        finish()

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        super.onActivityResult(requestCode, resultCode, intent)

        intent?.let {
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    IntercrossConstants.IMPORT_ZPL -> {

                        val text = readText(intent.data)
                        mCodeTextView.text = text

                        val edit = PreferenceManager.getDefaultSharedPreferences(this).edit()
                        edit.putString("ZPL_CODE", text.toString())
                        edit.apply()
                    }
                }
            }
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("Security", "Permission is granted")
                return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), IntercrossConstants.REQUEST_WRITE_PERMISSION)
            }
        } else
            return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

        return false
    }

    private fun readText(uri: Uri?): CharSequence {
        uri?.let {
            if (isExternalStorageWritable()) {
                val lines = File(getPath(uri)).readLines()
                return lines.joinToString("\n")
            }
        }
        return String()
    }

    override fun onBackPressed() {
        //do nothing
    }

    private fun getPath(uri: Uri): String? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            if (DocumentsContract.isDocumentUri(this@ImportZPL, uri)) {

                if ("com.android.externalstorage.documents" == uri.authority) {
                    val doc = DocumentsContract.getDocumentId(uri).split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val documentType = doc[0]

                    if ("primary".equals(documentType, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + doc[1]
                    }
                } else if ("com.android.providers.downloads.documents" == uri.authority) {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (!id.isEmpty()) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:".toRegex(), "")
                        }
                    }
                    val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                    return getDataColumn(this@ImportZPL, contentUri, null, null)
                }
            } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
                return uri.path
            } else if ("com.estrongs.files" == uri.authority) {
                return uri.path
            }
        }
        return null
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }
}

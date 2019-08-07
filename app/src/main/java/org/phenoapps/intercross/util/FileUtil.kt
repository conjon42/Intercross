package org.phenoapps.intercross.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.phenoapps.intercross.fragments.SettingsFragment
import java.io.*

class FileUtil(val ctx: Context) {

    fun ringNotification(success: Boolean) {

        if (PreferenceManager.getDefaultSharedPreferences(ctx)
                        .getBoolean(SettingsFragment.AUDIO_ENABLED, false)) {
            try {
                when (success) {
                    true -> {
                        val chimePlayer = MediaPlayer.create(ctx, ctx.resources.getIdentifier("plonk", "raw", ctx.packageName))
                        chimePlayer.start()
                        chimePlayer.setOnCompletionListener {
                            chimePlayer.release()
                        }
                    }
                    false -> {
                        val chimePlayer = MediaPlayer.create(ctx, ctx.resources.getIdentifier("error", "raw", ctx.packageName))
                        chimePlayer.start()
                        chimePlayer.setOnCompletionListener {
                            chimePlayer.release()
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    private fun getPath(uri: Uri?): String {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            when {
                DocumentsContract.isDocumentUri(ctx, uri) -> {
                    return when (uri?.authority) {
                        "com.android.externalstorage.documents" -> {
                            val docId = DocumentsContract.getDocumentId(uri).split(":")
                            if (docId.isNotEmpty() && "primary" == docId[0].toLowerCase()) {
                                "${Environment.getExternalStorageDirectory()}/${docId[1]}"
                            } else ""
                        }
                        "com.android.providers.downloads.documents" -> {
                            val docId = DocumentsContract.getDocumentId(uri)
                            if (docId.isNotEmpty() && docId.startsWith("raw:")) {
                                docId.replaceFirst("raw:", "")
                            } else ""
                        }
                        else -> ""
                    }
                }
                "file" == (uri?.scheme ?: "").toLowerCase() -> {
                    return uri?.path ?: ""
                }
            }
        }
        return String()
    }

    fun parseUri(uri: Uri): List<String> {

        val fileUri = uri.path ?: ""
       /* val fileName =
                if (fileUri.lastIndexOf('/') != -1) {
                    fileUri.substring(fileUri.lastIndexOf('/') + 1)
                } else ""*/
        val filePath = FileUtil(ctx).getPath(uri)
        val lastDot = fileUri.lastIndexOf(".")
        return when (fileUri.substring(lastDot + 1)) {
            "xlsx", "xls" -> {
                parseExcelSheet(filePath)
            }
            "tsv" -> {
                parseTextFile(uri, "\t")
            }
            "csv", "txt" -> {
                parseTextFile(uri, ",")
            }
            else -> ArrayList()
        }

    }

    private fun parseExcelSheet(filePath: String): List<String> {
        val workbook = WorkbookFactory.create(File(filePath))
        return if (workbook.numberOfSheets > 0) {
            workbook.getSheetAt(0).rowIterator().asSequence().toList().map { it ->
                it.cellIterator().asSequence().joinToString(",")
            }
        } else ArrayList()
    }

    private fun parseTextFile(it: Uri, delim: String): List<String> {
        var ret = ArrayList<String>()
        try {
            val stream = ctx.contentResolver.openInputStream(it)
            stream?.let {
                val reader = BufferedReader(InputStreamReader(it))
                ret = ArrayList(reader.readLines())
                //val headers = reader.readLine().split(delim)

                /*if (headers.size == 4) {
                    /*reader.readLines().forEachIndexed { index, line ->
                        //if (index > 0) {
                        val row = line.split(delim)
                        if (row.isNotEmpty() && row.size == 4) {
                            mParentsViewModel.addParents(row[0], row[1], row[2], row[3])
                        }
                    }*/
                }*/
            }
        } catch (fo: FileNotFoundException) {
            fo.printStackTrace()
        } catch (io: IOException) {
            io.printStackTrace()
        }
        return ret
    }

}
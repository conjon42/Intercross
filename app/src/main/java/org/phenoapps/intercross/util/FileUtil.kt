package org.phenoapps.intercross.util

//import org.apache.poi.ss.usermodel.WorkbookFactory
import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.PollenGroup
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.fragments.SettingsFragment
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

class FileUtil(private val ctx: Context) {

    /**
     * Parent's input file header fields. These are queried from the strings XML.
     */
    private val parentsCodeIdHeader: String by lazy { ctx.getString(R.string.parents_header_code_id) }

    private val parentsNameHeader: String by lazy { ctx.getString(R.string.parents_header_readable_name) }

    private val parentsSexHeader: String by lazy { ctx.getString(R.string.parents_header_sex) }

    /**
     * Wishlist input file header fields. These are queried from the strings XML.
     */
    private val wishlistFemaleIdHeader: String by lazy { ctx.getString(R.string.wishlist_header_female_id) }

    private val wishlistMaleIdHeader: String by lazy { ctx.getString(R.string.wishlist_header_male_id) }

    private val wishlistMaleNameHeader: String by lazy { ctx.getString(R.string.wishlist_header_male_name) }

    private val wishlistFemaleNameHeader: String by lazy { ctx.getString(R.string.wishlist_header_female_name) }

    private val wishlistTypeHeader: String by lazy { ctx.getString(R.string.wishlist_header_type) }

    private val wishlistMinHeader: String by lazy { ctx.getString(R.string.wishlist_header_min) }

    private val wishlistMaxHeader: String by lazy { ctx.getString(R.string.wishlist_header_max) }


    /**
     * Cross table export file header fields.
     */

    private val crossIdHeader: String by lazy { ctx.getString(R.string.crosses_export_id_header) }

    private val crossMomHeader: String by lazy { ctx.getString(R.string.crosses_export_mom_header) }

    private val crossDadHeader: String by lazy { ctx.getString(R.string.crosses_export_dad_header) }

    private val crossTimestampHeader: String by lazy { ctx.getString(R.string.crosses_export_date_header) }

    private val crossPersonHeader: String by lazy { ctx.getString(R.string.crosses_export_person_header) }

    private val crossExperimentHeader: String by lazy { ctx.getString(R.string.crosses_export_experiment_header) }

    private val crossTypeHeader: String by lazy { ctx.getString(R.string.crosses_export_type_header) }

    private val crossFruitsHeader: String by lazy { ctx.getString(R.string.crosses_export_fruits_header) }

    private val crossFlowersHeader: String by lazy { ctx.getString(R.string.crosses_export_flowers_header) }

    private val crossSeedsHeader: String by lazy { ctx.getString(R.string.crosses_export_seeds_header) }

    private val eventModelHeaderString by lazy {
        arrayOf(crossIdHeader, crossMomHeader, crossDadHeader,
                crossTimestampHeader, crossPersonHeader, crossExperimentHeader,
                crossTypeHeader, crossFruitsHeader, crossFlowersHeader, crossSeedsHeader)
                .joinToString(",")
    }

    /***
     * Main parse driver. Either a parents or wishlist file can be loaded.
     * Parents files populate a table of barcodeIds with readable names.
     *  --Parents table is specifically used to print barcodes that don't exist in the crosses table (CIP)
     *  --But this table is also updated during normal cross entry. (Parents are inserted from data entry)
     * The wishlist table contains rows of samples to be crossed with minimum and maximum requirements
     *
     * This driver function parses a text file and determines the input file type.
     * If a wishType column is detected, it is assumed to be a wishlist file.
     *
     * TODO Localizations must require that the parents input file header should not contain the wishlist type header.
     */
    fun parseInputFile(uri: Uri): Pair<List<Parent>, List<Wishlist>> {

        val wishlist = ArrayList<Wishlist>()

        val parents = ArrayList<Parent>()

        val lines = parseTextFile(uri)

        if (lines.isNotEmpty()) {

            val headers = lines[0].split(",").map { it -> it.replace(" ", "") }

            //ensure the headers size > 0
            if (headers.isNotEmpty()) {

                if (headers.find { it == wishlistTypeHeader }.isNullOrBlank()) {

                    //import parents file
                    loadParents(headers, lines-lines[0], parents)

                } else {

                    loadWishlist(headers, lines-lines[0], wishlist, parents)

                }
            }

        }

        return parents to wishlist
    }


    //Name's must be to specification / order doesn't matter
    //Barcodes required, names copy to barcode

    /**
     * Input File expected format:
     *      header1, header2
     *      code1,  readableName
     *      code2, readableName2,
     *      ...
     *
     * Where header1 and header2 are string texts defined in XML (to be translated if necessary)
     *
     */
    //TODO Low-priority: switch to yield/iterator to reduce heap usage
    private fun loadParents(headers: List<String>,
                            lines: List<String>,
                            parents: ArrayList<Parent>) {

        //the headers must include at least the code id header
        if (!headers.find { it == parentsCodeIdHeader }.isNullOrBlank()) {

            val headerToIndex = headers
                    .mapIndexed { index, s -> s to index }
                    .toMap()

            lines.forEach { it ->

                val row = it.split(",").map { it.trim() }

                //if the rowsize is 2 it must be the parents code id and sex
                if (row.size == 2 && parentsCodeIdHeader in headerToIndex.keys
                        && parentsSexHeader in headerToIndex.keys) {

                    headerToIndex[parentsCodeIdHeader]?.let { codeKey ->

                        headerToIndex[parentsSexHeader]?.let { sexKey ->

                            parents.add(Parent(row[codeKey], row[sexKey].toInt()))
                        }
                    }

                } else if (row.size == 3) {

                    //ensure code id exists
                    headerToIndex[parentsCodeIdHeader]?.let { key ->

                        headerToIndex[parentsSexHeader]?.let { sexKey ->

                            val readableNameIndex = headerToIndex[parentsNameHeader]

                            if (readableNameIndex == null) {

                                parents.add(
                                        Parent(row[key], row[sexKey].toInt())
                                )

                            } else {

                                parents.add(
                                        Parent(
                                                row[key],
                                                row[sexKey].toInt(),
                                                row[readableNameIndex])
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This function returns True if the required headers exist in the input file
     */
    private fun validateHeaders(headers: List<String>, ensured: List<String>): Boolean {

        return headers.intersect(ensured).size == ensured.size
    }

    //TODO Low-priority: switch to yield/iterator
    private fun loadWishlist(headers: List<String>,
                             lines: List<String>,
                             wishlist: ArrayList<Wishlist>,
                             parents: ArrayList<Parent>) {

        if (validateHeaders(headers, listOf(
                        wishlistFemaleIdHeader,
                        wishlistMaleIdHeader,
                        wishlistTypeHeader,
                        wishlistMinHeader))) {

            val headerIndices = headers
                    .mapIndexed { index, s -> s to index }
                    .toMap()

            lines.forEach { rawRow ->

                val row = rawRow.split(',').map { it.trim() }

                var readableMaleName: String? = null

                var readableFemaleName: String? = null

                var wishlistMax: Int? = null

                /*
                Try to parse all optional columns.
                 */
                if (wishlistMaleNameHeader in headerIndices) {

                    headerIndices[wishlistMaleNameHeader]?.let { key ->

                        readableMaleName = row[key]
                    }
                }

                if (wishlistFemaleNameHeader in headerIndices) {

                    headerIndices[wishlistFemaleNameHeader]?.let { key ->

                        readableFemaleName = row[key]
                    }
                }

                if (wishlistMaxHeader in headerIndices) {

                    headerIndices[wishlistMaxHeader]?.let { key ->

                        wishlistMax = row[key].toInt()
                    }
                }

                /**
                 * finally ensure that required columns exist, and add wishlists to ref array
                 */

                if (wishlistMaleIdHeader in headerIndices
                        && wishlistFemaleIdHeader in headerIndices
                        && wishlistTypeHeader in headerIndices
                        && wishlistMinHeader in headerIndices) {

                    headerIndices[wishlistMaleIdHeader]?.let { maleId ->

                        headerIndices[wishlistFemaleIdHeader]?.let { femaleId ->

                            headerIndices[wishlistTypeHeader]?.let { type ->

                                headerIndices[wishlistMinHeader]?.let { min ->

                                    wishlist.add(Wishlist(
                                            femaleDbId = row[femaleId],
                                            maleDbId = row[maleId],
                                            femaleName = readableFemaleName ?: row[femaleId],
                                            maleName = readableMaleName ?: row[maleId],
                                            wishType = row[type],
                                            wishMin = row[min].toInt(),
                                            wishMax = wishlistMax
                                    ))

                                    /**
                                     * Add all parents parsed from the wishlist
                                     */
                                    parents.add(Parent(row[femaleId], 0).apply {
                                        name = readableFemaleName ?: row[femaleId]
                                    })

                                    parents.add(Parent(row[maleId], 1).apply {
                                        name = readableMaleName ?: row[maleId]
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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

    fun exportCrossesToFile(uri: Uri, crosses: List<Event>, parents: List<Parent>, groups: List<PollenGroup>) {

        val newLine: ByteArray = System.getProperty("line.separator")?.toByteArray() ?: "\n".toByteArray()

        try {

            ctx.contentResolver.openOutputStream(uri).apply {

                this?.let {

                    write(eventModelHeaderString.toByteArray())

                    write(newLine)

                    crosses.forEachIndexed { index, cross ->

                        if (groups.any { g -> g.codeId == cross.maleObsUnitDbId }) {

                            val males = groups.filter { g -> g.codeId == cross.maleObsUnitDbId }
                                    .map { g ->
                                        parents.find { c -> c.id == g.maleId }.let {
                                            it?.name
                                        }
                                    }.joinToString(",", "{", "}")

                            write(cross.toPollenGroupString(males).toByteArray())

                            write(newLine)

                        } else {

                            write(cross.toString().toByteArray())

                            write(newLine)

                        }
                    }

                    close()
                }

            }

        } catch (exception: FileNotFoundException) {

            Log.e("IntFileNotFound", "Chosen uri path was not found: $uri")

        }

        MediaScannerConnection.scanFile(ctx, arrayOf(uri.path), arrayOf("*/*"), null)

    }

    //    fun parseUri(uri: Uri): List<String> {
    //
    //        val fileUri = uri.path ?: ""
    //       /* val fileName =
    //                if (fileUri.lastIndexOf('/') != -1) {
    //                    fileUri.substring(fileUri.lastIndexOf('/') + 1)
    //                } else ""*/
    //        val filePath = FileUtil(ctx).getPath(uri)
    //
    //        val lastDot = fileUri.lastIndexOf(".")
    //
    //        return when (fileUri.substring(lastDot + 1)) {
    //            "xlsx", "xls" -> {
    //                parseExcelSheet(filePath)
    //            }
    //            "tsv" -> {
    //                parseTextFile(uri, "\t")
    //            }
    //            "csv", "txt" -> {
    //                parseTextFile(uri, ",")
    //            }
    //            else -> ArrayList()
    //        }
    //
    //    }

        @WorkerThread
        fun getFilePath(context: Context, uri: Uri): String? = context.run {
            when {

                Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ->
                    getDataColumn(uri, null, null)

                else -> getPathKitkatPlus(uri)
            }
        }

        private fun Context.getPathKitkatPlus(uri: Uri): String? {
            when {
                DocumentsContract.isDocumentUri(applicationContext, uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    when {
                        uri.isExternalStorageDocument -> {
                            val parts = docId.split(":")
                            if ("primary".equals(parts[0], true)) {
                                return "${Environment.getExternalStorageDirectory()}/${parts[1]}"
                            }
                        }
                        uri.isDownloadsDocument -> {
                            val contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"),
                                    docId.toLong()
                            )
                            return getDataColumn(contentUri, null, null)
                        }
                        uri.isMediaDocument -> {
                            val parts = docId.split(":")
                            val contentUri = when (parts[0].toLowerCase(Locale.ROOT)) {
                                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                                else -> return null
                            }
                            return getDataColumn(contentUri, "_id=?", arrayOf(parts[1]))
                        }
                    }
                }
                "content".equals(uri.scheme, true) -> {
                    return if (uri.isGooglePhotosUri) {
                        uri.lastPathSegment
                    } else {
                        getDataColumn(uri, null, null)
                    }
                }
                "file".equals(uri.scheme, true) -> {
                    return uri.path
                }
            }
            return null
        }

        private fun Context.getDataColumn(uri: Uri, selection: String?, args: Array<String>?): String? {
            contentResolver?.query(uri, arrayOf("_data"), selection, args, null)?.use {
                if (it.moveToFirst()) {
                    return it.getString(it.getColumnIndexOrThrow("_data"))
                }
            }
            return null
        }

        private val Uri.isExternalStorageDocument: Boolean
            get() = authority == "com.android.externalstorage.documents"

        private val Uri.isDownloadsDocument: Boolean
            get() = authority == "com.android.providers.downloads.documents"

        private val Uri.isMediaDocument: Boolean
            get() = authority == "com.android.providers.media.documents"

        private val Uri.isGooglePhotosUri: Boolean
            get() = authority == "com.google.android.apps.photos.content"

//        private fun parseExcelSheet(filePath: String): List<String> {
//    //        val workbook = WorkbookFactory.create(File(filePath))
//    //        return if (workbook.numberOfSheets > 0) {
//    //            workbook.getSheetAt(0).rowIterator().asSequence().toList().map {
//    //                it.cellIterator().asSequence().joinToString(",")
//    //            }
//    //        } else ArrayList()
//            return ArrayList()
//        }

        private fun parseTextFile(it: Uri): List<String> {

            var ret = ArrayList<String>()

            try {

                val stream = ctx.contentResolver.openInputStream(it)

                stream?.let {

                    val reader = BufferedReader(InputStreamReader(it))

                    ret = ArrayList(reader.readLines())
                }
            } catch (fo: FileNotFoundException) {
                fo.printStackTrace()
            } catch (io: IOException) {
                io.printStackTrace()
            }

            return ret
        }

        fun readText(context: Context, uri: Uri?): CharSequence {

            uri?.let {

                val lines = File(getFilePath(context, uri)).readLines()

                return lines.joinToString("\n")
            }

            return String()
        }

}
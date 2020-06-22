package org.phenoapps.intercross.util

//import org.apache.poi.ss.usermodel.WorkbookFactory
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.BaseTable
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.fragments.SettingsFragment
import java.io.*
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


    /***
     * Main parse driver. Either a parents or wishlist file can be loaded.
     * Parents files populates a table of barcodeIds with readable names.
     *  --Parents table is specifically used to print barcodes that don't exist in the crosses table (CIP)
     * The wishlist table contains rows of samples to be crossed with minimum and maximum requirements
     *
     * This driver function parses a text file and determines the input file type.
     * If a wishType column is detected, it is assumed to be a wishlist file.
     *
     * TODO Localizations must require that the parents input file header should not contain the wishlist type header.
     */
    fun parseInputFile(uri: Uri): Map<String, List<BaseTable>> {

        val lines = parseTextFile(uri, ",")

        /***
         * Column map is a hash map storing to-be inserted columns.
         * The map is populated during parsing and batch-inserted afterwards.
         */
        val columnMap = HashMap<String, ArrayList<out BaseTable>>()


        columnMap["Wishlist"] = ArrayList<Wishlist>()

        columnMap["Events"] = ArrayList<Event>()

        if (lines.isNotEmpty()) {

            val headers = lines[0].split(",")

            //ensure the headers size > 0
            if (headers.isNotEmpty()) {

                if (headers.find { it == wishlistTypeHeader }.isNullOrBlank()) {

                    val parents = ArrayList<Parent>()

                    //import parents file
                    loadParents(headers, lines-lines[0], parents)

                    columnMap["Parents"] = parents

                } else {

                    val wishlist = ArrayList<Wishlist>()

                    loadWishlist(headers, lines-lines[0], wishlist)

                    columnMap["Wishlist"] = wishlist
                }
            }

        }

        return columnMap
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
                             wishlist: ArrayList<Wishlist>) {

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

                                    //TODO chaney add null name case constructor in Wishlist class
                                    wishlist.add(Wishlist(
                                            femaleDbId = row[femaleId],
                                            maleDbId = row[maleId],
                                            femaleName = readableFemaleName ?: row[femaleId],
                                            maleName = readableMaleName ?: row[maleId],
                                            wishType = row[type],
                                            wishMin = row[min].toInt(),
                                            wishMax = wishlistMax
                                    ))
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

    private fun getPath(uri: Uri?): String {

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
//        val workbook = WorkbookFactory.create(File(filePath))
//        return if (workbook.numberOfSheets > 0) {
//            workbook.getSheetAt(0).rowIterator().asSequence().toList().map {
//                it.cellIterator().asSequence().joinToString(",")
//            }
//        } else ArrayList()
        return ArrayList()
    }

    private fun parseTextFile(it: Uri, delim: String): List<String> {

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

    fun readText(uri: Uri?): CharSequence {

        uri?.let {

            val lines = File(getPath(uri)).readLines()

            return lines.joinToString("\n")
        }
        return String()
    }
}
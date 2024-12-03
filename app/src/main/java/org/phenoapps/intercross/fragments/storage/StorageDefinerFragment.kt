package org.phenoapps.intercross.fragments.storage

import android.app.Activity
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.phenoapps.fragments.storage.PhenoLibStorageDefinerFragment
import org.phenoapps.intercross.R
import org.phenoapps.intercross.activities.DefineStorageActivity
import javax.inject.Inject

@AndroidEntryPoint
class StorageDefinerFragment : PhenoLibStorageDefinerFragment() {

    @Inject
    lateinit var prefs: SharedPreferences

    // default root folder name if user choose an incorrect root on older devices
    override val defaultAppName: String = "intercross"

    // if this file exists the migrator will be skipped
    override val migrateChecker: String = ".intercross"

    // define sample data and where to transfer
    override val samples = mapOf(
        AssetSample("wishlist_import", "wishlist_sample.csv") to R.string.dir_wishlist_import
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // define directories that should be created in root storage
        context?.let { ctx ->
            val wishlistImport = ctx.getString(R.string.dir_wishlist_import)
            directories = arrayOf(wishlistImport)
        }
    }

    override fun onTreeDefined(treeUri: Uri) {
            (activity as DefineStorageActivity).enableBackButton(false)
            super.onTreeDefined(treeUri)
            (activity as DefineStorageActivity).enableBackButton(true)
    }

    override fun actionAfterDefine() {
        actionNoMigrate()
    }

    override fun actionNoMigrate() {
        activity?.setResult(Activity.RESULT_OK)
        activity?.finish()
    }
}
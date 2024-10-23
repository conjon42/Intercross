package org.phenoapps.intercross.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope

import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.mikepenz.aboutlibraries.LibsBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.phenoapps.intercross.BuildConfig
import org.phenoapps.intercross.R
import java.net.HttpURLConnection
import java.net.URL

class AboutFragment : MaterialAboutFragment() {

    private lateinit var updateCheckItem: MaterialAboutActionItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().setTheme(R.style.AppTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkForUpdate()
    }

    override fun getMaterialAboutList(c: Context): MaterialAboutList {
        val appCardBuilder = MaterialAboutCard.Builder()

        appCardBuilder.addItem(MaterialAboutTitleItem.Builder()
                .text(getString(R.string.app_name))
                .icon(R.mipmap.ic_launcher)
                .build())
        appCardBuilder.addItem(ConvenienceBuilder.createVersionActionItem(c,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_about_info),
                getString(R.string.about_version_title),
                false))

        updateCheckItem = MaterialAboutActionItem.Builder()
                .text(getString(R.string.check_updates_title))
                .icon(R.drawable.ic_about_changelog)
                .build()
        appCardBuilder.addItem(updateCheckItem)

        appCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_about_website),
                getString(R.string.about_manual_title),
                false,
                Uri.parse("https://docs.fieldbook.phenoapps.org/en/latest/intercross.html")))

        appCardBuilder.addItem(ConvenienceBuilder.createRateActionItem(c,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_about_rate),
                getString(R.string.about_rate),
                null
        ))

        val authorCardBuilder = MaterialAboutCard.Builder()
        authorCardBuilder.title(getString(R.string.about_project_lead_title))
        authorCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_developer_trife))
                .subText(getString(R.string.about_developer_trife_location))
                .icon(R.drawable.ic_nv_about)
                .build())

        authorCardBuilder.addItem(ConvenienceBuilder.createEmailItem(c,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_about_email),
                getString(R.string.about_email_title),
                true,
                getString(R.string.about_developer_trife_email),
                "Intercross Question"))

        val contributorsCardBuilder = MaterialAboutCard.Builder()
        contributorsCardBuilder.title(getString(R.string.about_support_title))

        contributorsCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_about_contributors),
                getString(R.string.about_contributors_title),
                false,
                Uri.parse("https://github.com/PhenoApps/Intercross#-contributors")))

        contributorsCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_about_funding),
                getString(R.string.about_contributors_funding_title),
                false,
                Uri.parse("https://github.com/PhenoApps/Intercross#-funding")))

        val technicalCardBuilder = MaterialAboutCard.Builder()
        technicalCardBuilder.title(getString(R.string.about_technical_title))

        technicalCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(R.string.about_github_title)
                .icon(R.drawable.ic_about_github)
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(c, Uri.parse("https://github.com/PhenoApps/Intercross")))
                .build())

        technicalCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(R.string.about_libraries_title)
                .icon(R.drawable.ic_about_libraries)
                .setOnClickAction {
                    LibsBuilder()
                            .withActivityTitle(getString(R.string.about_libraries_title))
                            .withAboutIconShown(true)
                            .withAboutVersionShown(true)
                            .withAboutAppName(getString(R.string.app_name))
                            .start(requireContext())
                }
                .build())

        val otherAppsCardBuilder = MaterialAboutCard.Builder()
        otherAppsCardBuilder.title(getString(R.string.about_title_other_apps))

        otherAppsCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_about_website),
                "PhenoApps.org",
                false,
                Uri.parse("http://phenoapps.org/")))

        otherAppsCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text("Field Book")
                .icon(R.drawable.other_ic_field_book)
                .setOnClickAction(openAppOrStore("com.fieldbook.tracker", c))
                .build())

        otherAppsCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text("Coordinate")
                .icon(R.drawable.other_ic_coordinate)
                .setOnClickAction(openAppOrStore("org.wheatgenetics.coordinate", c))
                .build())

        return MaterialAboutList(
                appCardBuilder.build(),
                authorCardBuilder.build(),
                contributorsCardBuilder.build(),
                technicalCardBuilder.build(),
                otherAppsCardBuilder.build()
        )
    }

    private fun checkForUpdate() {
        val currentVersion = BuildConfig.VERSION_NAME
        val owner = "PhenoApps"
        val repo = "Intercross"
        val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val url = URL(apiUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        null
                    }
                }

                result?.let {
                    val json = JSONObject(it)
                    val latestVersion = json.getString("tag_name")
                    val downloadUrl = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")

                    val isNewVersionAvailable = isNewerVersion(currentVersion, latestVersion)
                    showVersionStatus(isNewVersionAvailable, latestVersion, downloadUrl)
                }
            } catch (e: Exception) {
                Log.e("AboutFragment", "Error checking for updates: ${e.message}")
            }
        }
    }

    private fun showVersionStatus(isNewVersionAvailable: Boolean, latestVersion: String?, downloadUrl: String?) {
        if (isNewVersionAvailable) {
            updateCheckItem.text = getString(R.string.found_updates_title)
            updateCheckItem.subText = latestVersion
            updateCheckItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_about_changelog)
            updateCheckItem.setOnClickAction {
                downloadUrl?.let {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    startActivity(browserIntent)
                }
            }
        } else {
            updateCheckItem.text = getString(R.string.no_updates_title)
            updateCheckItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_about_changelog)
            updateCheckItem.setOnClickAction(null)
        }
        refreshMaterialAboutList()
    }

    private fun isNewerVersion(currentVersion: String, latestVersion: String): Boolean {
        val currentVersionComponents = currentVersion.split(".")
        val latestVersionComponents = latestVersion.split(".")

        val versionComponentsToCompare = minOf(currentVersionComponents.size, latestVersionComponents.size)

        for (i in 0 until versionComponentsToCompare) {
            val currentComponent = currentVersionComponents[i].toInt()
            val latestComponent = latestVersionComponents[i].toInt()

            if (currentComponent < latestComponent) return true
            if (currentComponent > latestComponent) return false
        }

        return false
    }

    private fun openAppOrStore(packageName: String, c: Context): MaterialAboutItemOnClickAction {
        val packageManager = requireContext().packageManager
        return try {
            packageManager.getPackageInfo(packageName, 0)
            MaterialAboutItemOnClickAction {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                startActivity(launchIntent)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            ConvenienceBuilder.createWebsiteOnClickAction(c, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        }
    }
}

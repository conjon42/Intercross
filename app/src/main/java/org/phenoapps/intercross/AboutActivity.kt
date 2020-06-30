package org.phenoapps.intercross

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.michaelflisar.changelog.ChangelogBuilder
import com.michaelflisar.changelog.classes.ImportanceChangelogSorter
import com.mikepenz.aboutlibraries.LibsBuilder

class AboutActivity : MaterialAboutActivity() {

    //todo move to fragments so aboutactivity can extend base activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    public override fun getMaterialAboutList(c: Context): MaterialAboutList {
        val appCardBuilder = MaterialAboutCard.Builder()

        // Add items to card
        appCardBuilder.addItem(MaterialAboutTitleItem.Builder()
                .text(getString(R.string.app_name))
                .icon(R.mipmap.ic_launcher)
                .build())
        appCardBuilder.addItem(ConvenienceBuilder.createVersionActionItem(c,
                getDrawable(R.drawable.ic_about_info),
                getString(R.string.about_version_title),
                false))
        appCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.changelog_title))
                .icon(R.drawable.ic_about_changelog)
                .setOnClickAction { showChangelog(false, false) }
                .build())
        appCardBuilder.addItem(ConvenienceBuilder.createRateActionItem(c,
                getDrawable(R.drawable.ic_about_rate),
                getString(R.string.about_rate),
                null
        ))
        //TODO: Add new translation project
        appCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(R.string.about_help_translate_title)
                .icon(R.drawable.ic_about_help_translate)
                //.setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(c, Uri.parse("https://osij6hx.oneskyapp.com/collaboration/project?id=28259")))
                .build())
        val authorCardBuilder = MaterialAboutCard.Builder()
        authorCardBuilder.title(getString(R.string.about_project_lead_title))
        authorCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_developer_trife))
                .subText(getString(R.string.about_developer_trife_location))
                .icon(R.drawable.ic_nav_drawer_person)
                .build())
        authorCardBuilder.addItem(ConvenienceBuilder.createEmailItem(c,
                getDrawable(R.drawable.ic_about_email),
                getString(R.string.about_email_title),
                true,
                getString(R.string.about_developer_trife_email),
                "Intercross Question"))
        authorCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                getDrawable(R.drawable.ic_about_website),
                "PhenoApps.org",
                false,
                Uri.parse("http://phenoapps.org/")))
        val contributorsCardBuilder = MaterialAboutCard.Builder()
        contributorsCardBuilder.title(getString(R.string.about_contributors_title))

        //TODO: Add contributors link.
        contributorsCardBuilder.addItem(ConvenienceBuilder.createWebsiteActionItem(c,
                resources.getDrawable(R.drawable.ic_about_contributors),
                getString(R.string.about_contributors_developers_title),
                false,
                Uri.parse("https://github.com/PhenoApps/Field-Book/graphs/contributors")))
        contributorsCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_translators_title))
                .subText(getString(R.string.about_translators_text))
                .icon(R.drawable.ic_about_translators)
                .build())
        contributorsCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_contributors_funding_title))
                .subText(getString(R.string.about_contributors_funding_text))
                .icon(R.drawable.ic_about_funding)
                .build())
        val technicalCardBuilder = MaterialAboutCard.Builder()
        technicalCardBuilder.title(getString(R.string.about_technical_title))
        technicalCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(R.string.about_github_title)
                .icon(R.drawable.ic_about_github)
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(c, Uri.parse("https://github.com/PhenoApps/Intercross")))
                .build())
        technicalCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(R.string.libraries_title)
                .icon(R.drawable.ic_about_libraries)
                .setOnClickAction(MaterialAboutItemOnClickAction {
                    LibsBuilder()
                            //.withActivityTheme(R.style.AppTheme)
                            .withAutoDetect(true)
                            .withActivityTitle(getString(R.string.libraries_title))
                            .withLicenseShown(true)
                            .withVersionShown(true)
                            .start(applicationContext)
                })
                .build())
        val otherAppsCardBuilder = MaterialAboutCard.Builder()
        otherAppsCardBuilder.title(getString(R.string.about_title_other_apps))
        otherAppsCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text("Coordinate")
                .icon(R.drawable.other_ic_coordinate)
                .setOnClickAction(openAppOrStore("org.wheatgenetics.coordinate", c))
                .build())
        otherAppsCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text("Inventory")
                .icon(R.drawable.other_ic_inventory)
                .setOnClickAction(openAppOrStore("org.wheatgenetics.inventory", c))
                .build())
        otherAppsCardBuilder.addItem(MaterialAboutActionItem.Builder()
                .text("Verify")
                .icon(R.drawable.other_ic_verify)
                .build())

        return MaterialAboutList(appCardBuilder.build(), authorCardBuilder.build(), contributorsCardBuilder.build(), otherAppsCardBuilder.build(), technicalCardBuilder.build())
    }

    private fun showChangelog(managedShow: Boolean, rateButton: Boolean) {
        val builder = ChangelogBuilder()
                .withUseBulletList(true) // true if you want to show bullets before each changelog row, false otherwise
                .withManagedShowOnStart(managedShow) // library will take care to show activity/dialog only if the changelog has new infos and will only show this new infos
                .withRateButton(rateButton) // enable this to show a "rate app" button in the dialog => clicking it will open the play store; the parent activity or target fragment can also implement IChangelogRateHandler to handle the button click
                .withSummary(false, true) // enable this to show a summary and a "show more" button, the second paramter describes if releases without summary items should be shown expanded or not
                .withTitle(getString(R.string.changelog_title)) // provide a custom title if desired, default one is "Changelog <VERSION>"
                .withOkButtonLabel("OK") // provide a custom ok button text if desired, default one is "OK"
                .withSorter(ImportanceChangelogSorter())
                .buildAndShowDialog(this, false) // second parameter defines, if the dialog has a dark or light theme
    }

    override fun getActivityTitle(): CharSequence? {
        return getString(R.string.mal_title_about)
    }

    private fun openAppOrStore(packageName: String, c: Context): MaterialAboutItemOnClickAction {
        val packageManager = baseContext.packageManager
        return try {
            packageManager.getPackageInfo(packageName, 0)
            MaterialAboutItemOnClickAction {
                val launchIntent = getPackageManager().getLaunchIntentForPackage(packageName)
                startActivity(launchIntent)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            ConvenienceBuilder.createWebsiteOnClickAction(c, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        }
    }
}
package org.phenoapps.intercross.fragments.preferences

class GeneralKeys {
    companion object {
        val INTERCROSS_PREFIX = "org.phenoapps.intercross."

        // PROFILE PREFERENCES
        val FIRST_NAME = INTERCROSS_PREFIX + "FIRST_NAME"
        val LAST_NAME = INTERCROSS_PREFIX + "LAST_NAME"
        val REQUIRE_USER_TO_COLLECT = INTERCROSS_PREFIX + "REQUIRE_USER_TO_COLLECT"
        val REQUIRE_USER_INTERVAL = INTERCROSS_PREFIX + "REQUIRE_USER_INTERVAL"

        val LAST_TIME_ASKED = INTERCROSS_PREFIX + "LAST_TIME_OPENED"
        val ASKED_SINCE_OPENED = INTERCROSS_PREFIX + "ASKED_SINCE_OPENED"
        val MODIFY_PROFILE_SETTINGS = "MODIFY_PROFILE_SETTINGS"
        val PERSON_UPDATE = "PERSON_UPDATE"


        // BRAPI PREFERENCES
        const val BRAPI_BASE_URL = "org.phenoapps.intercross.BRAPI_BASE_URL"
        const val BRAPI_PAGE_SIZE = "1000"
        const val BRAPI_TOKEN = "org.phenoapps.intercross.BRAPI_TOKEN"
        const val BRAPI_VERSION = "2"
    }
}
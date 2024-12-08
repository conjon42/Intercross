package org.phenoapps.intercross.fragments.preferences

class GeneralKeys {
    companion object {
        const val INTERCROSS_PREFIX = "org.phenoapps.intercross."

        const val FIRST_RUN = INTERCROSS_PREFIX + "FIRST_RUN"

        // APP INTRO
        const val LOAD_SAMPLE_PARENTS = INTERCROSS_PREFIX + "LOAD_SAMPLE_PARENTS"
        const val LOAD_SAMPLE_WISHLIST = INTERCROSS_PREFIX + "LOAD_SAMPLE_WISHLIST"

        // PROFILE PREFERENCES
        const val FIRST_NAME = INTERCROSS_PREFIX + "FIRST_NAME"
        const val LAST_NAME = INTERCROSS_PREFIX + "LAST_NAME"
        const val REQUIRE_USER_TO_COLLECT = INTERCROSS_PREFIX + "REQUIRE_USER_TO_COLLECT"
        const val REQUIRE_USER_INTERVAL = INTERCROSS_PREFIX + "REQUIRE_USER_INTERVAL"

        const val LAST_TIME_ASKED = INTERCROSS_PREFIX + "LAST_TIME_OPENED"
        const val ASKED_SINCE_OPENED = INTERCROSS_PREFIX + "ASKED_SINCE_OPENED"
        const val MODIFY_PROFILE_SETTINGS = "MODIFY_PROFILE_SETTINGS"
        const val PERSON_UPDATE = "PERSON_UPDATE"

        const val EXPERIMENT_NAME = INTERCROSS_PREFIX + "EXPERIMENT_NAME"

        // PRINTING PREFERENCES
        const val ZPL_CODE = INTERCROSS_PREFIX + "ZPL_CODE"

        // BEHAVIOR PREFERENCES
        // Naming
        const val BLANK_MALE_ID = INTERCROSS_PREFIX + "BLANK_MALE_ID"
        const val CROSS_ORDER = INTERCROSS_PREFIX + "CROSS_ORDER"
        const val CREATE_PATTERN = INTERCROSS_PREFIX + "CREATE_PATTERN"
        // Workflow
        const val COLLECT_INFO = INTERCROSS_PREFIX + "COLLECT_INFO"
        const val META_DATA = INTERCROSS_PREFIX + "META_DATA"
        const val META_DATA_DEFAULTS = INTERCROSS_PREFIX + "META_DATA_DEFAULTS"
        const val SOUND_NOTIFICATIONS = INTERCROSS_PREFIX + "SOUND_NOTIFICATIONS"
        const val OPEN_CROSS_IMMEDIATELY = INTERCROSS_PREFIX + "OPEN_CROSS_IMMEDIATELY"
        const val COMMUTATIVE_CROSSING = INTERCROSS_PREFIX + "COMMUTATIVE_CROSSING"


        // val COMMUTATIVE_CROSSING = INTERCROSS_PREFIX + "BLANK_MALE_ID"

        // BRAPI PREFERENCES
        const val BRAPI_ENABLED = "org.phenoapps.intercross.BRAPI_ENABLED"
        const val BRAPI_BASE_URL = "org.phenoapps.intercross.BRAPI_BASE_URL"
        const val BRAPI_PAGE_SIZE = "1000"
        const val BRAPI_TOKEN = "org.phenoapps.intercross.BRAPI_TOKEN"
        const val BRAPI_VERSION = "2"
        const val BRAPI_DISPLAY_NAME = "org.phenoapps.intercross.BRAPI_DISPLAY_NAME"
    }
}
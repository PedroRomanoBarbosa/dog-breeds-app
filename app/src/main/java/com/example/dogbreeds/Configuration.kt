package com.example.dogbreeds

/**
 * Object that contains various configuration values used throughout the app
 */
object Configuration {
    /**
     * The limit number of breeds per page for a paginated request
     */
    const val PAGE_LIMIT = 20

    /**
     * The delay to be used to simulate slow connections. This is only used if
     * [BuildConfig.REQUEST_DELAY_ENABLED] is enabled in the build
     */
    const val REQUEST_DELAY = 1000L
}
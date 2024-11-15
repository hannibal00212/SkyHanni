package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class EliteAPISettingsJson(
    @Expose val refreshTimeMinutes: Int = 10,
    @Expose val disableFetchingWhenPassed: Boolean = false,
    @Expose val disableRefreshCommand: Boolean = false,
)

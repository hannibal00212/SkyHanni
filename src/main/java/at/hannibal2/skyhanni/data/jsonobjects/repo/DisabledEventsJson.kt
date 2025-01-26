package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class DisabledEventsJson(
    @Expose val disabledHandlers: Set<String> = emptySet(),
    @Expose val disabledInvokers: Set<String> = emptySet(),
)

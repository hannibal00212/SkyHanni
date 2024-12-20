package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class DisabledFeaturesJson(
    @Expose val features: Map<String, Boolean>,
)

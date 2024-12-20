package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class VipVisitsJson(
    @Expose val vipVisits: List<String>,
)

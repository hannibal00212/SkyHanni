package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class MultiFilterJson(
    @Expose val equals: List<String>,
    @Expose val startsWith: List<String>,
    @Expose val endsWith: List<String>,
    @Expose val contains: List<String>,
    @Expose val containsWord: List<String>,
    @Expose val description: String,
)

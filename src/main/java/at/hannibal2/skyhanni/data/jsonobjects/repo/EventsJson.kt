package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class EventsJson(
    @Expose @SerializedName("great_spook") val greatSpook: Map<String, SimpleTimeMark>,
)

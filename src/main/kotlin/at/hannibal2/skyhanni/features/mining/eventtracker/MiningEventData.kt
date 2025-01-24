package at.hannibal2.skyhanni.features.mining.eventtracker

import at.hannibal2.skyhanni.data.IslandType
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MiningEventDataSend(
    @Expose @SerializedName("server_type") val serverType: IslandType,
    @Expose @SerializedName("server_id") val serverId: String,
    @Expose val event: MiningEventType,
    @Expose @SerializedName("time_left") val timeRemaining: Long,
    @Expose @SerializedName("reporter_uuid") val uuid: String
)

data class MiningEventDataReceive(
    @Expose val success: Boolean,
    @Expose val data: MiningEventData,
    @Expose val cause: String
)

data class MiningEventData(
    @Expose @SerializedName("event_datas") val eventData: Map<IslandType, Map<MiningEventType, EventData>>,
    @Expose @SerializedName("running_events") val runningEvents: Map<IslandType, List<RunningEventType>>,
    @Expose @SerializedName("total_lobbys") val totalLobbies: Map<IslandType, Int>,
    @Expose @SerializedName("update_in") val updateIn: Long,
    @Expose @SerializedName("curr_time") val currentTime: Long
)

data class EventData(
    @Expose @SerializedName("starts_at_min") val startMin: Long,
    @Expose @SerializedName("starts_at_max") val startMax: Long,
    @Expose @SerializedName("ends_at_min") val endMin: Long,
    @Expose @SerializedName("ends_at_max") val endMax: Long,
    @Expose @SerializedName("lobby_count") val lobbyCount: Int
)

data class RunningEventType(
    @Expose val event: MiningEventType,
    @Expose @SerializedName("ends_at") val endsAt: Long,
    @Expose @SerializedName("lobby_count") val lobbyCount: Int,
    @Expose @SerializedName("is_double") val isDoubleEvent: Boolean
)

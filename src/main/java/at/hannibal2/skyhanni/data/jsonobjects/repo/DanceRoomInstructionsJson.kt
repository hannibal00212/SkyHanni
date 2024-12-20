package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class DanceRoomInstructionsJson(
    @Expose val instructions: List<String>,
)

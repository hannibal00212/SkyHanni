package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose

data class ModGuiSwitcherJson(
    @Expose val mods: Map<String, OtherModInfo>,
)

data class OtherModInfo(
    @Expose val description: List<String>,
    @Expose val command: String,
    @Expose val guiPath: List<String>,
)

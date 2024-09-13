package at.hannibal2.skyhanni.config.enums

import at.hannibal2.skyhanni.SkyHanniMod
import net.minecraft.client.Minecraft

enum class OutsideSbFeature(private val displayName: String) {
    MODIFY_VISUAL_WORDS("Modify Visual Words"),
    CUSTOM_TEXT_BOX("Custom Text Box"),
    REAL_TIME("Real Time"),
    TPS_DISPLAY("TPS Display"),
    MARKED_PLAYERS("Marked Players"),
    FARMING_WEIGHT("Farming Weight"),
    NEXT_JACOB_CONTEST("Next Jacobs's Contest"),
    COMPOSTER_TIME("Composter Time"),
    YAW_AND_PITCH("Yaw and Pitch"),
    ATMOSPHERIC_FILTER("Atmospheric Filter"),
    QUICK_MOD_MENU_SWITCH("Quick Mod Menu Switch"),
    FOLLOWING_LINE("Following Line"),
    ARROW_TRAIL("Arrow Trail"),
    HIGHLIGHT_PARTY_MEMBERS("Highlight Party Members"),
    MOVEMENT_SPEED("Movement Speed"),
    CUSTOM_SCOREBOARD("Custom Scoreboard (only on Hypixel)"),
    ;

    override fun toString() = displayName

    fun isSelected() = Minecraft.getMinecraft().thePlayer != null && SkyHanniMod.feature.misc.showOutsideSB.get().contains(this)
}

package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.RenderEntityOutlineEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.FontRenderer
import net.minecraft.entity.Entity
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.scoreboard.Team

@SkyHanniModule
object DungeonTeammateOutlines {

    private val config get() = SkyHanniMod.feature.dungeon

    @HandleEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent) {
        if (isEnabled() && event.type === RenderEntityOutlineEvent.Type.XRAY) {
            event.queueEntitiesToOutline { entity -> getEntityOutlineColor(entity) }
        }
    }

    private fun isEnabled() = DungeonApi.inDungeon() && config.highlightTeammates

    private fun getEntityOutlineColor(entity: Entity): Int? {
        if (entity !is EntityOtherPlayerMP || entity.team == null) return null

        // Must be visible on the scoreboard
        val team = entity.team as ScorePlayerTeam
        if (team.nameTagVisibility == Team.EnumVisible.NEVER) return null

        val colorFormat = FontRenderer.getFormatFromString(team.colorPrefix)
        return if (colorFormat.length >= 2)
            Minecraft.getMinecraft().fontRendererObj.getColorCode(colorFormat[1])
        else null
    }
}

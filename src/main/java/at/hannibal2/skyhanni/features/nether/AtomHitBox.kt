package at.hannibal2.skyhanni.features.nether

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyHanniModule
object AtomHitBox {

    private val config get() = SkyHanniMod.feature.crimsonIsle.atomHitBox
    private var atomsList = mapOf<EntityArmorStand, Atom>()

    @SubscribeEvent
    fun onRender(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        atomsList = atomsList.editCopy {
            entries.removeIf {
                !it.key.isEntityAlive
            }
        }
        for ((entity, atom) in atomsList) {
            RenderUtils.drawWireframeBoundingBox_nea(entity.entityBoundingBox, atom.color, event.partialTicks)
            event.drawDynamicText(entity.getLorenzVec(), atom.displayName, 1.0, ignoreBlocks = false)
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        for (entity in EntityUtils.getAllEntities().filterIsInstance<EntityArmorStand>()) {
            val atom = Atom.entries.firstOrNull { entity.hasSkullTexture(it.texture) } ?: continue
            if (!atom.isSelected()) continue
            atomsList = atomsList.editCopy {
                this[entity] = atom
            }
        }
    }

    enum class Atom(
        val displayName: String,
        val texture: String,
        val color: Color,
    ) {
        EXE(
            "§aExe",
            "ewogICJ0aW1lc3RhbXAiIDogMTY0NjA2NzI1MDA4NSwKICAicHJvZmlsZUlkIiA6ICJmNThkZWJkNTlmNTA0MjIyOGY2MDIyMjExZDRjMTQwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ1bnZlbnRpdmV0YWxlbnQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU0NmEzOWE4ZTYxYTRkYTA3YjE3YWRlZjBlZTIyMDlmNjRkYThhZWI5YTliZDYxMTBhNGUyNGUzNWVkNzRmMyIKICAgIH0KICB9Cn0=",
            Color.GREEN,
        ),
        WAI(
            "§6Wai",
            "ewogICJ0aW1lc3RhbXAiIDogMTY0NjA2NzE3MjQxNywKICAicHJvZmlsZUlkIiA6ICI0NDAzZGM1NDc1YmM0YjE1YTU0OGNmZGE2YjBlYjdkOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDaGFvc0NvbXB1dHJDbHViIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y4M2ZkZGY3YWE2ZmYzYzUwYWJhYzdlNmE3Nzk2ZTE1NWNjYTRjY2ZjZTQ1NzM0ZjY2ZTQxMWNiY2E5ODlhMDQiCiAgICB9CiAgfQp9",
            Color.ORANGE,
        ),
        ZEE(
            "§5Zee",
            "ewogICJ0aW1lc3RhbXAiIDogMTY0NjA2NzIyODA3MywKICAicHJvZmlsZUlkIiA6ICJhOGJhMGY1YTFmNjQ0MTgzODZkZGI3OWExZmY5ZWRlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJDcmVlcGVyOTA3NSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80ZThmMTlkZWYzMzA4NWRiNTY0OTA5ZmM4YzFiMmVhYmMwZWJjNTljZDg5NjY1ZjMxZTU4MGYxZmMyNDk3YjU4IgogICAgfQogIH0KfQ==",
            Color.MAGENTA,
        ),
        ;

        override fun toString(): String {
            return displayName
        }
    }

    private fun Atom.isSelected() = config.atomsEntries.contains(this)

    fun isEnabled() = LorenzUtils.inSkyBlock && IslandType.CRIMSON_ISLE.isInIsland() && config.enabled
}

package at.hannibal2.skyhanni.features.misc.pets

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.CurrentPetAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CurrentPetDisplay {

    private val config get() = SkyHanniMod.feature.misc.pets

<<<<<<< HEAD
=======
    private val patternGroup = RepoPattern.group("misc.currentpet")

    /**
     * REGEX-TEST: §7§7Selected pet: §6Enderman
     * REGEX-TEST: §7§7Selected pet: §cNone
     */
    private val inventorySelectedPetPattern by patternGroup.pattern(
        "inventory.selected",
        "§7§7Selected pet: (?<pet>.*)",
    )

    /**
     * REGEX-TEST: §aYou summoned your §r§6Enderman§r§a!
     */
    private val chatSpawnPattern by patternGroup.pattern(
        "chat.spawn",
        "§aYou summoned your §r(?<pet>.*)§r§a!",
    )

    /**
     * REGEX-TEST: §aYou despawned your §r§6Enderman§r§a!
     */
    private val chatDespawnPattern by patternGroup.pattern(
        "chat.despawn",
        "§aYou despawned your §r.*§r§a!",
    )

    /**
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 100] §6Griffin§4 ✦§e! §a§lVIEW RULE
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 100] §6Elephant§e! §a§lVIEW RULE
     */
    private val chatPetRulePattern by patternGroup.pattern(
        "chat.rule",
        "§cAutopet §eequipped your §7\\[Lvl .*] (?<pet>.*)§e! §a§lVIEW RULE",
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        findPetInChat(event.message)?.let {
            PetAPI.currentPet = it
            if (config.hideAutopet) {
                event.blockedReason = "pets"
            }
        }
    }

    private fun findPetInChat(message: String): String? {
        chatSpawnPattern.matchMatcher(message) {
            return group("pet")
        }
        if (chatDespawnPattern.matches(message)) {
            return ""
        }
        chatPetRulePattern.matchMatcher(message) {
            return group("pet")
        }

        return null
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!PetAPI.isPetMenu(event.inventoryName)) return

        val lore = event.inventoryItems[4]?.getLore() ?: return
        inventorySelectedPetPattern.firstMatcher(lore) {
            val newPet = group("pet")
            PetAPI.currentPet = if (newPet != "§cNone") newPet else ""
        }
    }

>>>>>>> upstream/beta
    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!LorenzUtils.inSkyBlock || RiftAPI.inRift() || !config.display) return

        val displayName = CurrentPetAPI.currentPet?.getUserFriendlyName(includeLevel = false) ?: return
        config.displayPos.renderString(displayName, posLabel = "Current Pet")
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "misc.petDisplay", "misc.pets.display")
        event.move(9, "misc.petDisplayPos", "misc.pets.displayPos")
    }
}

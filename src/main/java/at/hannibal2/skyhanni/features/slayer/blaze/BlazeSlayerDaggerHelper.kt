package at.hannibal2.skyhanni.features.slayer.blaze

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.core.config.gui.GuiPositionEditor
import at.hannibal2.skyhanni.config.features.slayer.blaze.BlazeHellionConfig.FirstDaggerEntry
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.TitleReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object BlazeSlayerDaggerHelper {

    private val config get() = SkyHanniMod.feature.slayer.blazes.hellion

    private val attunementPattern by RepoPattern.pattern(
        "slayer.blaze.dagger.attunement",
        "§cStrike using the §r(.+) §r§cattunement on your dagger!"
    )

    private var clientSideClicked = false
    private var textTop = ""
    private var textBottom = ""

    private var lastDaggerCheck = SimpleTimeMark.farPast()
    private var lastNearestCheck = SimpleTimeMark.farPast()
    private var lastNearest: HellionShield? = null

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.hideDaggerWarning) return

        val message = event.message
        if (attunementPattern.matches(message) || message == "§cYour hit was reduced by Hellion Shield!") {
            event.blockedReason = "blaze_slayer_dagger"
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        val player = Minecraft.getMinecraft().thePlayer
        val dagger = getDaggerFromStack(player.inventory.mainInventory[player.inventory.currentItem])
        if (dagger != null) {
            setDaggerText(dagger)
            return
        }

        textTop = ""
        textBottom = ""
    }

    private fun setDaggerText(holding: Dagger) {
        checkActiveDagger()
        lastNearest = findNearest()

        val first = Dagger.entries[config.firstDagger.ordinal] // todo avoid ordinal
        val second = first.other()

        textTop = format(holding, true, first) + " " + format(holding, true, second)
        textBottom = format(holding, false, first) + " " + format(holding, false, second)
    }

    private fun findNearest(): HellionShield? {
        if (!config.markRightHellionShield) return null

        if (lastNearestCheck.passedSince() < 100.milliseconds) return lastNearest
        lastNearestCheck = SimpleTimeMark.now()

        val playerLocation = LocationUtils.playerLocation()
        return HellionShieldHelper.hellionShieldMobs
            .filter { !it.key.isDead && it.key.getLorenzVec().distance(playerLocation) < 10 && it.key.health > 0 }
            .toSortedMap { a, b ->
                if (a.getLorenzVec().distance(playerLocation) > b.getLorenzVec().distance(playerLocation)) 1 else 0
            }.firstNotNullOfOrNull { it.value }
    }

    private fun format(dagger: Dagger, active: Boolean, compareInHand: Dagger): String {
        var daggerInHand = dagger
        val inHand = dagger == compareInHand

        if (!inHand) {
            daggerInHand = daggerInHand.other()
        }

        var shield = daggerInHand.getActive()
        if (!active) {
            shield = shield.other()
        }

        return if (inHand && active) {
            if (lastNearest == null) {
                "§7[" + shield.chatColor + shield.cleanName + "§7]"
            } else {
                if ((shield == lastNearest)) {
                    "§a[" + shield.chatColor + shield.cleanName.uppercase() + "§a]"
                } else {
                    "§c[§m" + shield.chatColor + shield.cleanName + "§c]"
                }
            }
        } else {
            if (shield == lastNearest) {
                "§6[" + shield.chatColor + shield.cleanName + "§6]"
            } else {
                shield.chatColor + shield.cleanName
            }
        }
    }

    private fun checkActiveDagger() {
        if (lastDaggerCheck.passedSince() < 1.seconds) return
        lastDaggerCheck = SimpleTimeMark.now()

        for (dagger in Dagger.entries) {
            if (dagger.updated) continue

            val first = dagger.shields[0]
            if (!first.active && !dagger.shields[1].active) {

                val shield = readFromInventory(dagger)
                if (shield != null) {
                    shield.active = true
                    dagger.updated = true
                } else {
                    first.active = true
                }
            }
        }
    }

    private fun readFromInventory(dagger: Dagger): HellionShield? {
        val player = Minecraft.getMinecraft().thePlayer
        for (stack in player.inventory.mainInventory) {
            val otherDagger = getDaggerFromStack(stack) ?: continue
            if (dagger != otherDagger) continue
            for (line in stack.getLore()) {
                if (!line.contains("§7Attuned: ")) continue

                for (shield in dagger.shields) {
                    if (line.contains(shield.cleanName)) {
                        return shield
                    }
                }
            }
        }

        return null
    }

    private fun getDaggerFromStack(stack: ItemStack?): Dagger? {
        val itemName = stack?.name.orEmpty()
        for (dagger in Dagger.entries) {
            if (dagger.daggerNames.any { itemName.contains(it) }) {
                return dagger
            }
        }

        return null
    }

    @SubscribeEvent
    fun onTitleReceived(event: TitleReceivedEvent) {
        if (!isEnabled()) return


        for (shield in HellionShield.entries) {
            if (shield.formattedName + "§r" == event.title) {
                for (dagger in Dagger.entries.filter { shield in it.shields }) {
                    dagger.shields.forEach { it.active = false }
                    dagger.updated = true
                }
                shield.active = true
                event.cancel()
                clientSideClicked = false
                return
            }
        }
    }

    private fun isEnabled(): Boolean {
        return LorenzUtils.inSkyBlock && config.daggers
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onBlockClick(event: BlockClickEvent) {
        if (!isEnabled()) return
        if (clientSideClicked) return
        if (event.clickType != ClickType.RIGHT_CLICK) return

        val itemInHand = event.itemInHand ?: return
        val dagger = getDaggerFromStack(itemInHand)
        dagger?.shields?.forEach { shield -> shield.active = !shield.active }
        clientSideClicked = true
    }

    enum class Dagger(val daggerNames: List<String>, vararg val shields: HellionShield, var updated: Boolean = false) {
        TWILIGHT(
            listOf("Twilight Dagger", "Mawdredge Dagger", "Deathripper Dagger"),
            HellionShield.SPIRIT,
            HellionShield.CRYSTAL
        ),
        FIREDUST(
            listOf("Firedust Dagger", "Kindlebane Dagger", "Pyrochaos Dagger"),
            HellionShield.ASHEN,
            HellionShield.AURIC
        ),
        ;

        fun other(): Dagger = if (this == TWILIGHT) {
            FIREDUST
        } else {
            TWILIGHT
        }

        fun getActive(): HellionShield {
            for (shield in shields) {
                if (shield.active) {
                    return shield
                }
            }
            throw RuntimeException("no active shield found for dagger $this")
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        if (textTop == "") return
        val currentScreen = Minecraft.getMinecraft().currentScreen
        if (currentScreen != null && currentScreen !is GuiPositionEditor) return

        config.positionTop.renderString(textTop, posLabel = "Blaze Slayer Dagger Top")
        config.positionBottom.renderString(textBottom, posLabel = "Blaze Slayer Dagger Bottom")
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "slayer.blazeDaggers", "slayer.blazes.hellion.daggers")
        event.move(3, "slayer.blazeMarkRightHellionShield", "slayer.blazes.hellion.markRightHellionShield")
        event.move(3, "slayer.blazeFirstDagger", "slayer.blazes.hellion.firstDagger")
        event.move(3, "slayer.blazeHideDaggerWarning", "slayer.blazes.hellion.hideDaggerWarning")

        event.transform(15, "slayer.blazes.hellion.firstDagger") { element ->
            ConfigUtils.migrateIntToEnum(element, FirstDaggerEntry::class.java)
        }
    }
}

private fun HellionShield.other(): HellionShield {
    for (dagger in BlazeSlayerDaggerHelper.Dagger.entries) {
        if (this in dagger.shields) {
            for (shield in dagger.shields) {
                if (shield != this) {
                    return shield
                }
            }
        }
    }

    throw RuntimeException("Found no other shield for $this")
}

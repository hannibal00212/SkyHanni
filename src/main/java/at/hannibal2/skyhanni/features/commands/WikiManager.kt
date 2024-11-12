package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.net.URLEncoder

@SkyHanniModule
object WikiManager {
    private const val OFFICIAL_URL_PREFIX = "https://wiki.hypixel.net/"
    private const val OFFICIAL_SEARCH_PREFIX = "index.php?search="
    private const val FANDOM_URL_PREFIX = "https://hypixel-skyblock.fandom.com/wiki/"
    private const val FANDOM_SEARCH_PREFIX = "Special:Search?query="

    private val config get() = SkyHanniMod.feature.misc.commands.betterWiki

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(6, "commands.useFandomWiki", "commands.fandomWiki.enabled")
    }

    @SubscribeEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!isEnabled()) return
        val message = event.message.lowercase()
        if (!(message.startsWith("/wiki"))) return

        event.cancel()
        if (message == "/wiki") {
            sendWikiMessage()
            return
        }
        if (message.startsWith("/wiki ")) {
            val search = event.message.drop("/wiki ".length)
            sendWikiMessage(search)
            return
        }
        if (message == ("/wikithis")) {
            val itemInHand = InventoryUtils.getItemInHand() ?: run {
                ChatUtils.userError("You must be holding an item to use this command!")
                return
            }
            wikiTheItem(itemInHand, config.autoOpenWiki)
            return
        }
    }

    @SubscribeEvent
    fun onKeybind(event: GuiKeyPressEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (NEUItems.neuHasFocus()) return
        val stack = event.guiContainer.slotUnderMouse?.stack ?: return

        if (!config.wikiKeybind.isKeyHeld()) return
        wikiTheItem(stack, config.menuOpenWiki)
    }

    private fun wikiTheItem(item: ItemStack, autoOpen: Boolean, useFandom: Boolean = config.useFandom) {
        val itemDisplayName =
            item.displayName.replace("§a✔ ", "").replace("§c✖ ", "")
        val internalName = item.getInternalName().asString()
        val wikiUrlSearch = if (internalName != "NONE") internalName else itemDisplayName.removeColor()

        sendWikiMessage(wikiUrlSearch, itemDisplayName.removeColor(), autoOpen, useFandom)
    }

    fun otherWikiCommands(args: Array<String>, useFandom: Boolean, wikithis: Boolean = false) {
        if (wikithis && !LorenzUtils.inSkyBlock) {
            ChatUtils.userError("You must be in SkyBlock to do this!")
            return
        }

        var search = ""
        for (arg in args) search = "$search$arg"

        if (wikithis) {
            val itemInHand = InventoryUtils.getItemInHand() ?: run {
                ChatUtils.userError("You must be holding an item to use this command!")
                return
            }
            wikiTheItem(itemInHand, false, useFandom = useFandom)
            return
        }
        if (search == "") {
            sendWikiMessage(useFandom = useFandom)
            return
        }
        sendWikiMessage(search, useFandom = useFandom)
    }

    fun sendWikiMessage(
        search: String = "",
        displaySearch: String = search,
        autoOpen: Boolean = config.autoOpenWiki,
        useFandom: Boolean = config.useFandom,
    ) {
        val wiki = if (useFandom) "SkyBlock Fandom Wiki" else "Official SkyBlock Wiki"
        val urlPrefix = if (useFandom) FANDOM_URL_PREFIX else OFFICIAL_URL_PREFIX
        if (search == "") {
            ChatUtils.clickableLinkChat(
                "§7Click §e§lHERE §7to visit the §6$wiki§7!", urlPrefix, "§7The $wiki!"
            )
            return
        }

        val urlSearchPrefix = if (useFandom) "$urlPrefix$FANDOM_SEARCH_PREFIX" else "$urlPrefix$OFFICIAL_SEARCH_PREFIX"
        val searchUrl = "$urlSearchPrefix${URLEncoder.encode(search, "UTF-8")}&scope=internal"

        ChatUtils.clickableLinkChat(
            "§7Click §e§lHERE §7to find §a$displaySearch §7on the §6$wiki§7!",
            searchUrl,
            "§7View §a$displaySearch §7on the §6$wiki§7!",
            autoOpen
        )
    }

    private fun isEnabled() = config.enabled
}

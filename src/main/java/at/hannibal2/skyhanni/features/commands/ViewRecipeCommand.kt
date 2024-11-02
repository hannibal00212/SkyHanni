package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils.senderIsSkyhanni
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NumberUtil.isInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ViewRecipeCommand {

    private val config get() = SkyHanniMod.feature.misc.commands

    /**
     * REGEX-TEST: /viewrecipe aspect of the end
     * REGEX-TEST: /viewrecipe aspect_of_the_end
     * REGEX-TEST: /viewrecipe ASPECT_OF_THE_END
     */
    private val pattern by RepoPattern.pattern(
        "commands.viewrecipe",
        "\\/viewrecipe (?<item>.*)"
    )

    @SubscribeEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (!config.viewRecipeLowerCase) return
        if (event.senderIsSkyhanni()) return

        val input = pattern.matchMatcher(event.message.lowercase()) {
            group("item").uppercase()
        } ?: return

        val args = input.split(" ")
        val endsWithPageNumber = args.last().isInt()
        val finalCommand = if (endsWithPageNumber) {
            "${args.dropLast(1).joinToString("_")} ${args.last()}"
        } else {
            input.replace(" ", "_")
        }

        event.cancel()
        HypixelCommands.viewRecipe(finalCommand)
    }

    val list by lazy {
        val list = mutableListOf<String>()
        for ((key, value) in NEUItems.allNeuRepoItems()) {
            if (value.has("recipe")) {
                list.add(key.lowercase())
            }
        }
        list
    }

    fun customTabComplete(command: String): List<String>? {
        if (command == "viewrecipe" && config.tabComplete.viewrecipeItems) {
            return list
        }

        return null
    }
}

package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.item.ItemHoverEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryApi
import at.hannibal2.skyhanni.features.misc.ContributorManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CircularList
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

@SkyHanniModule
object ReplaceHoppityWithContributor {

    private val config get() = ChocolateFactoryApi.config

    private val replaceMap = mutableMapOf<String, String>()

    @HandleEvent(priority = 5)
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        update()
    }

    @HandleEvent(priority = HandleEvent.LOW)
    fun onRepoReload(event: RepositoryReloadEvent) {
        update()
    }

    fun update() {
        replaceMap.clear()

        val contributors = ContributorManager.contributorNames
        val rabbits = HoppityCollectionData.rabbitRarities

        if (contributors.isEmpty()) return
        if (rabbits.isEmpty()) return

        val newNames = CircularList(contributors.toList())
        for (internalName in rabbits.map { it.key }.shuffled()) {
            val realName = internalName.allLettersFirstUppercase()
            val newName = newNames.next()
            replaceMap[realName] = newName
        }
    }

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onTooltip(event: ItemHoverEvent) {
        if (!isEnabled()) return
        if (!HoppityCollectionStats.inInventory) return

        val itemStack = event.itemStack
        val lore = itemStack.getLore()
        val last = lore.lastOrNull() ?: return
        if (!last.endsWith(" RABBIT")) return

        val realName = itemStack.name
        val cleanName = realName.removeColor()
        val fakeName = replaceMap[cleanName] ?: return

        val newName = event.toolTip[0].replace(cleanName, fakeName)
        event.toolTip[0] = newName

        event.toolTip.add(" ")
        event.toolTip.add("§8§oSome might say this rabbit is also known as $realName")

        // TODO find a way to handle non containing entries in a kotlin nullable way instead of checking for -1
        val index = event.toolTip.indexOfFirst { it.contains(" a duplicate") }
        if (index == -1) return
        val oldLine = event.toolTip[index]
        val newLine = oldLine.replace(cleanName, fakeName)
        event.toolTip[index] = newLine
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.contributorRabbitName
}

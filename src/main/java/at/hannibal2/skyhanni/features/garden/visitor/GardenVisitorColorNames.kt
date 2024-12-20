package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.data.jsonobjects.repo.GardenJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object GardenVisitorColorNames {

    private val visitorColors = mutableMapOf<String, String>() // name -> color code
    var visitorItems = mutableMapOf<String, List<String>>()

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<GardenJson>("Garden")
        visitorColors.clear()
        visitorItems.clear()
        for ((visitor, visitorData) in data.visitors) {
            visitorColors[visitor] = visitorData.rarity.color.getChatColor()
            visitorItems[visitor] = visitorData.needItems
        }
    }

    fun getColoredName(name: String): String {
        val cleanName = name.removeColor()
        val color = visitorColors[cleanName] ?: return name
        return color + cleanName
    }
}

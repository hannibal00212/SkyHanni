package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.utils.ItemUtils.isEnchanted
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getHotPotatoCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getItemUuid
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getReforgeName
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasArtOfPeace
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.isRecombobulated
import org.junit.jupiter.api.Test

class ItemModifierTest {

    @Test
    fun testUpgradeLevelMasterStars() {
        val itemStack = TestExportTools.getTestData(TestExportTools.Item, "10starnecronhead")
        assert(!itemStack.isRecombobulated())
        assert(itemStack.getReforgeName() == "ancient")
        assert(itemStack.getItemUuid() == "2810b7fe-33af-4dab-bb41-b4815f5847af")
        assert(itemStack.isEnchanted())
        assert(itemStack.getHotPotatoCount() == 15)
        assert(itemStack.getEnchantments()?.size == 11)
        assert(itemStack.hasArtOfPeace())
//        assert(itemStack.getDungeonStarCount() == 10)
    }
}

package at.hannibal2.skyhanni.test.garden

import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI
import org.junit.jupiter.api.Test

class VisitorAPITest {

    @Test
    fun testFromHypixelName() {
        assert(VisitorAPI.fromHypixelName(" §r Jacob") == "§fJacob")
        assert(VisitorAPI.fromHypixelName("§r Jacob") == "§fJacob")
        assert(VisitorAPI.fromHypixelName("§rJacob") == "§fJacob")
        assert(VisitorAPI.fromHypixelName("Jacob") == "§fJacob")
        assert(VisitorAPI.fromHypixelName(" Jacob ") == "§fJacob")
        assert(VisitorAPI.fromHypixelName("§cSpaceman") == "§cSpaceman")
        assert(VisitorAPI.fromHypixelName("§cGrandma Wolf") == "§cGrandma Wolf")
    }

    @Test
    fun testIsVisitorInfo() {

        // To short
        assert(
            !VisitorAPI.isVisitorInfo(
                mutableListOf(
                    "§a§lVisitor Info",
                    "§7§oClick to view info about this visitor."
                )
            )
        )

        // To long
        assert(
            !VisitorAPI.isVisitorInfo(
                mutableListOf(
                    "§a§lVisitor Info",
                    "§7§oClick to view info about this visitor.",
                    "§7§oClick to view info about this visitor.",
                    "§7§oClick to view info about this visitor.",
                    "§7§oClick to view info about this visitor.",
                )
            )
        )

        // Third line is §7Offers Accepted: §a
        assert(
            VisitorAPI.isVisitorInfo(
                mutableListOf(
                    "§a§lVisitor Info",
                    "§7§oClick to view info about this visitor.",
                    "§7§oClick to view info about this visitor.",
                    "§7Offers Accepted: §a",
                )
            )
        )
    }
}
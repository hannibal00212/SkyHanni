package at.hannibal2.skyhanni.utils

object HypixelCommands {

    fun bazaar(searchTerm: String) {
        send("bz $searchTerm")
    }

    fun teleportToPlot(plotName: String) {
        send("tptoplot $plotName")
    }

    fun getFromSacks(itemName: String, amount: Int) {
        send("gfs $itemName $amount")
    }

    fun widget() {
        send("widget")
    }

    fun chocolateFactory() {
        send("cf")
    }

    fun openBaker() {
        send("openbaker")
    }

    fun gardenLevels() {
        send("gardenlevels")
    }

    fun calendar() {
        send("calendar")
    }

    fun sacks() {
        send("sax")
    }

    fun toggleMusic() {
        send("togglemusic")
    }

    private fun send(command: String) {
        ChatUtils.sendCommandToServer(command)
    }
}

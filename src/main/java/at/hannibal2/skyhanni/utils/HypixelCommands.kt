package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.api.GetFromSackApi
import at.hannibal2.skyhanni.utils.ChatUtils.debug
import at.hannibal2.skyhanni.utils.ChatUtils.sendMessageToServer
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName

object HypixelCommands {
    fun skyblock() {
        send("skyblock")
    }

    fun bazaar(searchTerm: String) {
        send("bz $searchTerm")
    }

    fun auctionSearch(searchTerm: String) {
        send("ahs $searchTerm")
    }

    fun playtime() {
        send("playtime")
    }

    fun skills() {
        send("skills")
    }

    fun viewRecipe(itemName: String) {
        send("viewrecipe $itemName")
    }

    fun recipe(itemName: String) {
        send("recipe $itemName")
    }

    fun npcOption(npc: String, answer: String) {
        send("selectnpcoption $npc $answer")
    }

    fun warp(warp: String) {
        send("warp $warp")
    }

    fun island() {
        send("is")
    }

    fun gardenDesk() {
        send("desk")
    }

    fun teleportToPlot(plotName: String) {
        send("plottp $plotName")
    }

    fun gardenLevels() {
        send("gardenlevels")
    }

    fun setHome() {
        send("sethome")
    }

    fun getFromSacks(itemName: String, amount: Int) {
        GetFromSackApi.getFromSack(itemName.toInternalName(), amount)
    }

    fun widget() {
        send("widget")
    }

    fun chocolateFactory() {
        send("cf")
    }

    fun pet() {
        send("pet")
    }

    fun openBaker() {
        send("openbaker")
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

    fun bingo() {
        send("bingo")
    }

    fun wiki(text: String) {
        send("wiki $text")
    }

    fun backPack(position: Int) {
        send("bp $position")
    }

    fun enderChest(position: Int) {
        send("ec $position")
    }

    fun partyWarp() {
        send("party warp")
    }

    fun partyTransfer(player: String) {
        send("party transfer $player")
    }

    fun partyDisband() {
        send("party disband")
    }

    fun partyKick(player: String) {
        send("party kick $player")
    }

    fun partyKickOffline() {
        send("party kickoffline")
    }

    fun partyAllInvite() {
        send("party settings allinvite")
    }

    fun partyPromote(player: String) {
        send("party promote $player")
    }

    fun partyChat(message: String) {
        send("pc $message")
    }

    fun allChat(message: String) {
        send("ac $message")
    }

    fun particleQuality(quality: String) {
        send("pq $quality")
    }

    // Changes the speed of Rancher's Boots
    fun setMaxSpeed(speed: Int? = null) = when {
        speed == null -> send("setmaxspeed")
        else -> send("setmaxspeed $speed")
    }

    fun showRng(major: String? = null, minor: String? = null) = when {
        major == null || minor == null -> send("rng")
        else -> send("rng $major $minor")
    }

    fun chatPrompt(prompt: String) {
        send("chatprompt $prompt")
    }

    fun callback(uuid: String) {
        send("cb $uuid")
    }

    fun bank() {
        send("bank")
    }

    fun pickupStash() {
        send("pickupstash")
    }

    fun viewStash(type: String) {
        send("viewstash $type")
    }

    fun locraw() {
        send("locraw")
    }

    private fun send(command: String) {
        if (command.startsWith("/")) {
            debug("Sending wrong command to server? ($command)")
        }
        sendMessageToServer("/$command")
    }
}

package at.hannibal2.skyhanni.data.hypixel.chat.event

import at.hannibal2.skyhanni.utils.ComponentSpan
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.fml.common.eventhandler.ListenerList
import org.jetbrains.annotations.ApiStatus

class PlayerAllChatEvent(
    val levelComponent: ComponentSpan?,
    val privateIslandRank: ComponentSpan?,
    val privateIslandGuest: ComponentSpan?,
    val chatColor: String,
    authorComponent: ComponentSpan,
    messageComponent: ComponentSpan,
    chatComponent: IChatComponent,
    blockedReason: String? = null,
) : AbstractChatEvent(authorComponent, messageComponent, chatComponent, blockedReason) {
    val levelColor = levelComponent?.sampleStyleAtStart()?.color
    val level by lazy { levelComponent?.getText()?.toInt() }
    val isAGuest by lazy { privateIslandGuest != null }

    companion object {
        private val listenerList = ListenerList(
            AbstractChatEvent(
                ComponentSpan.empty(),
                ComponentSpan.empty(),
                ChatComponentText(""),
                ""
            ).listenerList
        )
    }

    /**
     * This method is here to prevent FML from trying to add its own constructor. FML adds a public no args
     * constructor to every Event class, which is used to determine the listener list using inheritance. This is done
     * using class transformations, more specifically ASM. Sadly this class contains expressions which cause the class
     * writer used by ASM to perform type unifications. Due to the way ASM is loaded these type unifications do not
     * have access to some of the classes used in this class, causing a ClassNotFoundException. In order to prevent
     * these unifications from happening we need to prevent FML from trying to generate this constructor, which we do
     * by having our own setup function.
     */
    override fun setup() {
        super.setup()
    }

    /**
     * This method is required if [setup] is present.
     */
    @ApiStatus.Internal
    constructor() : this(
        null, null, null, "",
        ComponentSpan.empty(), ComponentSpan.empty(), ChatComponentText("")
    )

    /**
     * This method is required if [setup] is present.
     */
    override fun getListenerList(): ListenerList {
        return PlayerAllChatEvent.listenerList
    }
}

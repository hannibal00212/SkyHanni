package at.hannibal2.skyhanni.mixins.hooks

import net.minecraft.util.IChatComponent

interface ChatLineData {
    @Suppress("ktlint:standard:property-naming", "VariableNaming", "PropertyName")
    var skyHanni_fullComponent: IChatComponent
}

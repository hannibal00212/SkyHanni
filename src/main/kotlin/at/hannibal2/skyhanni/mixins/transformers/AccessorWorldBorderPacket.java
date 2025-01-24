package at.hannibal2.skyhanni.mixins.transformers;

import net.minecraft.network.play.server.S44PacketWorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(S44PacketWorldBorder.class)
public interface AccessorWorldBorderPacket {
    @Accessor("action")
    S44PacketWorldBorder.Action getAction();

    @Accessor("warningTime")
    int getWarningTime();
}

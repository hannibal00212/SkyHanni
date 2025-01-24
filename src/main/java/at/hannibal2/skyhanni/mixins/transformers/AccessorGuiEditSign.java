package at.hannibal2.skyhanni.mixins.transformers;

import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.tileentity.TileEntitySign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiEditSign.class)
public interface AccessorGuiEditSign {
    @Accessor("tileSign")
    TileEntitySign getTileSign();

    @Accessor("editLine")
    int getEditLine();
}

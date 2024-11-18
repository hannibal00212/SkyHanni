package at.hannibal2.skyhanni.mixins.transformers;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface AccessorKeyBinding {

    @Accessor("pressed")
    boolean getPressed_skyhanni();

    @Accessor("hash")
    IntHashMap<KeyBinding> getHash_skyhanni();
}

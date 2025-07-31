package dev.zenolth.the_fog.common.mixin;

import net.minecraft.server.dedicated.gui.PlayerListGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Vector;

@Mixin(PlayerListGui.class)
public class PlayerListGuiMixin {
    @ModifyVariable(method = "tick",at = @At("STORE"),name = "vector")
    public Vector<String> tick(Vector<String> value) {

        value.add("hi mom");

        return value;
    }
}

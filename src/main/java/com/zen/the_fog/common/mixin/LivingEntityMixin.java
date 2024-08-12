package com.zen.the_fog.common.mixin;

import com.zen.the_fog.common.entity.the_man.TheManEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyVariable(method = "travel", at = @At("STORE"), name = "h", ordinal = 0)
    public float fakeDepthStrider(float depthStriderBonus) {
        if ((Object) this instanceof TheManEntity theMan) {
            if (theMan.isTouchingWater()) {
                return 0.8f;
            }
        }

        return depthStriderBonus;
    }
}

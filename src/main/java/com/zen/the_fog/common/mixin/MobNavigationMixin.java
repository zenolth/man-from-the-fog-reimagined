package com.zen.the_fog.common.mixin;


import com.zen.the_fog.common.entity.the_man.TheManEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobNavigation.class)
public abstract class MobNavigationMixin extends EntityNavigation {

    public MobNavigationMixin(MobEntity entity, World world) {
        super(entity, world);
    }

    @Inject(method = "isAtValidPosition", at = @At("RETURN"),cancellable = true)
    public void isValidWhenClimbing(CallbackInfoReturnable<Boolean> cir) {
        if (this.entity instanceof TheManEntity theMan) {
            if (!cir.getReturnValue() && (theMan.isClimbing() || theMan.isCrouching() || theMan.isCrawling())) {
                cir.setReturnValue(true);
            }
        }
    }
}

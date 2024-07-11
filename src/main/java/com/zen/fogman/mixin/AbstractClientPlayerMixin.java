package com.zen.fogman.mixin;

import com.mojang.authlib.GameProfile;
import com.zen.fogman.mixininterfaces.AbstractClientPlayerInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerMixin extends PlayerEntity implements AbstractClientPlayerInterface {

    @Unique
    public float fovModifier = 1.0f;

    public AbstractClientPlayerMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "getFovMultiplier", at=@At("RETURN"), cancellable = true)
    private void fovMultiplierInjected(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * this.fovModifier);
    }

    @Override
    public void setFovModifier(float fovModifier) {
        this.fovModifier = fovModifier;
    }

    @Override
    public float getFovModifier() {
        return this.fovModifier;
    }
}

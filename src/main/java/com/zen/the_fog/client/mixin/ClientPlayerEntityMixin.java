package com.zen.the_fog.client.mixin;

import com.mojang.authlib.GameProfile;
import com.zen.the_fog.client.mixin_interfaces.ClientPlayerEntityInterface;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements ClientPlayerEntityInterface {
    @Unique
    private float the_fog_is_coming$glitchMultiplier = 0;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public void the_fog_is_coming$setGlitchMultiplier(float value) {
        this.the_fog_is_coming$glitchMultiplier = value;
    }

    @Override
    public float the_fog_is_coming$getGlitchMultiplier() {
        return this.the_fog_is_coming$glitchMultiplier;
    }
}

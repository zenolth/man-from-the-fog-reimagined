package com.zen.the_fog.common.mixin;

import com.zen.the_fog.common.mixin_interfaces.LookingAtManInterface;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements LookingAtManInterface {

    @Unique
    private boolean the_fog_is_coming$lookingAtMan = false;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean the_fog_is_coming$isLookingAtMan() {
        return this.the_fog_is_coming$lookingAtMan;
    }

    @Override
    public void the_fog_is_coming$setLookingAtMan(boolean lookingAtMan) {
        this.the_fog_is_coming$lookingAtMan = lookingAtMan;
    }
}

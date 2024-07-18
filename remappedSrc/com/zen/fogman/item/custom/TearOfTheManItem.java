package com.zen.fogman.common.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class TearOfTheManItem extends Item {

    private boolean hasEffects = false;

    public TearOfTheManItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            if (entity.isPlayer() && entity instanceof ServerPlayerEntity player) {

                if (selected || player.getOffHandStack() == stack) {
                    if (!hasEffects) {
                        if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE));
                        }

                        if (!player.hasStatusEffect(StatusEffects.SPEED)) {
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, StatusEffectInstance.INFINITE));
                        }
                        hasEffects = true;
                    }
                } else {
                    if (hasEffects) {
                        if (player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                        }

                        if (player.hasStatusEffect(StatusEffects.SPEED)) {
                            player.removeStatusEffect(StatusEffects.SPEED);
                        }
                        hasEffects = false;
                    }
                }

            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}

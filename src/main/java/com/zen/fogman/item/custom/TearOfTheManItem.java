package com.zen.fogman.item.custom;

import com.zen.fogman.ManFromTheFog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TearOfTheManItem extends Item {
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
        if (entity.isPlayer() && entity instanceof LivingEntity) {

            if (selected || ((LivingEntity) entity).getOffHandStack() == stack) {
                if (!((LivingEntity) entity).hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                    ((LivingEntity) entity).setStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,10),entity);
                }

                if (!((LivingEntity) entity).hasStatusEffect(StatusEffects.SPEED)) {
                    ((LivingEntity) entity).setStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,10),entity);
                }
            } else {
                if (((LivingEntity) entity).hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                    ((LivingEntity) entity).removeStatusEffect(StatusEffects.NIGHT_VISION);
                }

                if (((LivingEntity) entity).hasStatusEffect(StatusEffects.SPEED)) {
                    ((LivingEntity) entity).removeStatusEffect(StatusEffects.SPEED);
                }
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}

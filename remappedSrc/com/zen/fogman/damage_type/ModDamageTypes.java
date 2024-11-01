package com.zen.the_fog.common.damage_type;

import com.zen.the_fog.ManFromTheFog;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ModDamageTypes {
    public static final RegistryKey<DamageType> GENERIC_ATTACK_PIERCE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(ManFromTheFog.MOD_ID, "generic_piercing"));

    public static DamageSource of(World world, RegistryKey<DamageType> key) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
    }
}

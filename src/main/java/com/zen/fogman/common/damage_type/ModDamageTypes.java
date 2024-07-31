package com.zen.fogman.common.damage_type;

import com.zen.fogman.common.ManFromTheFog;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ModDamageTypes {
    public static final RegistryKey<DamageType> MAN_ATTACK_DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(ManFromTheFog.MOD_ID, "man_attack_damage_type"));

    public static void register() {

    }
}

package dev.zenolth.the_fog.common.damage_type;

import dev.zenolth.the_fog.common.FogMod;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModDamageTypes {
    public static final RegistryKey<DamageType> MAN_ATTACK_DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, FogMod.id("man_attack_damage_type"));

    public static void register() {

    }
}

package dev.zenolth.the_fog.common.world.dimension;

import dev.zenolth.the_fog.common.FogMod;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;

public class ModDimensions {
    public static final RegistryKey<DimensionOptions> ENSHROUDED_KEY = RegistryKey.of(RegistryKeys.DIMENSION,
            FogMod.id("enshrouded")
    );

    public static final RegistryKey<World> ENSHROUDED_LEVEL_KEY = RegistryKey.of(RegistryKeys.WORLD,
            FogMod.id("enshrouded")
    );

    public static final RegistryKey<DimensionType> ENSHROUDED_DIMENSION_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
            FogMod.id("enshrouded_type")
    );
}

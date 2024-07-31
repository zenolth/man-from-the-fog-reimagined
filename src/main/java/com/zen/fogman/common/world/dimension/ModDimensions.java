package com.zen.fogman.common.world.dimension;

import com.zen.fogman.common.ManFromTheFog;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;

public class ModDimensions {
    public static final RegistryKey<DimensionOptions> ENSHROUDED_KEY = RegistryKey.of(RegistryKeys.DIMENSION,
            new Identifier(ManFromTheFog.MOD_ID,"enshrouded")
    );

    public static final RegistryKey<World> ENSHROUDED_LEVEL_KEY = RegistryKey.of(RegistryKeys.WORLD,
            new Identifier(ManFromTheFog.MOD_ID,"enshrouded")
    );

    public static final RegistryKey<DimensionType> ENSHROUDED_DIMENSION_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
            new Identifier(ManFromTheFog.MOD_ID,"enshrouded_type")
    );
}

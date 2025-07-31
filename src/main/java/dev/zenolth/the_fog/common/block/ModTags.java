package dev.zenolth.the_fog.common.block;

import dev.zenolth.the_fog.common.FogMod;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class ModTags {
    public static final TagKey<Block> COVER = TagKey.of(RegistryKeys.BLOCK, FogMod.id("cover"));
}

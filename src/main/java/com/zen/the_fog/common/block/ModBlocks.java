package com.zen.the_fog.common.block;

import com.zen.the_fog.common.ManFromTheFog;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block BLEEDING_OBSIDIAN = registerBlock(
            new Block(FabricBlockSettings.create().mapColor(MapColor.BLACK).strength(50.0F,1200.0F).luminance(state -> 12).requiresTool()),
            "bleeding_obsidian",
            true
    );

    public static final CustomPortalBlock ENSHROUDED_PORTAL = registerBlock(
            new CustomPortalBlock(
                    FabricBlockSettings.create()
                            .noCollision()
                            .ticksRandomly()
                            .strength(-1.0F)
                            .sounds(BlockSoundGroup.GLASS)
                            .luminance(state -> 11)
                            .pistonBehavior(PistonBehavior.BLOCK)
            ),
            "enshrouded_portal",
            false
    );

    public static final LanternBlock EREBUS_LANTERN = registerBlock(
            new LanternBlock(
                    FabricBlockSettings.create()
                            .mapColor(MapColor.IRON_GRAY)
                            .solid()
                            .requiresTool()
                            .strength(3.5f)
                            .hardness(1.5f)
                            .sounds(BlockSoundGroup.LANTERN)
                            .luminance(state -> 15)
                            .nonOpaque()
                            .pistonBehavior(PistonBehavior.DESTROY)
            ),
            "erebus_lantern",
            true
    );

    public static <T extends Block> T registerBlock(T block, String name, boolean shouldRegisterItem) {
        Identifier id = Identifier.of(ManFromTheFog.MOD_ID, name);

        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, blockItem);
        }

        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void register() {

    }
}

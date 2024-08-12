package com.zen.the_fog.common.block;

import com.zen.the_fog.common.ManFromTheFog;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block BLEEDING_OBSIDIAN = registerBlock(
            new Block(AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(50.0F,1200.0F).luminance(state -> 12).requiresTool()),
            "bleeding_obsidian",
            true
    );

    public static final CustomPortalBlock ENSHROUDED_PORTAL = registerBlock(
            new CustomPortalBlock(
                    AbstractBlock.Settings.create()
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

    public static Block registerBlock(Block block, String name, boolean shouldRegisterItem) {
        Identifier id = Identifier.of(ManFromTheFog.MOD_ID, name);

        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, blockItem);
        }

        return Registry.register(Registries.BLOCK, id, block);
    }

    public static CustomPortalBlock registerBlock(CustomPortalBlock block, String name, boolean shouldRegisterItem) {
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

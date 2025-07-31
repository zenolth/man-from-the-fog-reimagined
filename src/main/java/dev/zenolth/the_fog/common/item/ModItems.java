package dev.zenolth.the_fog.common.item;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.block.ModBlocks;
import dev.zenolth.the_fog.common.entity.ModEntities;
import dev.zenolth.the_fog.common.item.custom.ClawsItem;
import dev.zenolth.the_fog.common.item.custom.ErebusOrb;
import dev.zenolth.the_fog.common.item.custom.TearOfTheManItem;
import dev.zenolth.the_fog.common.util.Console;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public class ModItems {

    public static final Item CLAWS = registerItem("mans_claws",new ClawsItem(
            ToolMaterials.NETHERITE,
            2,
            1f,
            new FabricItemSettings().maxCount(1).maxDamage(40).rarity(Rarity.EPIC).fireproof()
    ));

    public static final Item TEAR_OF_THE_MAN = registerItem("tear_of_the_man",new TearOfTheManItem(
            new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC).fireproof()
    ));

    public static final Item EREBUS_ORB = registerItem("erebus_orb", new ErebusOrb(
            new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC).fireproof()
    ));

    public static final Item THE_MAN_SPAWN_EGG = registerItem(
            "fogman_spawn_egg",
            new SpawnEggItem(ModEntities.THE_MAN,0xc4c4c4, 0xadadad, new FabricItemSettings())
    );

    public static final Item THE_MAN_HALLUCINATION_SPAWN_EGG = registerItem(
            "fogman_trippy_spawn_egg",
            new SpawnEggItem(ModEntities.THE_MAN_HALLUCINATION,0xc4c4c4, 0xadadad, new FabricItemSettings())
    );

    private static void addItemsToIngredientItemsGroup(FabricItemGroupEntries entries) {
        entries.add(TEAR_OF_THE_MAN);
    }

    private static void addItemsToSpawnEggsGroup(FabricItemGroupEntries entries) {
        entries.add(THE_MAN_SPAWN_EGG);
        entries.add(THE_MAN_HALLUCINATION_SPAWN_EGG);
    }

    private static void addItemsToCombatGroup(FabricItemGroupEntries entries) {
        entries.add(CLAWS);
    }

    private static void addItemsToNaturalGroup(FabricItemGroupEntries entries) {
        entries.add(ModBlocks.BLEEDING_OBSIDIAN.asItem());
    }

    private static void addItemsToFunctionalGroup(FabricItemGroupEntries entries) {
        entries.add(EREBUS_ORB);
        entries.add(ModBlocks.EREBUS_LANTERN.asItem());
    }

    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, FogMod.id(name), item);
    }

    public static void register() {
        Console.writeln("Registering Items");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientItemsGroup);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(ModItems::addItemsToCombatGroup);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(ModItems::addItemsToFunctionalGroup);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(ModItems::addItemsToSpawnEggsGroup);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(ModItems::addItemsToNaturalGroup);

        Console.writeln("Registered Items");
    }
}

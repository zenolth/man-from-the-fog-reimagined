package com.zen.fogman.common.item;

import com.zen.fogman.common.ManFromTheFog;
import com.zen.fogman.common.entity.ModEntities;
import com.zen.fogman.common.item.custom.ClawsItem;
import com.zen.fogman.common.item.custom.TearOfTheManItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {

    public static final Item CLAWS = registerItem("mans_claws",new ClawsItem(
            ToolMaterials.NETHERITE,
            2,
            1f,
            new FabricItemSettings().maxCount(1).maxDamage(40).rarity(Rarity.EPIC).fireproof()
    ));

    public static final Item TEAR_OF_THE_MAN = registerItem("tear_of_the_man",new TearOfTheManItem(
            new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC)
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

    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(ManFromTheFog.MOD_ID, name), item);
    }

    public static void registerModItems() {
        ManFromTheFog.LOGGER.info("Registering Items");
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientItemsGroup);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(ModItems::addItemsToCombatGroup);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(ModItems::addItemsToSpawnEggsGroup);
        ManFromTheFog.LOGGER.info("Registered Items");
    }
}

package com.zen.fogman.entity;

import com.zen.fogman.ManFromTheFog;
import com.zen.fogman.entity.custom.TheManEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<TheManEntity> THE_MAN = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(ManFromTheFog.MOD_ID, "man_from_the_fog"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TheManEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.3f)).build()
    );

    public static void registerEntities() {
        FabricDefaultAttributeRegistry.register(THE_MAN,TheManEntity.createManAttributes());
    }
}



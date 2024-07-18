package com.zen.fogman.common.entity;

import com.zen.fogman.common.ManFromTheFog;
import com.zen.fogman.common.entity.the_man.TheManEntity;
import com.zen.fogman.common.entity.the_man.TheManEntityHallucination;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityDimensions THE_MAN_HITBOX_SIZE = EntityDimensions.fixed(0.6f, 1.3f);

    public static final EntityType<TheManEntity> THE_MAN = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(ManFromTheFog.MOD_ID, "the_man"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TheManEntity::new).dimensions(THE_MAN_HITBOX_SIZE).build()
    );

    public static final EntityType<TheManEntityHallucination> THE_MAN_HALLUCINATION = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(ManFromTheFog.MOD_ID, "the_man_hallucination"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TheManEntityHallucination::new).dimensions(THE_MAN_HITBOX_SIZE).build()
    );

    public static void registerEntities() {
        ManFromTheFog.LOGGER.info("Registering Entities");
        FabricDefaultAttributeRegistry.register(THE_MAN, TheManEntity.createManAttributes());
        FabricDefaultAttributeRegistry.register(THE_MAN_HALLUCINATION, TheManEntity.createManAttributes());
        ManFromTheFog.LOGGER.info("Registered Entities");
    }
}



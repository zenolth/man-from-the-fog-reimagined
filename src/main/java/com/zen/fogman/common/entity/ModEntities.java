package com.zen.fogman.common.entity;

import com.zen.fogman.common.ManFromTheFog;
import com.zen.fogman.common.entity.the_man.TheManEntity;
import com.zen.fogman.common.entity.the_man.TheManEntityHallucination;
import com.zen.fogman.common.entity.the_man.TheManSpitEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<TheManEntity> THE_MAN = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(ManFromTheFog.MOD_ID, "the_man"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TheManEntity::new).dimensions(TheManEntity.HITBOX_SIZE).build()
    );

    public static final EntityType<TheManEntityHallucination> THE_MAN_HALLUCINATION = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(ManFromTheFog.MOD_ID, "the_man_hallucination"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TheManEntityHallucination::new).dimensions(TheManEntity.HITBOX_SIZE).build()
    );

    // Projectiles
    public static final EntityType<TheManSpitEntity> THE_MAN_SPIT = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(ManFromTheFog.MOD_ID,"the_man_spit"),
            FabricEntityTypeBuilder.<TheManSpitEntity>create(SpawnGroup.MISC, TheManSpitEntity::new).dimensions(EntityDimensions.fixed(0.5f,0.5f))
                    .build()
    );

    public static void register() {
        ManFromTheFog.LOGGER.info("Registering Entities");
        FabricDefaultAttributeRegistry.register(THE_MAN, TheManEntity.createManAttributes());
        FabricDefaultAttributeRegistry.register(THE_MAN_HALLUCINATION, TheManEntity.createManAttributes());
        ManFromTheFog.LOGGER.info("Registered Entities");
    }
}



package dev.zenolth.the_fog.common.entity;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.entity.mimic.MimicEntity;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntityHallucination;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntityParanoia;
import dev.zenolth.the_fog.common.entity.the_man.TheManSpitEntity;
import dev.zenolth.the_fog.common.util.Console;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEntities {
    public static final EntityType<TheManEntity> THE_MAN = Registry.register(
            Registries.ENTITY_TYPE,
            FogMod.id("the_man"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TheManEntity::new).dimensions(TheManEntity.HITBOX_SIZE).build()
    );

    public static final EntityType<TheManEntityHallucination> THE_MAN_HALLUCINATION = Registry.register(
            Registries.ENTITY_TYPE,
            FogMod.id("the_man_hallucination"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TheManEntityHallucination::new).dimensions(TheManEntity.HITBOX_SIZE).build()
    );

    public static final EntityType<TheManEntityParanoia> THE_MAN_PARANOIA = Registry.register(
            Registries.ENTITY_TYPE,
            FogMod.id("the_man_paranoia"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TheManEntityParanoia::new).dimensions(TheManEntity.HITBOX_SIZE).build()
    );

    public static final EntityType<MimicEntity> MIMIC = Registry.register(
            Registries.ENTITY_TYPE,
            FogMod.id("mimic"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, MimicEntity::new).dimensions(PlayerEntity.STANDING_DIMENSIONS).build()
    );

    // Projectiles
    public static final EntityType<TheManSpitEntity> THE_MAN_SPIT = Registry.register(
            Registries.ENTITY_TYPE,
            FogMod.id("the_man_spit"),
            FabricEntityTypeBuilder.<TheManSpitEntity>create(SpawnGroup.MISC, TheManSpitEntity::new).dimensions(EntityDimensions.fixed(0.25f,0.25f))
                    .build()
    );

    public static void register() {
        Console.writeln("Registering Entities");
        FabricDefaultAttributeRegistry.register(THE_MAN, TheManEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(THE_MAN_HALLUCINATION, TheManEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(THE_MAN_PARANOIA, TheManEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(MIMIC,MimicEntity.createMimicAttributes());
        Console.writeln("Registered Entities");
    }
}



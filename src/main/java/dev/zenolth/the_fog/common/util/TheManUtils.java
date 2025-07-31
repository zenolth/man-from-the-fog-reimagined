package dev.zenolth.the_fog.common.util;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.config.ModConfig;
import dev.zenolth.the_fog.common.entity.ModEntities;
import dev.zenolth.the_fog.common.predicate.TheManPredicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TheManUtils {

    /**
     * @param serverWorld The World to check
     * @return If any TheManEntityHallucination exist in serverWorld
     */
    public static boolean hallucinationsExists(ServerWorld serverWorld) {
        return !serverWorld.getEntitiesByType(ModEntities.THE_MAN_HALLUCINATION, TheManPredicates.THE_MAN_HALLUCINATION_ENTITY_PREDICATE).isEmpty();
    }

    /**
     * @param world The World to check
     * @return If any TheManEntity exist in serverWorld
     */
    public static boolean manExists(ServerWorld world) {
        if (FogMod.getTheMan(world) != null) return true;
        return !world.getEntitiesByType(Util.LIVING_ENTITY_TYPE_FILTER, TheManPredicates.THE_MAN_ENTITY_PREDICATE).isEmpty();
    }

    public static void doLightning(World world, double x, double y, double z) {
        if (!FogMod.CONFIG.miscellaneous.summonCosmeticLightning) return;

        LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightningEntity.setCosmetic(true);
        lightningEntity.setInvulnerable(true);
        lightningEntity.setOnFire(false);
        lightningEntity.setPosition(x, y, z);
        world.spawnEntity(lightningEntity);
    }

    public static void doLightning(World world, Vec3d position) {
        doLightning(world,position.getX(),position.getY(),position.getZ());
    }

    public static void doLightning(World world, Entity entity) {
        doLightning(world,entity.getX(),entity.getY(),entity.getZ());
    }
}

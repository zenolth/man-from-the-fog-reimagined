package com.zen.the_fog.common.entity.the_man;

import com.zen.the_fog.common.entity.ModEntities;
import com.zen.the_fog.common.gamerules.ModGamerules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class TheManUtils {

    /**
     * @param serverWorld The World to check
     * @return If any TheManEntityHallucination exist in serverWorld
     */
    public static boolean hallucinationsExists(ServerWorld serverWorld) {
        return !serverWorld.getEntitiesByType(ModEntities.THE_MAN_HALLUCINATION, TheManPredicates.VALID_MAN_HALLUCINATION).isEmpty();
    }

    /**
     * @param serverWorld The World to check
     * @return If any TheManEntity exist in serverWorld
     */
    public static boolean manExists(ServerWorld serverWorld) {
        return !serverWorld.getEntitiesByType(ModEntities.THE_MAN, TheManPredicates.VALID_MAN).isEmpty();
    }

    public static void doLightning(ServerWorld serverWorld, double x, double y, double z) {
        if (!serverWorld.getGameRules().getBoolean(ModGamerules.MAN_CAN_SUMMON_LIGHTNING)) return;

        LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, serverWorld);
        lightningEntity.setCosmetic(true);
        lightningEntity.setInvulnerable(true);
        lightningEntity.setOnFire(false);
        lightningEntity.setPosition(x, y, z);
        serverWorld.spawnEntity(lightningEntity);
    }

    public static void doLightning(ServerWorld serverWorld, Vec3d position) {
        doLightning(serverWorld,position.getX(),position.getY(),position.getZ());
    }

    public static void doLightning(ServerWorld serverWorld, Entity entity) {
        doLightning(serverWorld,entity.getX(),entity.getY(),entity.getZ());
    }
}

package com.zen.fogman;

import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.entity.custom.ManState;
import com.zen.fogman.entity.custom.TheManEntity;
import com.zen.fogman.item.ModItems;
import com.zen.fogman.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class ManFromTheFog implements ModInitializer {
	public static final String MOD_ID = "the_fog_is_coming";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static long lastRandomTime = 0;
	public static Random random = new Random();

	@Override
	public void onInitialize() {
		ModSounds.registerSounds();
		ModItems.registerModItems();
		ModEntities.registerEntities();
		ServerTickEvents.END_WORLD_TICK.register((serverWorld) -> {
			if (serverWorld.isClient()) {
				return;
			}
			if (serverWorld.isDay()) {
				return;
			}
			List<? extends TheManEntity> entities = serverWorld.getEntitiesByType(ModEntities.THE_MAN, EntityPredicates.VALID_LIVING_ENTITY);
			if (!entities.isEmpty()) {
				return;
			}
			if (serverWorld.getTime() - lastRandomTime > 1) {
				if (random.nextInt(0,401) == 400) {
					ServerPlayerEntity player = serverWorld.getRandomAlivePlayer();
					if (player == null) {
						LOGGER.info("No player found");
						return;
					}
					int xOffset = (random.nextBoolean() ? 1 : -1) * random.nextInt(20,60);
					int zOffset = (random.nextBoolean() ? 1 : -1) * random.nextInt(20,60);
					Vec3d spawnPosition = serverWorld.getTopPosition(Heightmap.Type.WORLD_SURFACE,player.getBlockPos().add(xOffset,0,zOffset)).toCenterPos();
					TheManEntity man = new TheManEntity(ModEntities.THE_MAN,serverWorld);
					man.setPosition(spawnPosition);
					man.setTarget(player);
					serverWorld.spawnEntity(man);
					LOGGER.info("Spawned The Man");
				}
				lastRandomTime = serverWorld.getTime();
			}
		});
	}
}
package com.zen.fogman;

import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.entity.custom.TheManEntity;
import com.zen.fogman.item.ModItems;
import com.zen.fogman.other.MathUtils;
import com.zen.fogman.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Random;

public class ManFromTheFog implements ModInitializer {
	public static final String MOD_ID = "the_fog_is_coming";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static long lastRandomTime = 0;
	public static Random random = new Random();

	/**
	 * Generates a random position around position
	 * @param serverWorld The World
	 * @param position Position to generate around
	 * @param minRange Minimum range to generate
	 * @param maxRange Maximum range to generate
	 * @return The generated position
	 */
	public static Vec3d getRandomSpawnPositionAtPoint(ServerWorld serverWorld, BlockPos position, int minRange,int maxRange) {
		int xOffset = (random.nextBoolean() ? 1 : -1) * random.nextInt(minRange,maxRange);
		int zOffset = (random.nextBoolean() ? 1 : -1) * random.nextInt(minRange,maxRange);
		return serverWorld.getTopPosition(Heightmap.Type.WORLD_SURFACE,position.add(xOffset,0,zOffset)).toCenterPos();
	}

	/**
	 * Generates a random position around position
	 * @param serverWorld The World
	 * @param position Position to generate around
	 * @return The generated position
	 */
	public static Vec3d getRandomSpawnPositionAtPoint(ServerWorld serverWorld, BlockPos position) {
		int xOffset = (random.nextBoolean() ? 1 : -1) * random.nextInt(20,60);
		int zOffset = (random.nextBoolean() ? 1 : -1) * random.nextInt(20,60);
		return serverWorld.getTopPosition(Heightmap.Type.WORLD_SURFACE,position.add(xOffset,0,zOffset)).toCenterPos();
	}

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
			if (TheManEntity.manExist(serverWorld)) {
				return;
			}
			if (MathUtils.tickToSec(serverWorld.getTime()) - lastRandomTime > 1) {
				if (random.nextFloat(0f,1f) < 0.05) {
					ServerPlayerEntity player = serverWorld.getRandomAlivePlayer();
					if (player == null) {
						LOGGER.info("No player found");
						return;
					}
					Vec3d spawnPosition = ManFromTheFog.getRandomSpawnPositionAtPoint(serverWorld,player.getBlockPos());
					TheManEntity man = new TheManEntity(ModEntities.THE_MAN,serverWorld);
					man.setPosition(spawnPosition);
					man.setTarget(player);
					serverWorld.spawnEntity(man);
					LOGGER.info("Spawned The Man");
				}
				lastRandomTime = MathUtils.tickToSec(serverWorld.getTime());
			}
		});
	}
}
package dev.zenolth.the_fog.common.entity.the_man;

import dev.zenolth.the_fog.common.data_tracker.TrackingData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class TheManEntityParanoia extends TheManEntityHallucination {

    public static final TrackedData<Optional<UUID>> OWNER = TrackingData.register(TheManEntityParanoia.class,TrackedDataHandlerRegistry.OPTIONAL_UUID);

    public final TrackingData<TheManEntityParanoia,Optional<UUID>> owner;

    public TheManEntityParanoia(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType, world);
        this.owner = new TrackingData<>(this,OWNER,Optional.empty());
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return true;
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return true;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        var ownerUUID = this.owner.get();
        return ownerUUID.map(value -> this.getWorld().getPlayerByUuid(value)).orElse(null);
    }

    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        var ownerUUID = this.owner.get();
        if (ownerUUID.isEmpty()) return true;
        var ownerEntity = this.getWorld().getPlayerByUuid(ownerUUID.get());
        if (ownerEntity == null || ownerEntity.isDead()) return true;
        return player.getUuid() != ownerUUID.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0;
    }

    @Override
    public float getLoudSoundVolume() {
        return 0;
    }
}

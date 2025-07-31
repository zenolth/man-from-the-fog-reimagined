package dev.zenolth.the_fog.common.components;

import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.util.TimeHelper;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WorldComponent implements AutoSyncedComponent {
    public static final String THE_MAN_DAY_KILLED = "theManDayKilled";
    public static final String THE_MAN_KILL_COUNT = "theManKillCount";
    public static final String THE_MAN_ENTITY_ID = "theManEntityId";
    public static final String THE_MAN_HEALTH = "theManHealth";
    public static final String THE_MAN_SPAWN_ATTEMPT_TICKS = "theManSpawnAttemptTicks";

    private final World world;

    private long dayKilled = 0;
    private int killCount = 0;
    @Nullable private Integer theManId;
    private float theManHealth = (float) TheManEntity.createAttributes().build().getValue(EntityAttributes.GENERIC_MAX_HEALTH);
    private long spawnAttemptTicks = TimeHelper.secToTick(15.0);

    public WorldComponent(World world) {
        this.world = world;
    }

    public static WorldComponent get(World world) {
        return ModComponents.WORLD_COMPONENT.get(world);
    }

    public static void sync(World world) {
        if (world instanceof ServerWorld serverWorld) {
            ModComponents.WORLD_COMPONENT.sync(serverWorld);
        }
    }

    public static void syncWith(ServerPlayerEntity player) {
        ModComponents.WORLD_COMPONENT.syncWith(player,player.getServerWorld().asComponentProvider());
    }

    public long dayKilled() { return this.dayKilled; }

    public void setDayKilled(long value) {
        this.dayKilled = value;
        sync(this.getWorld());
    }

    public int killCount() { return this.killCount; }

    public void setKillCount(int value) {
        this.killCount = value;
        sync(this.getWorld());
    }

    public Optional<Integer> theManId() {
        if (this.theManId == null) return Optional.empty();
        return Optional.of(this.theManId);
    }

    public void setTheManId(@Nullable Integer value) {
        this.theManId = value;
        sync(this.getWorld());
    }

    public float theManHealth() { return this.theManHealth; }

    public void setTheManHealth(float value) {
        this.theManHealth = value;
        sync(this.getWorld());
    }

    public void setSpawnAttemptTicks(long value) {
        this.spawnAttemptTicks = value;
        sync(this.getWorld());
    }

    public long spawnAttemptTicks() { return this.spawnAttemptTicks; }

    public World getWorld() { return this.world; }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.dayKilled = tag.getLong(THE_MAN_DAY_KILLED);
        this.killCount = tag.getInt(THE_MAN_KILL_COUNT);
        if (tag.contains(THE_MAN_ENTITY_ID, NbtElement.INT_TYPE)) {
            this.theManId = tag.getInt(THE_MAN_ENTITY_ID);
        }
        this.theManHealth = tag.getFloat(THE_MAN_HEALTH);
        this.spawnAttemptTicks = tag.getLong(THE_MAN_SPAWN_ATTEMPT_TICKS);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong(THE_MAN_DAY_KILLED,this.dayKilled);
        tag.putInt(THE_MAN_KILL_COUNT,this.killCount);
        if (this.theManId != null) {
            tag.putInt(THE_MAN_ENTITY_ID,this.theManId);
        } else {
            if (tag.contains(THE_MAN_ENTITY_ID,NbtElement.INT_TYPE)) {
                tag.remove(THE_MAN_ENTITY_ID);
            }
        }
        tag.putFloat(THE_MAN_HEALTH,this.theManHealth);
        tag.putLong(THE_MAN_SPAWN_ATTEMPT_TICKS,this.spawnAttemptTicks);
    }
}

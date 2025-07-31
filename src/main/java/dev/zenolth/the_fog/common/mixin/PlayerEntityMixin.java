package dev.zenolth.the_fog.common.mixin;

import dev.zenolth.the_fog.common.mixin_interfaces.GroupInterface;
import dev.zenolth.the_fog.common.mixin_interfaces.LookingAtManInterface;
import dev.zenolth.the_fog.common.server.ServerEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements LookingAtManInterface, GroupInterface {

    @Unique
    private boolean the_fog_is_coming$lookingAtMan = false;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean the_fog_is_coming$isLookingAtMan() {
        return this.the_fog_is_coming$lookingAtMan;
    }

    @Override
    public void the_fog_is_coming$setLookingAtMan(boolean lookingAtMan) {
        this.the_fog_is_coming$lookingAtMan = lookingAtMan;
    }

    @Override
    public int the_fog_is_coming$getPlayersInGroupCount() {
        var curPlayer = (PlayerEntity)(Object)this;
        var world = curPlayer.getWorld();
        if (world.isClient()) return 0;
        var serverWorld = (ServerWorld) world;
        var chunk = serverWorld.getChunk(curPlayer.getBlockPos());
        var chunkPos = chunk.getPos();
        var minHeight = curPlayer.getBlockY() - 5;
        var maxHeight = curPlayer.getBlockY() + 5;
        var box = new Box(chunkPos.getStartX(),minHeight,chunkPos.getStartZ(),chunkPos.getEndX(),maxHeight,chunkPos.getEndZ());
        var players = new ArrayList<PlayerEntity>();
        serverWorld.collectEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class),box,(player) -> player.getUuid() != curPlayer.getUuid() && ServerEvents.VALID_PLAYER_PREDICATE.test(player),players,serverWorld.getServer().getCurrentPlayerCount());
        return players.size();
    }
}

package dev.zenolth.the_fog.common.entity.mimic;

import dev.zenolth.the_fog.common.data_tracker.TrackingData;
import dev.zenolth.the_fog.common.entity.ModEntities;
import dev.zenolth.the_fog.common.entity.MonitorPlayerLineOfSight;
import dev.zenolth.the_fog.common.entity.OnSpawnEntity;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.server.chat.ChatLog;
import dev.zenolth.the_fog.common.sounds.ModSounds;
import dev.zenolth.the_fog.common.state_machine.StateMachine;
import dev.zenolth.the_fog.common.state_machine.StateMachineEntity;
import dev.zenolth.the_fog.common.state_machine.states.MimicState;
import dev.zenolth.the_fog.common.state_machine.states.TheManState;
import dev.zenolth.the_fog.common.state_machine.states.mimic.MimicPersonState;
import dev.zenolth.the_fog.common.state_machine.states.mimic.PrepareForRevealState;
import dev.zenolth.the_fog.common.state_machine.states.mimic.WanderState;
import dev.zenolth.the_fog.common.util.*;
import dev.zenolth.the_fog.common.util.Timer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MimicEntity extends HostileEntity implements StateMachineEntity<MimicState>, OnSpawnEntity, MonitorPlayerLineOfSight {

    public static final int MIN_CHAT_COOLDOWN = 600; // 600
    public static final int MAX_CHAT_COOLDOWN = 18000; // 18000
    public static final int RECAMOUFLAGE_COOLDOWN = 6000;

    public static final String MIMICKED_PLAYER_NAME_NBT = "MimickedPlayerName";

    public static final TrackedData<Integer> STATE = TrackingData.register(MimicEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public static final TrackedData<Optional<UUID>> MIMICKED_PLAYER_UUID = TrackingData.register(MimicEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    public static final TrackedData<Text> MIMICKED_PLAYER_DISPLAY_NAME = TrackingData.register(MimicEntity.class, TrackedDataHandlerRegistry.TEXT_COMPONENT);

    public final TrackingData<MimicEntity,Integer> state;

    public final TrackingData<MimicEntity,Optional<UUID>> mimickedPlayerUUID;
    public final TrackingData<MimicEntity,Text> mimickedPlayerDisplayName;

    private int chatCooldown = RandomNum.next(MIN_CHAT_COOLDOWN,MAX_CHAT_COOLDOWN);
    private final Timer recamouflageTimer = new Timer(RECAMOUFLAGE_COOLDOWN,true,this::recamouflage);

    private final HashSet<UUID> playersWithLOS = new HashSet<>();

    private final StateMachine<MimicEntity,MimicState> stateMachine;

    public MimicEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);

        this.recamouflageTimer.start();

        this.state = new TrackingData<>(this,STATE,MimicState.WANDER.ordinal());

        this.mimickedPlayerUUID = new TrackingData<>(this,MIMICKED_PLAYER_UUID,Optional.empty());
        this.mimickedPlayerDisplayName = new TrackingData<>(this,MIMICKED_PLAYER_DISPLAY_NAME,Text.empty());

        this.stateMachine = new StateMachine<>(this);
        this.stateMachine.add(MimicState.WANDER,new WanderState(this));
        this.stateMachine.add(MimicState.MIMIC,new MimicPersonState(this));
        this.stateMachine.add(MimicState.PREPARE_FOR_REVEAL,new PrepareForRevealState(this));
    }

    public static DefaultAttributeContainer.Builder createMimicAttributes() {
        return PlayerEntity.createPlayerAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,4.317f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE,100f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,5.5)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK,3.5)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,0.95)
                .add(EntityAttributes.GENERIC_ARMOR,7)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,5);
    }

    @Override
    public void onSpawn(ServerWorld world) {
        this.randomizeMimickedPlayer();
        this.setState(MimicState.WANDER);
    }

    @Override
    public StateMachine<MimicEntity, MimicState> getStateMachine() {
        return this.stateMachine;
    }

    public void setState(MimicState state) {
        this.state.set(state.ordinal());
        this.stateMachine.start();
    }

    public MimicState getState() {
        return MimicState.values()[this.state.get()];
    }

    @Nullable
    public ServerPlayerEntity getMimickedPlayer() {
        if (this.getWorld().isClient()) return null;
        var uuid = this.mimickedPlayerUUID.get();
        return uuid.map(value -> PlayerHelper.getPlayerById(this.getServerWorld().getServer(),value)).orElse(null);
    }
    
    @Override
    public Text getDisplayName() {
        return this.mimickedPlayerDisplayName.get();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        this.reveal(true);
        super.onDeath(damageSource);
    }

    @Override
    public int getMaxLookYawChange() {
        return RandomNum.next(3,7);
    }

    @Override
    public int getMaxLookPitchChange() {
        return 3;
    }

    @Override
    public float getMovementSpeed() {
        return 0.1f;
    }

    @Override
    protected float getOffGroundSpeed() {
        return this.getMovementSpeed() * 0.1f;
    }

    public void recamouflage() {
        if (this.getWorld().isClient()) return;
        var serverWorld = this.getServerWorld();
        if (this.mimickedPlayerUUID.get().isEmpty() || !PlayerHelper.isPlayerWithIdPresent(serverWorld.getServer(),this.mimickedPlayerUUID.get().get(),true)) {
            this.randomizeMimickedPlayer();
        }
    }

    @Override
    protected void mobTick() {
        if (this.getWorld().isClient()) return;
        this.recamouflageTimer.tick();

        var serverWorld = this.getServerWorld();

        this.stateMachine.tick(serverWorld);

        if (--this.chatCooldown <= 0L) {
            var mimickedPlayer = this.getMimickedPlayer();
            if (mimickedPlayer != null) {
                var playerId = mimickedPlayer.getGameProfile().getId();
                var logs = ChatLog.getInstance().getMessageLogs(playerId);
                if (logs.isPresent() && !logs.get().isEmpty()) {
                    var logArray = logs.get().toArray(new ChatLog.MessageLog[0]);
                    var pickedMessage = logArray[RandomNum.next(logArray.length)];

                    var message = SignedMessage.ofUnsigned(playerId,pickedMessage.content().getString());
                    var params = MessageType.params(MessageType.CHAT,mimickedPlayer);
                    mimickedPlayer.networkHandler.sendChatMessage(message,params);
                }
            }

            this.chatCooldown = RandomNum.next(MIN_CHAT_COOLDOWN,MAX_CHAT_COOLDOWN);
        }
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (this.getWorld().isClient()) return;

        if (nbt.containsUuid(MIMICKED_PLAYER_NAME_NBT)) {
            var mimickedPlayerName = nbt.getString(MIMICKED_PLAYER_NAME_NBT);
            @Nullable ServerPlayerEntity mimickedPlayer = null;
            for (var player : this.getServerWorld().getServer().getPlayerManager().getPlayerList()) {
                if (Objects.equals(player.getGameProfile().getName(), mimickedPlayerName)) {
                    mimickedPlayer = player;
                    break;
                }
            }
            if (mimickedPlayer != null) {
                this.mimickedPlayerUUID.set(Optional.of(mimickedPlayer.getGameProfile().getId()));
            } else {
                this.randomizeMimickedPlayer();
            }
        } else {
            this.randomizeMimickedPlayer();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        var mimickedPlayer = this.getMimickedPlayer();
        if (mimickedPlayer != null) {
            nbt.putString(MIMICKED_PLAYER_NAME_NBT,mimickedPlayer.getGameProfile().getName());
        }
    }

    public ServerWorld getServerWorld() {
        if (this.getWorld().isClient()) {
            throw new Error("Attempt to get a ServerWorld in a Client thread");
        }
        return (ServerWorld) this.getWorld();
    }

    public void playRevealSound(boolean jumpscare) {
        if (jumpscare) {
            this.playSound(ModSounds.MIMIC_REVEAL_JUMPSCARE,this.getSoundVolume() * 5f,1f);
        } else {
            this.playSound(ModSounds.MIMIC_REVEAL,this.getSoundVolume() * 0.5f,1f);
        }
    }

    public void reveal(boolean chase) {
        if (this.getWorld().isClient()) return;
        if (TheManUtils.manExists(this.getServerWorld())) return;

        var theMan = new TheManEntity(ModEntities.THE_MAN,this.getWorld());
        theMan.setPosition(this.getPos());

        this.getWorld().spawnEntity(theMan);

        this.playRevealSound(chase);

        if (chase) {
            theMan.startChase();
        } else {
            theMan.setState(TheManState.STALK);
        }

        this.discard();
    }

    public void reveal() {
        this.reveal(false);
    }

    public Vec3d getLookDirection() {
        return GeometryHelper.calculateDirection(0f,this.getYaw(1.0F)).normalize();
    }

    public void updateName() {
        if (this.getWorld().isClient()) return;

        var player = this.getMimickedPlayer();

        if (player != null) {
            this.mimickedPlayerDisplayName.set(player.getDisplayName());
            return;
        }

        var playerIdOptional = this.mimickedPlayerUUID.get();
        if (playerIdOptional.isEmpty()) return;

        var playerId = playerIdOptional.get();

        var server = this.getServerWorld().getServer();

        var cache = server.getUserCache();
        if (cache == null) return;

        var profile = cache.getByUuid(playerId);
        if (profile.isEmpty()) return;

        this.mimickedPlayerDisplayName.set(Text.of(profile.get().getName()));
    }

    public void randomizeMimickedPlayer() {
        if (this.getWorld().isClient()) return;
        var players = this.getWorld().getPlayers();
        if (players.isEmpty()) return;

        var serverWorld = this.getServerWorld();
        var uuids = getEveryPlayer(serverWorld);

        var pickedId = uuids.size() == 1 ? uuids.get(0) : uuids.get(RandomNum.next(0,Math.max(0,uuids.size() - 1)));

        this.mimickedPlayerUUID.set(Optional.of(pickedId));
        this.updateName();
    }

    private static @NotNull List<UUID> getEveryPlayer(ServerWorld serverWorld) {
        List<UUID> uuids = new ArrayList<>();

        /*var userCache = serverWorld.getServer().getUserCache();
        if (userCache != null) {
            serverWorld.getServer().getPlayerNames();
            for (var entry : userCache.load()) {
                uuids.add(entry.getProfile().getId());
            }
        }*/

        for (var player : serverWorld.getServer().getPlayerManager().getPlayerList()) {
            if (uuids.contains(player.getGameProfile().getId())) continue;
            uuids.add(player.getGameProfile().getId());
        }

        return uuids;
    }

    public void moveTo(double x,double y,double z) {
        this.getNavigation().startMovingTo(x,y,z,1.0f);
    }

    public void stopMoving() {
        this.getNavigation().stop();
        this.getNavigation().startMovingTo(this.getX(),this.getY(),this.getZ(),1.0f);
    }

    @Override
    public HashSet<UUID> getPlayersWithLOS() {
        return this.playersWithLOS;
    }
}

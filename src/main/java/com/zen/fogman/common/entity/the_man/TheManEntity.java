package com.zen.fogman.common.entity.the_man;

import com.zen.fogman.common.entity.ModEntities;
import com.zen.fogman.common.entity.the_man.states.*;
import com.zen.fogman.common.item.ModItems;
import com.zen.fogman.common.other.Util;
import com.zen.fogman.common.sounds.ModSounds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Arrays;

public class TheManEntity extends HostileEntity implements GeoEntity {
    public static final double MAN_SPEED = 0.48;
    public static final double MAN_CLIMB_SPEED = 0.7;
    public static final double MAN_MAX_SCAN_DISTANCE = 10000.0;
    public static final double MAN_BLOCK_CHANCE = 0.1;
    public static final int MAN_CHASE_DISTANCE = 200;

    public static Block[] MAN_BREAK_WHITELIST = {
            Blocks.CHEST,
            Blocks.ENDER_CHEST,
            Blocks.DIAMOND_BLOCK,
            Blocks.DIAMOND_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.NETHERITE_BLOCK,
            Blocks.ANCIENT_DEBRIS,
            Blocks.EMERALD_BLOCK,
            Blocks.EMERALD_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.BEDROCK,
            Blocks.BEEHIVE,
            Blocks.BEE_NEST,
            Blocks.ACACIA_LOG,
            Blocks.RAIL,
            Blocks.ACTIVATOR_RAIL,
            Blocks.DETECTOR_RAIL,
            Blocks.POWERED_RAIL,
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.CAULDRON,
            Blocks.LAVA_CAULDRON,
            Blocks.WATER_CAULDRON,
            Blocks.BARREL,
            Blocks.BARRIER,
            Blocks.HOPPER
    };

    /* NBT data names */
    public static final String MAN_STATE_NBT = "ManState";
    public static final String MAN_ALIVE_TICKS_NBT = "ManAliveTicks";

    // Animation cache stuff
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private Path path;

    /* Cooldowns */
    // Attack cooldown
    private long attackCooldown;
    // Alive ticks
    private long aliveTicks;

    // State manager
    private final StateManager stateManager;

    // Booleans
    private boolean lookedAt = false;

    // Velocities
    private Vec3d climbPosition = Vec3d.ZERO;

    public TheManEntity(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType,world);

        this.attackCooldown = Util.secToTick(this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED));
        this.aliveTicks = Util.secToTick(this.getRandom().nextBetween(30,120));
        this.stateManager = new StateManager(this);

        this.addStatusEffects();
        this.initStates();
        this.initPathfindingPenalties();
    }

    /* Initialization */

    public void onSpawn(ServerWorld serverWorld) {
        this.setNoGravity(false);
        this.setNoDrag(false);
        if (!this.isHallucination()) {
            switch (this.getRandom().nextBetween(0,2)) {
                case 0:
                    this.setState(TheManState.STARE);
                    break;
                case 1:
                    this.setState(TheManState.STALK);
                    break;
            }
        } else {
            this.startChase();
        }

    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        if (packet.getEntityData() == 0 && !this.getWorld().isClient()) {
            this.onSpawn(this.getServerWorld());
        }
    }

    public void initPathfindingPenalties() {
        this.setPathfindingPenalty(PathNodeType.WALKABLE,4.0f);
        this.setPathfindingPenalty(PathNodeType.OPEN,2.0f);
        this.setPathfindingPenalty(PathNodeType.UNPASSABLE_RAIL,0);
        this.setPathfindingPenalty(PathNodeType.FENCE,0);
        this.setPathfindingPenalty(PathNodeType.DOOR_WOOD_CLOSED,0);
        this.setPathfindingPenalty(PathNodeType.DOOR_IRON_CLOSED,0);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE,-1);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE,-1);
        this.setPathfindingPenalty(PathNodeType.LEAVES,0);
        this.setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW,-1);
        this.setPathfindingPenalty(PathNodeType.TRAPDOOR,0);
    }

    public void initStates() {
        this.stateManager.add(TheManState.CHASE,new ChaseState(this));
        this.stateManager.add(TheManState.STARE,new StareState(this));
        this.stateManager.add(TheManState.FLEE,new FleeState(this));
        this.stateManager.add(TheManState.STALK,new StalkState(this));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        SpiderNavigation mobNavigation = new SpiderNavigation(this,world);

        mobNavigation.setCanEnterOpenDoors(true);
        mobNavigation.setCanPathThroughDoors(true);
        mobNavigation.setCanSwim(true);
        mobNavigation.setCanWalkOverFences(true);

        return mobNavigation;
    }

    /* Attributes */
    public static DefaultAttributeContainer.Builder createManAttributes() {
        return TheManEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH,350)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,MAN_SPEED)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,7)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK,4)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,0.4)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE,MAN_MAX_SCAN_DISTANCE)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,100)
                .add(EntityAttributes.GENERIC_ARMOR,7)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,5);
    }

    /* States */
    public StateManager getStateManager() {
        return this.stateManager;
    }

    public void setState(TheManState state) {
        this.getDataTracker().set(TheManDataTrackers.STATE,state.ordinal());
    }

    public TheManState getState() {
        return TheManState.values()[this.getDataTracker().get(TheManDataTrackers.STATE)];
    }

    public void startChase() {
        if (this.getState() == TheManState.CHASE) {
            return;
        }
        this.setState(TheManState.CHASE);
        this.playAlarmSound();
        TheManUtils.doLightning(this.getServerWorld(),this);
    }

    /* Data trackers */
    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(TheManDataTrackers.CLIMBING,false);
        this.getDataTracker().startTracking(TheManDataTrackers.STATE,TheManState.STARE.ordinal());
        this.getDataTracker().startTracking(TheManDataTrackers.IS_LUNGING,false);
    }

    public void setClimbing(boolean climbing) {
        this.getDataTracker().set(TheManDataTrackers.CLIMBING, climbing);
    }

    public boolean climbing() {
        return this.getDataTracker().get(TheManDataTrackers.CLIMBING);
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    public void setLookedAt(boolean lookedAt) {
        if (this.getWorld().isClient()) {
            PacketByteBuf packet = PacketByteBufs.create();
            packet.writeInt(this.getId());
            packet.writeBoolean(lookedAt);
            ClientPlayNetworking.send(TheManPackets.LOOKED_AT_PACKET_ID,packet);
        } else {
            this.lookedAt = lookedAt;
        }
    }

    public boolean isLookedAt() {
        return this.lookedAt;
    }

    public void setLunging(boolean lunging) {
        this.getDataTracker().set(TheManDataTrackers.IS_LUNGING,lunging);
    }

    public boolean isLunging() {
        return this.getDataTracker().get(TheManDataTrackers.IS_LUNGING);
    }

    /* NBT data */

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt(MAN_STATE_NBT,this.getState().ordinal());
        nbt.putLong(MAN_ALIVE_TICKS_NBT,this.aliveTicks);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains(MAN_STATE_NBT)) {
            this.setState(TheManState.values()[nbt.getInt(MAN_STATE_NBT)]);
        }
        if (nbt.contains(MAN_ALIVE_TICKS_NBT)) {
            this.aliveTicks = nbt.getLong(MAN_ALIVE_TICKS_NBT);
        }
    }

    /* Animations */
    private PlayState predictate(AnimationState<TheManEntity> event) {
        if (this.climbing()) {
            return event.setAndContinue(TheManAnimations.CLIMB);
        }

        if (event.isMoving()) {
            return event.setAndContinue(TheManAnimations.RUN);
        }

        return event.setAndContinue(TheManAnimations.IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this,"controller", Util.secToTick(0.1),this::predictate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    /* Hallucinations */
    public boolean isHallucination() {
        return false;
    }

    /* Sounds */
    @Override
    protected float getSoundVolume() {
        return 3.0f;
    }

    public float getLoudSoundVolume() {
        return 8.0f;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.MASTER;
    }

    public void playAlarmSound() {
        this.playSound(ModSounds.MAN_ALARM,this.getLoudSoundVolume(),this.getSoundPitch());
    }

    public void playLungeSound() {
        this.playSound(ModSounds.MAN_LUNGE,3.0f,this.getSoundPitch());
    }

    public void playSlashSound() {
        this.playSound(ModSounds.MAN_SLASH,this.getSoundVolume() + 1f,1.0f);
    }

    public void playAttackSound() {
        this.playSound(ModSounds.MAN_ATTACK,this.getSoundVolume(),this.getSoundPitch());
    }

    public void playLungeAttackSound() {
        this.playSound(ModSounds.MAN_LUNGE_ATTACK,this.getLoudSoundVolume(),this.getSoundPitch());
    }

    @Override
    public void playAmbientSound() {
        if (this.getState() == TheManState.CHASE) {
            return;
        }
        SoundEvent soundEvent = this.getAmbientSound();
        if (soundEvent != null) {
            this.playSound(soundEvent, this.getSoundVolume(), 1);
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.isTouchingWater()) {
            this.playSwimSound();
            this.playSecondaryStepSound(state);
        } else {
            BlockPos blockPos = this.getStepSoundPos(pos);
            if (!pos.equals(blockPos)) {
                BlockState blockState = this.getWorld().getBlockState(blockPos);
                if (blockState.isIn(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
                    this.playCombinationStepSounds(blockState, state);
                } else {
                    super.playStepSound(blockPos, blockState);
                }
            } else {
                super.playStepSound(pos, state);
            }
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MAN_PAIN;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MAN_DEATH;
    }

    public void playCritSound() {
        this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,this.getLoudSoundVolume(),1.0f);
    }

    /* Properties and Behavior */
    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Override
    public boolean canGather(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canUsePortals() {
        return false;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return false;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    @Override
    public boolean isFireImmune() {
        return false;
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return effect.getEffectType() != StatusEffects.INSTANT_DAMAGE &&
                effect.getEffectType() != StatusEffects.SLOWNESS &&
                effect.getEffectType() != StatusEffects.POISON &&
                effect.getEffectType() != StatusEffects.INVISIBILITY &&
                effect.getEffectType() != StatusEffects.WEAKNESS &&
                (getWorld().isDay() && effect.getEffectType() != StatusEffects.REGENERATION);
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return !(TheManUtils.manExists(this.getServerWorld()) || TheManUtils.hallucinationsExists(this.getServerWorld()));
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return this.canSpawn(world);
    }

    @Override
    public boolean disablesShield() {
        return true;
    }

    @Override
    public boolean isAiDisabled() {
        return super.isAiDisabled();
    }

    @Override
    protected void dropInventory() {
        if (this.getWorld().isDay() || this.isHallucination()) {
            return;
        }
        this.dropStack(new ItemStack(ModItems.TEAR_OF_THE_MAN,1));
        if (Math.random() < 0.45) {
            this.dropStack(new ItemStack(ModItems.CLAWS,1));
        } else {
            this.dropStack(new ItemStack(Items.WITHER_ROSE,this.random.nextBetween(1,6)));
        }
    }

    @Override
    public int getXpToDrop() {
        return 20;
    }

    @Override
    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        return 0;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.getWorld().isNight()) {
            Entity attacker = source.getAttacker();

            if (attacker instanceof LivingEntity livingEntity && !livingEntity.getMainHandStack().isOf(ModItems.CLAWS) && !this.isHallucination()) {

                if (attacker instanceof IronGolemEntity) {
                    attacker.damage(new DamageSource(source.getTypeRegistryEntry(),this),amount);
                    this.playCritSound();

                    return false;
                }

                if (Math.random() < MAN_BLOCK_CHANCE) {
                    attacker.damage(new DamageSource(source.getTypeRegistryEntry(),this),amount / 4f);
                    this.playCritSound();

                    this.aliveTicks -= 20;

                    return false;
                }
            }

            this.aliveTicks += 10;
        }

        return super.damage(source, amount);
    }

    public void addStatusEffects() {
        if (this.isHallucination()) {
            return;
        }
        this.addStatusEffect(TheManStatusEffects.REGENERATION);
    }

    public void despawn() {
        TheManUtils.doLightning(this.getServerWorld(),this);
        this.discard();
    }

    @Override
    protected Vec3d getAttackPos() {
        return this.getPos();
    }

    public void lunge(double x, double y, double z, double verticalForce) {
        if (this.isLunging()) {
            return;
        }
        this.playLungeSound();
        this.playLungeAttackSound();
        this.setVelocity((x - this.getX()) / 4,verticalForce + Math.abs((y - this.getY()) / 4),(z - this.getZ()) / 4);
        this.setLunging(true);
    }

    public void lunge(Vec3d position, double verticalForce) {
        this.lunge(position.getX(),position.getY(),position.getZ(),verticalForce);
    }

    public void lunge(Entity target, double verticalForce) {
        this.lunge(target.getX(),target.getY(),target.getZ(),verticalForce);
    }

    /**
     * Spawns hallucinations randomly around The Man
     */
    public void spawnHallucinations() {
        if (this.getState() != TheManState.CHASE || this.isHallucination()) {
            return;
        }

        ServerWorld serverWorld = this.getServerWorld();

        if (TheManUtils.hallucinationsExists(serverWorld)) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            int xOffset = (this.getRandom().nextBoolean() ? 1 : -1) * this.getRandom().nextBetween(2,11);
            int zOffset = (this.getRandom().nextBoolean() ? 1 : -1) * this.getRandom().nextBetween(2,11);

            TheManEntityHallucination hallucination = new TheManEntityHallucination(ModEntities.THE_MAN_HALLUCINATION,serverWorld);

            hallucination.setPosition(this.getPos().add(xOffset,0,zOffset));

            serverWorld.spawnEntity(hallucination);
        }
    }

    public static boolean isObstructed(World world, Vec3d origin, Vec3d target) {
        return world.raycast(new BlockStateRaycastContext(origin,target, BlockStatePredicate.ANY)).getType() != HitResult.Type.MISS;
    }

    public void chaseIfTooClose(double radius) {
        if (this.getTarget() != null && this.getTarget().isInRange(this,radius)) {
            this.startChase();
        }
    }

    public void chaseIfTooClose() {
        this.chaseIfTooClose(15);
    }

    public boolean shouldBreak(Block block) {
        return !Arrays.asList(MAN_BREAK_WHITELIST).contains(block);
    }

    private void breakBlocksInWay(ServerWorld serverWorld, LivingEntity target) {
        if (this.getTarget() == null || this.isMoving() || this.path == null || this.path.getLength() > 1 || !isObstructed(serverWorld,this.getPos().subtract(0,1,0),target.getPos().subtract(0,1,0))) {
            return;
        }

        Vec3d lookVector = Util.getRotationVector(0f,this.getYaw(1.0f));
        BlockPos lookBlockPos = BlockPos.ofFloored(this.getEyePos().add(lookVector));

        BlockState blockState = serverWorld.getBlockState(lookBlockPos);
        BlockState blockStateDown = serverWorld.getBlockState(lookBlockPos.down());

        Block block = blockState.getBlock();
        Block blockDown = blockStateDown.getBlock();

        if (!blockState.isAir() && block.getHardness() <= 2.0 && block.getHardness() >= 0.5) {
            if (this.shouldBreak(block)) {
                serverWorld.breakBlock(lookBlockPos,true);
            }

            if (!blockStateDown.isAir() && blockDown.getHardness() <= 2.0 && blockDown.getHardness() >= 0.5) {
                if (this.shouldBreak(blockDown)) {
                    serverWorld.breakBlock(lookBlockPos.down(),true);
                }
            }
        }
    }

    public void breakBlocksAround() {
        if (this.isDead() || this.climbing() || this.isHallucination() || !this.getServerWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return;
        }

        ServerWorld serverWorld = this.getServerWorld();
        LivingEntity target = this.getTarget();

        this.breakBlocksInWay(serverWorld,target);

        for (BlockPos blockPos : BlockPos.iterateOutwards(this.getBlockPos().up(), 1, 1, 1)) {
            BlockState blockState = serverWorld.getBlockState(blockPos);
            if (blockState.isAir() || blockState.isOf(Blocks.LAVA) || blockState.isOf(Blocks.WATER)) {
                continue;
            }

            Block block = blockState.getBlock();

            if (blockPos.getX() == this.getBlockX() && blockPos.getZ() == this.getBlockZ() && this.getBlockPos().getY() < blockPos.getY()) {
                if (block.getHardness() <= 2.0 && block.getHardness() >= 0.5) {
                    serverWorld.breakBlock(blockPos, true);
                }
            }

            if (block instanceof TrapdoorBlock && blockState.contains(TrapdoorBlock.OPEN)) {
                if (blockPos.getY() > this.getBlockY()) {
                    if (!blockState.get(TrapdoorBlock.OPEN)) {
                        serverWorld.setBlockState(blockPos,blockState.with(TrapdoorBlock.OPEN,true));
                    }
                } else {
                    if (blockState.get(TrapdoorBlock.OPEN)) {
                        serverWorld.setBlockState(blockPos,blockState.with(TrapdoorBlock.OPEN,false));
                    }
                }
                continue;
            }

            if (block instanceof DoorBlock) {
                serverWorld.breakBlock(blockPos, true);
                serverWorld.playSoundAtBlockCenter(
                        blockPos,
                        SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,
                        SoundCategory.BLOCKS,
                        this.getLoudSoundVolume(),
                        1.0f,
                        true
                );
                continue;
            }

            if (block instanceof TorchBlock && !blockState.emitsRedstonePower()) {
                serverWorld.breakBlock(blockPos, true);
                serverWorld.playSoundAtBlockCenter(
                        blockPos,
                        SoundEvents.BLOCK_WOOD_BREAK,
                        SoundCategory.BLOCKS,
                        this.getSoundVolume(),
                        1.0f,
                        true
                );
                continue;
            }

            if (block instanceof AbstractGlassBlock || block instanceof PaneBlock || blockState.getLuminance() >= 12) {
                serverWorld.breakBlock(blockPos, true);
                serverWorld.playSoundAtBlockCenter(
                        blockPos,
                        block.getSoundGroup(blockState).getBreakSound(),
                        SoundCategory.BLOCKS,
                        this.getSoundVolume(),
                        1.0f,
                        true
                );
            }
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        if (this.isHallucination()) {
            this.despawn();
            return false;
        }
        this.playAttackSound();
        this.playSlashSound();

        if (this.getState() == TheManState.STALK) {
            target.kill();
            this.startChase();
            return true;
        }

        return super.tryAttack(target);
    }

    public void attack(LivingEntity target) {
        if (this.isInAttackRange(target) && --this.attackCooldown <= 0L) {
            this.attackCooldown = Util.secToTick(this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED));
            this.swingHand(Hand.MAIN_HAND);
            this.tryAttack(target);
        }
    }

    @Nullable
    public Path findPath(double x, double y, double z) {
        return this.path = this.getNavigation().findPathTo(x, y, z, 0);
    }

    public Path findPath(Vec3d position) {
        return this.findPath(position.getX(),position.getY(),position.getZ());
    }

    public void moveTo(double x, double y, double z, double speed) {
        this.getNavigation().startMovingAlong(findPath(x, y, z),speed);
    }

    public void moveTo(Vec3d position, double speed) {
        this.moveTo(position.getX(),position.getY(),position.getZ(),speed);
    }

    public void moveTo(Entity target, double speed) {
        this.moveTo(target.getX(),target.getY(),target.getZ(),speed);
    }

    public boolean isMoving() {
        return this.getVelocity() != Vec3d.ZERO;
    }

    @Override
    public boolean isSilent() {
        return this.getState() == TheManState.STALK;
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        LivingEntity target = this.getServerWorld().getClosestPlayer(this.getX(),this.getY(),this.getZ(),MAN_MAX_SCAN_DISTANCE,TheManPredicates.TARGET_PREDICATE);
        this.setTarget(target);
        return target;
    }

    /* Ticking */
    public ServerWorld getServerWorld() {
        if (this.getWorld().isClient()) {
            throw new Error("Attempt to get a ServerWorld in a Client thread");
        }
        return (ServerWorld) this.getWorld();
    }

    public void serverTick(ServerWorld serverWorld) {
        if (this.isAiDisabled()) {
            return;
        }

        if (this.getTarget() != null && this.getTarget().isDead()) {
            this.despawn();
            return;
        }

        if (--this.aliveTicks <= 0L) {
            this.despawn();
            return;
        }

        if (this.isAlive() && this.isHallucination()) {
            this.setHealth(this.getHealth() - 4f);
        }

        if (serverWorld.isDay()) {
            this.despawn();
            return;
        }

        this.movementTick(serverWorld);

        this.getStateManager().tick(serverWorld);

        if (this.isLunging() && this.isOnGround()) {
            this.setLunging(false);
        }

        // If we are right above target, we move with the same velocity as the target and also down
        if (this.isLunging() && this.getTarget() != null) {
            Vec3d origin = new Vec3d(this.getX(),0,this.getZ());
            Vec3d target = new Vec3d(this.getTarget().getX(),0,this.getTarget().getZ());
            Vec3d targetVelocity = this.getTarget().getVelocity();

            if (target.isInRange(origin,10)) {
                this.setVelocity(targetVelocity.getX(),-2,targetVelocity.getZ());
                this.setLunging(false);
            }
        }

        this.targetTick();
    }

    @Override
    protected void mobTick() {
        if (!this.getWorld().isClient()) {
            this.serverTick((ServerWorld) this.getWorld());
        }
    }

    public void targetTick() {
        if (this.getTarget() == null) {
            return;
        }

        if (this.getTarget().isDead() && this.getState() == TheManState.CHASE) {
            this.despawn();
        }
    }

    @Nullable
    public static BlockPos getClosestBlockPosToTarget(ServerWorld serverWorld,Iterable<BlockPos> blockPosList,BlockPos target) {
        double closestDistance = 0;
        BlockPos closestBlockPos = null;

        for (BlockPos blockPos : blockPosList) {
            if (!serverWorld.getBlockState(blockPos).isAir()) {
                continue;
            }
            double distance = target.getSquaredDistance(blockPos);

            if (closestDistance == 0.0) {
                closestDistance = distance;
            }

            if (closestDistance > distance) {
                closestDistance = distance;
                closestBlockPos = blockPos;
            }
        }

        return closestBlockPos;
    }

    public static int getClimbableHeight(ServerWorld serverWorld,BlockPos blockPos) {
        if (serverWorld.getBlockState(blockPos).isAir()) {
            return 1;
        }

        int climbableHeight = 0;
        BlockPos currentPosition = blockPos.up();
        BlockState currentBlockState = serverWorld.getBlockState(currentPosition);

        while (!currentBlockState.isAir()) {
            climbableHeight++;
            if (climbableHeight > serverWorld.getTopY()) {
                break;
            }
            currentPosition = currentPosition.up();
        }

        return climbableHeight;
    }

    @Nullable
    public static BlockPos getFirstValidClimbableBlockPos(ServerWorld serverWorld,BlockPos climbBlockPos,int height) {
        for (BlockPos blockPos : BlockPos.iterateOutwards(climbBlockPos,1,0,1)) {
            BlockState blockState = serverWorld.getBlockState(blockPos);
            if (!blockState.isAir()) {
                continue;
            }

            BlockHitResult result = serverWorld.raycast(new BlockStateRaycastContext(blockPos.toCenterPos(), blockPos.up(height).toCenterPos(),BlockStatePredicate.ANY));

            if (result.getType() == HitResult.Type.MISS) {
                return blockPos;
            }
        }

        return null;
    }

    public void climbMovementTick(ServerWorld serverWorld) {
        boolean areBlocksAround = Util.areBlocksAround(serverWorld,this.getBlockPos().up(),1,0,1);

        this.setClimbing(areBlocksAround && this.getTarget() != null && this.getTarget().getBlockY() > this.getBlockY());

        if (!this.climbing() || this.getTarget() == null) {
            return;
        }

        this.setVelocity(0,MAN_CLIMB_SPEED,0);

        if (this.climbPosition != Vec3d.ZERO) {
            this.setPosition(this.climbPosition.getX(),this.climbPosition.getY(),this.climbPosition.getZ());
        }

        BlockPos blockPos = getClosestBlockPosToTarget(serverWorld,BlockPos.iterateOutwards(this.getBlockPos().up(),1,0,1),this.getTarget().getBlockPos());

        if (blockPos == null) {
            return;
        }

        int climbableHeight = getClimbableHeight(serverWorld,blockPos);

        BlockPos climbableBlockPos = getFirstValidClimbableBlockPos(serverWorld,blockPos,climbableHeight);

        if (climbableBlockPos == null) {
            return;
        }

        climbableBlockPos = climbableBlockPos.up();

        Vec3d climbablePos = climbableBlockPos.toCenterPos();

        this.teleport(climbablePos.getX(),this.getPos().getY(),climbablePos.getZ());
    }

    public void movementTick(ServerWorld serverWorld) {

        // If the target is higher than us, and it's more than 2 block tall, then we climb, otherwise, we don't do anything and let the
        // pathfinding deal with moving and jumping
        this.climbMovementTick(serverWorld);

        if (this.isSubmergedInWater()) {
            Vec3d oldVelocity = this.getVelocity();
            this.setVelocity(oldVelocity.getX(),0.5,oldVelocity.getZ());
        }

        /*if (this.climbing() && this.getTarget() != null) {

            if (this.isOnGround()) {
                this.jump();
            }

            Vec3d toPlayer = this.getTarget().getPos().subtract(this.getPos()).normalize().multiply(0.2);
            setVelocity(new Vec3d(toPlayer.getX(),1,toPlayer.getZ()).multiply(MAN_CLIMB_SPEED));
        }*/
    }

    /* Other */

    public void addEffectToClosePlayers(ServerWorld world, StatusEffectInstance statusEffectInstance) {
        if (this.isHallucination()) {
            return;
        }
        StatusEffectUtil.addEffectToPlayersWithinDistance(world,this,this.getPos(),MAN_CHASE_DISTANCE,statusEffectInstance,statusEffectInstance.getDuration() - 5);
    }
}

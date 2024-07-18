package com.zen.fogman.common.entity.the_man;

import com.zen.fogman.common.entity.ModEntities;
import com.zen.fogman.common.entity.the_man.states.*;
import com.zen.fogman.common.item.ModItems;
import com.zen.fogman.common.other.MathUtils;
import com.zen.fogman.common.sounds.ModSounds;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TheManEntity extends HostileEntity implements GeoEntity {
    public static final double MAN_SPEED = 0.54;
    public static final double MAN_CLIMB_SPEED = 0.8;
    public static final double MAN_MAX_SCAN_DISTANCE = 10000.0;
    public static final int MAN_CHASE_DISTANCE = 200;
    public static final double MAN_BLOCK_CHANCE = 0.1;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    /* Cooldowns */
    // Attack cooldown
    private long attackCooldown;
    // Move cooldown
    private long moveCooldown = 5;
    // Alive ticks
    private long aliveTicks;

    // State manager
    private final StateManager stateManager;

    // Sound instances
    private EntityTrackingSoundInstance chaseSoundInstance;

    public TheManEntity(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType,world);

        this.attackCooldown = MathUtils.secToTick(this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED));
        this.aliveTicks = MathUtils.secToTick(this.getRandom().nextBetween(30,120));
        this.stateManager = new StateManager(this);

        this.addStatusEffects();
        this.initSounds();
        this.initStates();
        this.initPathfindingPenalties();

        switch (this.getRandom().nextBetween(0,2)) {
            case 0:
                this.setState(TheManState.STARE);
                break;
            case 1:
                this.setState(TheManState.STALK);
                break;
        }
    }

    /* Initialization */
    public void initPathfindingPenalties() {
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

    public void initSounds() {
        this.chaseSoundInstance = new EntityTrackingSoundInstance(ModSounds.MAN_CHASE,this.getSoundCategory(),1.0f,1.0f,this,this.method_48926().getTime());
    }

    /* Attributes */
    public static DefaultAttributeContainer.Builder createManAttributes() {
        return TheManEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH,350)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,MAN_SPEED)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,5)
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
        this.getDataTracker().startTracking(TheManDataTrackers.TARGET_FOV,90f);
    }

    public void setClimbing(boolean climbing) {
        this.getDataTracker().set(TheManDataTrackers.CLIMBING, climbing);
    }

    @Override
    public boolean isClimbing() {
        return this.getDataTracker().get(TheManDataTrackers.CLIMBING);
    }

    public void setTargetFOV(float fov) {
        this.getDataTracker().set(TheManDataTrackers.TARGET_FOV,fov);
    }

    public float getTargetFOV() {
        return this.getDataTracker().get(TheManDataTrackers.TARGET_FOV);
    }

    /* NBT data */

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("manstate",this.getState().ordinal());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("manstate")) {
            this.setState(TheManState.values()[nbt.getInt("manstate")]);
        }
    }

    /* Animations */
    private PlayState predictate(AnimationState<TheManEntity> event) {
        if (this.isClimbing()) {
            return event.setAndContinue(TheManAnimations.CLIMB);
        }

        if (event.isMoving()) {
            return event.setAndContinue(TheManAnimations.RUN);
        }

        return event.setAndContinue(TheManAnimations.IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this,"controller", MathUtils.secToTick(0.1),this::predictate));
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
                BlockState blockState = this.method_48926().getBlockState(blockPos);
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

    /**
     * Plays the chase theme
     */
    public void playChaseSound(MinecraftClient client) {
        SoundManager soundManager = client.getSoundManager();

        if (client.player == null || this.getState() != TheManState.CHASE || this.isDead()) {
            soundManager.stop(this.chaseSoundInstance);
            return;
        }

        if (client.player.isInRange(this,MAN_CHASE_DISTANCE)) {
            if (!soundManager.isPlaying(this.chaseSoundInstance)) {
                soundManager.play(this.chaseSoundInstance);
            }
        } else {
            if (soundManager.isPlaying(this.chaseSoundInstance)) {
                soundManager.stop(this.chaseSoundInstance);
            }
        }
    }

    /**
     * Stops all sounds being played by The Man
     */
    public void stopSounds() {
        if (!this.isHallucination()) {
            MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_CHASE_ID,this.getSoundCategory());
        }
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_IDLECALM_ID,this.getSoundCategory());
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
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return effect.getEffectType() != StatusEffects.INSTANT_DAMAGE &&
                effect.getEffectType() != StatusEffects.SLOWNESS &&
                effect.getEffectType() != StatusEffects.POISON &&
                effect.getEffectType() != StatusEffects.INVISIBILITY &&
                effect.getEffectType() != StatusEffects.WEAKNESS &&
                (method_48926().isDay() && effect.getEffectType() != StatusEffects.REGENERATION);
    }

    @Override
    public boolean disablesShield() {
        return true;
    }

    @Override
    public boolean isAiDisabled() {
        return false;
    }

    @Override
    protected void dropInventory() {
        if (this.method_48926().isDay() || this.isHallucination()) {
            return;
        }
        this.dropStack(new ItemStack(ModItems.TEAR_OF_THE_MAN,1));
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
        Entity attacker = source.getAttacker();

        if (attacker != null && !this.isHallucination()) {

            if (attacker instanceof IronGolemEntity) {
                attacker.damage(new DamageSource(source.getTypeRegistryEntry(),this),amount);
                this.playCritSound();

                return false;
            }

            if (Math.random() < MAN_BLOCK_CHANCE) {
                attacker.damage(new DamageSource(source.getTypeRegistryEntry(),this),amount / 4f);
                this.playCritSound();

                return false;
            }
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
        this.stopSounds();
        TheManUtils.doLightning(this.getServerWorld(),this);
        this.discard();
    }

    @Override
    protected Vec3d getAttackPos() {
        return this.getPos();
    }

    public void lunge(double x, double y, double z, double verticalForce) {
        this.playLungeSound();
        this.setVelocity(x - this.getX(),verticalForce + Math.abs(y - this.getY()),z - this.getZ());
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

    public void breakBlocksAround() {
        if (this.isDead() || this.isHallucination()) {
            return;
        }

        ServerWorld serverWorld = this.getServerWorld();

        for (BlockPos blockPos : BlockPos.iterateOutwards(this.getBlockPos(), 1, 2, 1)) {
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
        return super.tryAttack(target);
    }

    public void attack(LivingEntity target) {
        if (this.isInAttackRange(target) && --this.attackCooldown <= 0L) {
            this.attackCooldown = MathUtils.secToTick(this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED));
            this.swingHand(Hand.MAIN_HAND);
            this.tryAttack(target);
        }
    }

    public void moveTo(double x, double y, double z, double speed) {
        if (--this.moveCooldown <= 0L) {
            this.getNavigation().startMovingTo(x,y,z,speed);
            this.moveCooldown = 5;
        }
    }

    public void moveTo(Vec3d position, double speed) {
        this.moveTo(position.getX(),position.getY(),position.getZ(),speed);
    }

    public void moveTo(Entity target, double speed) {
        this.moveTo(target.getX(),target.getY(),target.getZ(),speed);
    }

    /* Ticking */
    public ServerWorld getServerWorld() {
        if (this.method_48926().isClient()) {
            throw new Error("Attempt to get a ServerWorld in a Client thread");
        }
        return (ServerWorld) this.method_48926();
    }

    public void clientTick(MinecraftClient client) {
        if (this.isHallucination() || this.isDead()) {
            return;
        }

        targetFOVTick(client);
        playChaseSound(client);
    }

    public void serverTick(ServerWorld serverWorld) {
        if (--this.aliveTicks <= 0L) {
            this.despawn();
        }

        this.setTarget(serverWorld.getClosestPlayer(this.getX(),this.getY(),this.getZ(),MAN_MAX_SCAN_DISTANCE,TheManPredicates.TARGET_PREDICATE));

        if (serverWorld.isDay() && this.hasStatusEffect(StatusEffects.REGENERATION)) {
            this.removeStatusEffect(StatusEffects.REGENERATION);
        }

        this.setOnFire(serverWorld.isDay());

        this.getStateManager().tick(serverWorld);
        this.targetTick();
    }

    @Override
    protected void mobTick() {
        if (!this.method_48926().isClient()) {
            this.serverTick((ServerWorld) this.method_48926());
        }
    }

    public void targetTick() {
        if (this.getTarget() == null) {
            return;
        }

        if (this.getTarget() != null && this.getTarget().isInRange(this,15)) {
            this.startChase();
        }

        if (this.getTarget().isDead() && this.getState() == TheManState.CHASE) {
            this.despawn();
        }
    }

    public void targetFOVTick(MinecraftClient client) {
        if (this.isHallucination()) {
            return;
        }

        if (client.player == null) {
            return;
        }

        if (client.player.isTarget(this, TargetPredicate.DEFAULT)) {
            float fov = client.options.getFov().getValue() * client.player.getFovMultiplier();
            if (this.getTargetFOV() != fov) {
                this.setTargetFOV(fov);
            }
        }
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        this.setClimbing(this.horizontalCollision && this.getTarget() != null && this.getTarget().getBlockY() > this.getBlockY());

        if (this.isSubmergedInWater()) {
            Vec3d oldVelocity = this.getVelocity();
            this.setVelocity(oldVelocity.getX(),0.5,oldVelocity.getZ());
        }

        if (this.isClimbing() && this.getTarget() != null) {

            if (this.isOnGround()) {
                this.jump();
            }

            Vec3d toPlayer = this.getTarget().getPos().subtract(this.getPos()).normalize().multiply(0.2);
            setVelocity(new Vec3d(toPlayer.getX(),1,toPlayer.getZ()).multiply(MAN_CLIMB_SPEED));
        }
    }

    /* Other */
    /**
     * Checks if The Man is being looked at
     * @return  If The Man is being looked at or not
     */
    public boolean isLookedAt() {
        if (getTarget() == null) {
            return false;
        }
        if (getTarget() instanceof PlayerEntity player) {
            if (!method_48926().isClient()) {

                Vec3d lookVector = player.getRotationVec(1.0f).normalize();
                Vec3d direction = new Vec3d(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
                double e = lookVector.dotProduct(direction.normalize());

                return e > Math.cos(Math.toRadians(this.getTargetFOV())) &&
                        this.method_48926().raycast(
                                new BlockStateRaycastContext(
                                        new Vec3d(this.getX(), this.getEyeY(), this.getZ()),
                                        new Vec3d(player.getX(), player.getEyeY(), player.getZ()),
                                        TheManPredicates.BLOCK_STATE_PREDICATE
                                )
                        ).getType() == HitResult.Type.MISS;
            }
        }
        return false;
    }

    public void addEffectToClosePlayers(ServerWorld world, StatusEffectInstance statusEffectInstance) {
        if (this.isHallucination()) {
            return;
        }
        StatusEffectUtil.addEffectToPlayersWithinDistance(world,this,this.getPos(),MAN_CHASE_DISTANCE,statusEffectInstance,statusEffectInstance.getDuration() - 5);
    }
}

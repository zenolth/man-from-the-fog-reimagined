package com.zen.fogman.entity.custom;

import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.goals.custom.BreakDoorInstantGoal;
import com.zen.fogman.item.ModItems;
import com.zen.fogman.other.MathUtils;
import com.zen.fogman.sounds.ManSoundInstance;
import com.zen.fogman.sounds.ModSounds;
import com.zen.fogman.state.*;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Predicate;

/*
    TODO :
    - Add some more random events
    - Refactor some code
    - (Maybe) Optimize
    - Add compatibility with some mods (Lunar cycles etc.)
    - Make it creepier (yeah)
    - Make and add more animations
 */

public class TheManEntity extends HostileEntity implements GeoEntity {
    public static final double MAN_SPEED = 0.66;
    public static final double MAN_CLIMB_SPEED = 1.2;
    public static final double MAN_MAX_SCAN_DISTANCE = 100000.0;
    public static final long MAN_LOOK_TIME_TO_CHASE = 4;
    public static final int MAN_CHASE_DISTANCE = 200;

    public static final Predicate<BlockState> MAN_BLOCK_STATE_PREDICATE = blockState -> {

        if (blockState.isAir()) {
            return false;
        }

        if (blockState.getBlock() instanceof LeavesBlock) {
            return false;
        }

        return blockState.isOpaque();
    };

    private static final TrackedData<Boolean> MAN_CLIMBING_FLAG = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> MAN_STATE_FLAG = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.INTEGER);

    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation CLIMB_ANIM = RawAnimation.begin().thenLoop("climb");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private ManState state;
    private final HashMap<ManState, AbstractState> states = new HashMap<>();

    public final Random random2 = new Random();
    public long aliveTime;
    private long lastTime;

    private long lastHallucinationTime;

    private int targetFOV = 90;

    public ManSoundInstance chaseSoundInstance;
    private long lastBreakTime;

    public TheManEntity(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType, world);

        this.setState(ManState.STARE);

        this.initPathfindingPenalties();
        this.initStates();
        this.initTimes();
        this.initSounds();
    }

    public void initStates() {
        this.addState(ManState.CHASE,new ChaseState(this,this.getWorld()));
        this.addState(ManState.STARE,new StareState(this,this.getWorld()));
        this.addState(ManState.STALK,new StalkState(this,this.getWorld()));
        this.addState(ManState.FLEE,new FleeState(this,this.getWorld()));
    }

    public void initPathfindingPenalties() {
        this.setPathfindingPenalty(PathNodeType.UNPASSABLE_RAIL,0);
        this.setPathfindingPenalty(PathNodeType.FENCE,0);
        this.setPathfindingPenalty(PathNodeType.DOOR_WOOD_CLOSED,0);
        this.setPathfindingPenalty(PathNodeType.DOOR_IRON_CLOSED,0);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE,-1);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE,-1);
        this.setPathfindingPenalty(PathNodeType.LEAVES,0);
        this.setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW,-1);
        this.setPathfindingPenalty(PathNodeType.TRAPDOOR,-1);
    }

    public void initTimes() {
        this.lastTime = MathUtils.tickToSec(this.getWorld().getTime());
        this.lastHallucinationTime = MathUtils.tickToSec(this.getWorld().getTime());
        this.lastBreakTime = MathUtils.tickToSec(this.getWorld().getTime());
        this.aliveTime = this.random2.nextLong(30,60);
    }

    public void initSounds() {
        this.chaseSoundInstance = new ManSoundInstance(ModSounds.MAN_CHASE,this.getSoundCategory(),1.0f,1.0f,this,this.getWorld().getTime());
    }

    public void addState(ManState state,AbstractState object) {
        this.states.put(state,object);
    }

    /**
     * @param serverWorld The World to check
     * @return If any TheManEntityHallucination exist in serverWorld
     */
    public static boolean hallucinationsExist(ServerWorld serverWorld) {
        return !serverWorld.getEntitiesByType(ModEntities.THE_MAN_HALLUCINATION,EntityPredicates.VALID_LIVING_ENTITY).isEmpty();
    }

    /**
     * @param serverWorld The World to check
     * @return If any TheManEntity exist in serverWorld
     */
    public static boolean manExist(ServerWorld serverWorld) {
        return !serverWorld.getEntitiesByType(ModEntities.THE_MAN, EntityPredicates.VALID_LIVING_ENTITY).isEmpty();
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        ItemStack itemStack = new ItemStack(Items.LEATHER_BOOTS,1);
        itemStack.addEnchantment(Enchantments.DEPTH_STRIDER,3);

        return Collections.singleton(itemStack);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.MASTER;
    }

    public void setState(ManState state) {
        this.state = state;
        this.dataTracker.set(MAN_STATE_FLAG,this.state.ordinal());
    }

    public ManState getState() {
        return ManState.values()[this.dataTracker.get(MAN_STATE_FLAG)];
    }

    public boolean isChasing() {
        return this.getState() == ManState.CHASE;
    }

    public void startChase(ServerWorld serverWorld) {
        this.setState(ManState.CHASE);
        this.playAlarmSound();
        TheManEntity.doLightning(serverWorld,this.getPos());
    }

    public void flee() {
        this.setState(ManState.FLEE);
    }

    public void addEffectsToClosePlayers(ServerWorld world, Vec3d pos, @Nullable Entity entity, int range) {
        StatusEffectInstance darknessInstance = new StatusEffectInstance(StatusEffects.DARKNESS, 260, 2, false, false);
        StatusEffectUtil.addEffectToPlayersWithinDistance(world, entity, pos, range, darknessInstance, 200);
        StatusEffectInstance speedInstance = new StatusEffectInstance(StatusEffects.SPEED, 460, 1, false, false);
        StatusEffectUtil.addEffectToPlayersWithinDistance(world, entity, pos, range, speedInstance, 400);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new BreakDoorInstantGoal(this));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(MAN_CLIMBING_FLAG,false);
        this.dataTracker.startTracking(MAN_STATE_FLAG,0);
    }

    public static DefaultAttributeContainer.Builder createManAttributes() {
        return TheManEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH,350)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,MAN_SPEED)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,5)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK,4)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,1.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE,MAN_MAX_SCAN_DISTANCE)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,100)
                .add(EntityAttributes.GENERIC_ARMOR,7)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,5);
    }

    /**
     * Spawns a lightning at The Man's position
     */
    public static void doLightning(ServerWorld serverWorld, Vec3d position) {
        LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, serverWorld);
        lightningEntity.setCosmetic(true);
        lightningEntity.setInvulnerable(true);
        lightningEntity.setOnFire(false);
        lightningEntity.setPosition(position);
        serverWorld.spawnEntity(lightningEntity);
    }

    @Override
    public boolean canSpawn(WorldView world) {
        if (this.getWorld().isDay()) {
            return false;
        }
        if (!this.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            return TheManEntity.manExist(serverWorld);
        }
        return super.canSpawn(world);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        SpiderNavigation nav = new SpiderNavigation(this,world);
        nav.setCanWalkOverFences(true);
        nav.setCanPathThroughDoors(true);
        nav.setCanSwim(true);
        nav.setSpeed(MAN_SPEED);
        return nav;
    }

    public boolean isHallucination() {
        return false;
    }

    // Animations
    private PlayState predictate(AnimationState event) {
        if (this.isClimbing()) {
            return event.setAndContinue(CLIMB_ANIM);
        }

        if (event.isMoving()) {
            return event.setAndContinue(RUN_ANIM);
        }

        return event.setAndContinue(IDLE_ANIM);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this,"controller",0,this::predictate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    // Properties (or "can" stuff)
    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    @Override
    public boolean canUsePortals() {
        return false;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Override
    public boolean canMoveVoluntarily() {
        return !getWorld().isClient();
    }

    @Override
    public boolean disablesShield() {
        return true;
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
    protected void dropInventory() {
        if (getWorld().isDay()) {
            return;
        }
        this.dropStack(new ItemStack(ModItems.TEAR_OF_THE_MAN,1));
    }

    @Override
    public int getXpToDrop() {
        return 20;
    }

    @Override
    public boolean isGlowing() {
        return this.isChasing() && this.getTarget() != null && MathUtils.distanceTo(this,this.getTarget()) <= MAN_CHASE_DISTANCE / 2.0;
    }

    @Override
    protected void dropXp() {
        if (this.getWorld().isDay()) {
            return;
        }
        super.dropXp();
    }

    /**
     * Despawns The Man, spawns a lightning and stops all sounds
     */
    public void begone(ServerWorld serverWorld) {
        TheManEntity.doLightning(serverWorld,this.getPos());
        this.stopSounds();
        this.discard();
    }

    // Other mechanics
    @Override
    public boolean isClimbing() {
        return this.dataTracker.get(MAN_CLIMBING_FLAG);
    }

    public void doLunge(LivingEntity target) {
        this.playLungeSound();
        Vec3d toTarget = target.getPos().subtract(this.getPos()).add(0,1,0).multiply(0.3,0.2,0.3);
        this.setVelocity(toTarget);
    }

    /**
     * Spawns hallucinations randomly around The Man
     */
    public void spawnHallucinations() {
        if (!this.isChasing()) {
            return;
        }

        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        if (TheManEntity.hallucinationsExist(serverWorld)) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            int xOffset = (random2.nextBoolean() ? 1 : -1) * random2.nextInt(2,10);
            int zOffset = (random2.nextBoolean() ? 1 : -1) * random2.nextInt(2,10);

            TheManEntityHallucination hallucination = new TheManEntityHallucination(ModEntities.THE_MAN_HALLUCINATION,serverWorld);

            hallucination.setPosition(this.getPos().add(xOffset,0,zOffset));

            serverWorld.spawnEntity(hallucination);
        }
    }

    public void breakBlocksAround(ServerWorld serverWorld) {
        if (this.isDead()) {
            return;
        }

        if (MathUtils.tickToSec(serverWorld.getTime()) - this.lastBreakTime > 0.5) {
            for (BlockPos blockPos : BlockPos.iterateOutwards(this.getBlockPos(), 0, 2, 0)) {
                BlockState blockState = serverWorld.getBlockState(blockPos);
                if (blockState.isAir() || this.getBlockPos().getY() >= blockPos.getY()) {
                    continue;
                }

                if (blockState.getBlock().getHardness() <= 2.0 && blockState.getBlock().getHardness() >= 1.5) {
                    serverWorld.breakBlock(blockPos, true);
                }
            }

            for (BlockPos blockPos : BlockPos.iterateOutwards(this.getBlockPos(), 1, 1, 1)) {
                BlockState blockState = serverWorld.getBlockState(blockPos);
                if (blockState.isAir()) {
                    continue;
                }

                if (blockState.getBlock() instanceof AbstractGlassBlock || blockState.getBlock() instanceof PaneBlock) {
                    serverWorld.breakBlock(blockPos, true);
                    serverWorld.playSoundAtBlockCenter(blockPos, SoundEvents.BLOCK_GLASS_BREAK,SoundCategory.BLOCKS,this.getSoundVolume(),this.getSoundPitch(),true);
                }
            }

            this.lastBreakTime = MathUtils.tickToSec(serverWorld.getTime());
        }
    }

    public void closeTrapdoors(ServerWorld serverWorld) {
        if (this.isDead()) {
            return;
        }

        for (BlockPos blockPos : BlockPos.iterateOutwards(this.getBlockPos(), 2, 2, 2)) {
            BlockState blockState = serverWorld.getBlockState(blockPos);
            if (blockState.isAir() || blockPos.getY() > this.getBlockY()) {
                continue;
            }

            if (blockState.contains(TrapdoorBlock.OPEN) && blockState.get(TrapdoorBlock.OPEN)) {
                serverWorld.setBlockState(blockPos,blockState.with(TrapdoorBlock.OPEN,false));
            }
        }
    }

    public void updateTargetFov(MinecraftClient client) {
        if (client.player.isTarget(this, TargetPredicate.DEFAULT)) {
            if (this.targetFOV != client.options.getFov().getValue()) {
                this.targetFOV = MinecraftClient.getInstance().options.getFov().getValue();
            }
        }
    }

    public void chaseIfTooClose(ServerWorld serverWorld) {
        if (this.getTarget() != null && this.getTarget().isPlayer() && MathUtils.distanceTo(this,this.getTarget()) <= 15) {
            this.startChase(serverWorld);
        }
    }

    // Ticks
    public void stateTick(ServerWorld serverWorld) {
        if (this.states.containsKey(this.state)) {
            this.states.get(this.state).tick(serverWorld);
        }
    }

    public void moveTick() {
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

    public void dataTrackerTick() {
        this.dataTracker.set(
                MAN_CLIMBING_FLAG,
                this.horizontalCollision && this.getTarget() != null && this.getTarget().getBlockY() > this.getBlockY()
        );
        this.dataTracker.set(MAN_STATE_FLAG,this.state.ordinal());
    }

    public void hallucinationTick() {
        if (MathUtils.tickToSec(this.getWorld().getTime()) - this.lastHallucinationTime > 5) {
            if (this.random2.nextFloat(0f,1f) < 0.2) {
                this.spawnHallucinations();
            }
            this.lastHallucinationTime = MathUtils.tickToSec(this.getWorld().getTime());
        }
    }

    public void healthTick(ServerWorld serverWorld) {
        if (this.isAlive() && ((aliveTime > 0 && MathUtils.tickToSec(this.getWorld().getTime()) - lastTime > aliveTime) || (this.getTarget() != null && this.getTarget().isDead()))) {
            this.begone(serverWorld);
        }
        if (this.getHealth() < this.getMaxHealth() && this.isAlive() && this.getWorld().isNight()) {
            this.setHealth(this.getHealth() + 0.1f);
        }
        if (this.getWorld().isDay()) {
            if (this.isAttacking()) {
                this.setAttacking(false);
            }
            if (!this.isOnFire()) {
                this.setHealth(5);
            }
            this.setOnFireFor(60);
        }
    }

    public void clientTick(MinecraftClient client) {
        this.updateTargetFov(client);
        this.playChaseSound(client);
    }

    public void serverTick() {
        if (this.isDead()) {
            this.stopSounds();
            return;
        }

        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        this.setTarget(this.getWorld().getClosestPlayer(this.getX(),this.getY(),this.getZ(),MAN_MAX_SCAN_DISTANCE,true));

        this.random2.setSeed(serverWorld.getTime());

        this.stateTick(serverWorld);
        this.moveTick();
        this.dataTrackerTick();
        this.hallucinationTick();
        this.healthTick(serverWorld);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient) {
            this.chaseSoundInstance.tick();
            this.serverTick();
        }
    }

    // Sounds
    public void playAlarmSound() {
        this.playSound(ModSounds.MAN_ALARM,10.0f,this.getSoundPitch());
    }

    public void playLungeSound() {
        this.playSound(ModSounds.MAN_LUNGE,3.0f,this.getSoundPitch());
    }

    @Override
    public void playAmbientSound() {
        SoundEvent soundEvent = this.getAmbientSound();
        float soundVolume;
        if (this.getTarget() == null) {
            soundVolume = this.getSoundVolume();
        } else {
            soundVolume = 10;
        }
        if (soundEvent != null) {
            this.playSound(soundEvent, soundVolume, 1);
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getTarget() != null) {
            return null;
        }
        return ModSounds.MAN_IDLECALM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MAN_PAIN;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MAN_DEATH;
    }

    /**
     * Plays the chase theme
     */
    public void playChaseSound(MinecraftClient client) {
        if (this.isDead()) {
            client.getSoundManager().stop(this.chaseSoundInstance);
            return;
        }

        if (client.player == null) {
            client.getSoundManager().stop(this.chaseSoundInstance);
            return;
        }

        if (!this.isChasing()) {
            client.getSoundManager().stop(this.chaseSoundInstance);
            return;
        }

        if (MathUtils.distanceTo(this,client.player) <= MAN_CHASE_DISTANCE) {
            this.chaseSoundInstance.setVolumeModifier(1.0f);
            if (!client.getSoundManager().isPlaying(this.chaseSoundInstance)) {
                client.getSoundManager().play(this.chaseSoundInstance);
            }
        } else {
            if (this.chaseSoundInstance.getVolumeModifier() > 0) {
                this.chaseSoundInstance.setVolumeModifier(this.chaseSoundInstance.getVolumeModifier() - 0.1f);
            }
            if (this.chaseSoundInstance.getVolumeModifier() <= 0) {
                client.getSoundManager().stop(this.chaseSoundInstance);
            }
        }
    }

    /**
     * Stops all sounds being played by The Man
     */
    public void stopSounds() {
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_CHASE_ID,this.getSoundCategory());
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_IDLECALM_ID,this.getSoundCategory());
    }

    // Attacking and damaging

    public double getSquaredMaxAttackDistance(LivingEntity entity) {
        return this.getWidth() * 2.0f * (this.getWidth() * 2.0f) + entity.getWidth();
    }

    public void attackTarget(LivingEntity target) {
        float damage = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float knockback = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);

        this.playSound(ModSounds.MAN_ATTACK,this.getSoundVolume(),this.getSoundPitch());

        if (knockback > 0.0f) {
            target.takeKnockback(knockback * 0.5f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
            this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
        }

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS,50));

        target.damage(this.getDamageSources().mobAttack(this), damage);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof IronGolemEntity) {
            return false;
        }
        return super.damage(source, amount);
    }

    // Movement and jumping
    @Override
    protected float getJumpVelocity() {
        return 0.72f * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    @Override
    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        return 0;
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {
        super.slowMovement(state,new Vec3d(1,1,1));
    }

    /**
     * Checks if The Man is being looked at
     * @return  If The Man is being looked at or not
     */
    public boolean isLookedAt() {
        if (getTarget() == null) {
            return false;
        }
        if (getTarget() instanceof PlayerEntity player) {
            if (!getWorld().isClient()) {
                
                Vec3d lookVector = player.getRotationVec(1.0f).normalize();
                Vec3d direction = new Vec3d(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
                double e = lookVector.dotProduct(direction.normalize());

                return e > Math.cos(Math.toRadians(this.targetFOV)) &&
                        this.getWorld().raycast(
                                new BlockStateRaycastContext(
                                        new Vec3d(this.getX(), this.getEyeY(), this.getZ()),
                                        new Vec3d(player.getX(), player.getEyeY(), player.getZ()),
                                        MAN_BLOCK_STATE_PREDICATE
                                )
                        ).getType() == HitResult.Type.MISS;
            }
        }
        return false;
    }
}

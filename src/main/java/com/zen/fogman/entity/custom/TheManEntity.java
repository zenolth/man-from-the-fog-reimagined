package com.zen.fogman.entity.custom;

import com.zen.fogman.ManFromTheFog;
import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.goals.custom.BreakDoorInstantGoal;
import com.zen.fogman.goals.custom.ManChasePlayerGoal;
import com.zen.fogman.item.ModItems;
import com.zen.fogman.sounds.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
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
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class TheManEntity extends HostileEntity implements GeoEntity {

    private static final TrackedData<Byte> MAN_FLAGS = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final float field_30498 = 0.5f;

    public static final double MAN_SPEED = 0.55;

    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private boolean didTarget = false;

    public final Random timeRandom = new Random();
    private long aliveTime;
    private long lastTime;

    public TheManEntity(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType, world);
    }

    public static void addDarknessToClosePlayers(ServerWorld world, Vec3d pos, @Nullable Entity entity, int range) {
        StatusEffectInstance statusEffectInstance = new StatusEffectInstance(StatusEffects.DARKNESS, 260, 0, false, false);
        StatusEffectUtil.addEffectToPlayersWithinDistance(world, entity, pos, range, statusEffectInstance, 200);
    }

    @Override
    protected void initGoals() {
        // Goals
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));

        this.goalSelector.add(2, new BreakDoorInstantGoal(this));
        this.goalSelector.add(2, new WanderNearTargetGoal(this, 0.7, 200.0f));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0));

        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(3, new GoToVillageGoal(this,1000));

        this.goalSelector.add(4, new LookAroundGoal(this));

        // Targets
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, false));

        this.targetSelector.add(2, new ActiveTargetGoal<>(this, GolemEntity.class, false));

        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, PatrolEntity.class, false));

        this.targetSelector.add(7, new ActiveTargetGoal<>(this, AnimalEntity.class, false));
        //this.targetSelector.add(7, new ActiveTargetGoal<>(this, ZombieEntity.class, false));
        //this.targetSelector.add(7, new ActiveTargetGoal<>(this, AbstractSkeletonEntity.class, false));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(MAN_FLAGS, (byte)0);
    }

    public static DefaultAttributeContainer.Builder createManAttributes() {
        return TheManEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH,350)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,MAN_SPEED)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,5)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK,2)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,0.9)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE,75)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,100)
                .add(EntityAttributes.GENERIC_ARMOR,7)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,5);
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public void setAliveTime(long aliveTime) {
        this.aliveTime = aliveTime;
    }

    public void doLightning() {
        if (!getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld) getWorld();
            LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, serverWorld);
            lightningEntity.setCosmetic(true);
            lightningEntity.setInvulnerable(true);
            lightningEntity.setOnFire(false);
            lightningEntity.setPosition(this.getPos());
            serverWorld.spawnEntity(lightningEntity);
        }
    }

    @Override
    public boolean canSpawn(WorldView world) {
        if (getWorld().isDay()) {
            return false;
        }
        if (!getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld)getWorld();
            List<?> entities = serverWorld.getEntitiesByType(this.getType(), EntityPredicates.VALID_ENTITY);
            if (entities.size() > 1) {
                return false;
            }
        }
        return super.canSpawn(world);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        SpiderNavigation nav = new SpiderNavigation(this,world);
        nav.setCanWalkOverFences(true);
        nav.setCanPathThroughDoors(true);
        nav.setCanSwim(true);
        nav.setSpeed(MAN_SPEED * 2);
        return nav;
    }

    protected <E extends TheManEntity> PlayState idleAnimController(final AnimationState<E> event) {
        if (!event.isMoving()) {
            return event.setAndContinue(IDLE_ANIM);
        }
        return PlayState.STOP;
    }

    protected <E extends TheManEntity> PlayState runAnimController(final AnimationState<E> event) {
        if (event.isMoving()) {
            return event.setAndContinue(RUN_ANIM);
        }
        return PlayState.STOP;
    }

    public boolean isClimbingWall() {
        return (this.dataTracker.get(MAN_FLAGS) & 1) != 0;
    }

    @Override
    public boolean isClimbing() {
        return isClimbingWall();
    }

    public void setClimbingWall(boolean climbing) {
        byte b = this.dataTracker.get(MAN_FLAGS);
        b = climbing ? (byte)(b | 1) : (byte)(b & 0xFFFFFFFE);
        this.dataTracker.set(MAN_FLAGS, b);
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
    public boolean canUsePortals() {
        return false;
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this,"Standing",5,this::idleAnimController));
        controllers.add(new AnimationController<>(this,"Running",5,this::runAnimController));
    }

    public void playSpottedSound() {
        this.playSound(ModSounds.MAN_SPOT,this.getSoundVolume(),this.getSoundPitch());
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {

    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (getTarget() != null) {
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

    @Override
    public void playAmbientSound() {
        SoundEvent soundEvent = this.getAmbientSound();
        float soundVolume;
        if (getTarget() == null) {
            soundVolume = this.getSoundVolume();
        } else {
            soundVolume = 10;
        }
        if (soundEvent != null) {
            this.playSound(soundEvent, soundVolume, 1);
        }
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
    protected void dropXp() {
        if (getWorld().isDay()) {
            return;
        }
        super.dropXp();
    }

    public void begone() {
        doLightning();
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient()) {
            this.setClimbingWall(this.horizontalCollision && getTarget() != null && getTarget().getBlockY() > getBlockY());

            if (isAlive() && ((aliveTime > 0 && getWorld().getTime() - lastTime > aliveTime) || (getTarget() != null && getTarget().isDead())) && getRandom().nextBetween(0,7) == 6) {
                begone();
            }
            if (getHealth() < getMaxHealth() && isAlive() && getWorld().isNight()) {
                setHealth(getHealth() + 0.1f);
            }
            if (getWorld().isDay()) {
                if (this.isAttacking()) {
                    this.setAttacking(false);
                }
                if (!this.isOnFire()) {
                    this.setHealth(5);
                }
                this.setOnFireFor(60);
            }

            if (getTarget() != null && !didTarget) {
                playSpottedSound();
                didTarget = true;
            } else if (getTarget() == null && didTarget) {
                didTarget = false;
            }

        }
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (getWorld().isClient()) {
            return;
        }
        addDarknessToClosePlayers((ServerWorld) getWorld(),this.getPos(),this,40);
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
    public boolean tryAttack(Entity target) {
        this.playSound(ModSounds.MAN_ATTACK,this.getSoundVolume(),this.getSoundPitch());
        return super.tryAttack(target);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof IronGolemEntity) {
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    protected float getJumpVelocity() {
        return 0.72f * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    @Override
    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        return 0;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}

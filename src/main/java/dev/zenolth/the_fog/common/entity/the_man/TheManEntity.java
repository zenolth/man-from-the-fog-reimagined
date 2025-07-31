package dev.zenolth.the_fog.common.entity.the_man;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.animation.TheManAnimations;
import dev.zenolth.the_fog.common.components.WorldComponent;
import dev.zenolth.the_fog.common.damage_type.ModDamageTypes;
import dev.zenolth.the_fog.common.data_tracker.TrackingData;
import dev.zenolth.the_fog.common.entity.ModEntities;
import dev.zenolth.the_fog.common.entity.MonitorPlayerLineOfSight;
import dev.zenolth.the_fog.common.entity.OnSpawnEntity;
import dev.zenolth.the_fog.common.pathfinding.ModPathNodeTypes;
import dev.zenolth.the_fog.common.pathfinding.astar.PathfindingAgent;
import dev.zenolth.the_fog.common.predicate.TheManPredicates;
import dev.zenolth.the_fog.common.state_machine.StateMachineEntity;
import dev.zenolth.the_fog.common.state_machine.states.TheManState;
import dev.zenolth.the_fog.common.state_machine.states.the_man.*;
import dev.zenolth.the_fog.common.item.ModItems;
import dev.zenolth.the_fog.common.status_effect.TheManStatusEffects;
import dev.zenolth.the_fog.common.util.*;
import dev.zenolth.the_fog.common.pathfinding.TheManNavigation;
import dev.zenolth.the_fog.common.sounds.ModSounds;
import dev.zenolth.the_fog.common.state_machine.StateMachine;
import dev.zenolth.the_fog.common.util.Timer;
import dev.zenolth.the_fog.common.world.dimension.ModDimensions;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
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

import java.util.*;
import java.util.function.Predicate;

public class TheManEntity extends HostileEntity implements GeoEntity, StateMachineEntity<TheManState>, OnSpawnEntity, MonitorPlayerLineOfSight {
    public static final EntityDimensions HITBOX_SIZE = EntityDimensions.fixed(0.6f, 2.3f);
    public static final EntityDimensions CROUCH_HITBOX_SIZE = EntityDimensions.fixed(0.6f, 1.3f);
    public static final EntityDimensions CRAWL_HITBOX_SIZE = EntityDimensions.fixed(0.6f, 0.8f);
    public static final EntityDimensions CLIMB_HITBOX_SIZE = EntityDimensions.fixed(0.3f, 0.4f);
    public static final EntityDimensions LUNGE_HITBOX_SIZE = EntityDimensions.fixed(0.5f,0.4f);
    public static final float ATTACK_HITBOX_SIZE = 1f;

    public static final int MIN_PATH_COMPUTE_TICKS = 1;
    public static final int MAX_PATH_COMPUTE_TICKS = 5;

    public static final int TOO_FAR_AWAY_TICKS = 2400;
    public static final int LUNGE_COOLDOWN = 600;

    public static final double SPEED = 0.52;
    public static final double SPEED_NO_STATUS_EFFECT = 0.43;
    public static final double CLIMB_SPEED = 0.7;
    public static final double SHIELD_DISABLE_CHANCE = 0.75;
    public static final double BLOCK_CHANCE = 0.25;
    public static final int CHASE_DISTANCE = 200;

    public static final int STATUS_EFFECT_DURATION = 20;

    public static List<RegistryKey<DamageType>> ALLOWED_DAMAGE_TYPES = Arrays.asList(
            DamageTypes.PLAYER_ATTACK,
            DamageTypes.MOB_ATTACK,
            DamageTypes.MOB_ATTACK_NO_AGGRO,
            DamageTypes.ARROW,
            DamageTypes.GENERIC_KILL
    );

    /* NBT data names */
    public static final String STATE_NBT_KEY = "ManState";
    public static final String ALIVE_TICKS_NBT_KEY = "ManAliveTicks";
    public static final String SHIELD_HEALTH_NBT_KEY = "ManShieldHealth";

    // Animation cache stuff
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Nullable private Path currentPath;
    private final PathfindingAgent<TheManEntity> pathfindingAgent = new PathfindingAgent<>(this);

    /* Tracked Data */
    public static final TrackedData<Float> SHIELD_HEALTH = TrackingData.register(TheManEntity.class,TrackedDataHandlerRegistry.FLOAT);

    public static final TrackedData<Boolean> CLIMBING = TrackingData.register(TheManEntity.class,TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> CROUCHING = TrackingData.register(TheManEntity.class,TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> CRAWLING = TrackingData.register(TheManEntity.class,TrackedDataHandlerRegistry.BOOLEAN);

    public static final TrackedData<Boolean> SPITTING = TrackingData.register(TheManEntity.class,TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> LUNGING = TrackingData.register(TheManEntity.class,TrackedDataHandlerRegistry.BOOLEAN);

    public static final TrackedData<Integer> STATE = TrackingData.register(TheManEntity.class,TrackedDataHandlerRegistry.INTEGER);

    /* Tracking Data */
    public final TrackingData<TheManEntity,Float> shieldHealth;

    public final TrackingData<TheManEntity,Boolean> climbing;
    public final TrackingData<TheManEntity,Boolean> crouching;
    public final TrackingData<TheManEntity,Boolean> crawling;

    public final TrackingData<TheManEntity,Boolean> spitting;
    public final TrackingData<TheManEntity,Boolean> lunging;

    public final TrackingData<TheManEntity,Integer> state;

    /* Cooldowns */
    // Attack cooldown
    private final Timer attackTimer;
    // Alive ticks
    private final Timer aliveTimer;
    // Hitbox ticks
    private final Timer hitboxUpdateTimer;
    // Target ticks
    private final Timer targetDetectTimer;
    // Far away ticks
    private final Timer farAwayTimer;
    // Forget ticks
    private final Timer forgetTimer;
    // Path compute ticks
    private long pathComputeTicks;

    private boolean shouldComputePath = true;

    // State manager
    private final StateMachine<TheManEntity,TheManState> stateMachine;

    // Chances
    private double blockChance = BLOCK_CHANCE;

    private final HashSet<UUID> playersWithLineOfSight = new HashSet<>();
    private final HashSet<UUID> targetablePlayers = new HashSet<>();
    @Nullable private Vec3d lastKnownTargetPosition;

    // Hitbox size
    private EntityDimensions currentHitboxSize = HITBOX_SIZE;

    public TheManEntity(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType,world);

        this.shieldHealth = new TrackingData<>(this,SHIELD_HEALTH,50f);
        this.pathComputeTicks = RandomNum.next(MIN_PATH_COMPUTE_TICKS,MAX_PATH_COMPUTE_TICKS);

        this.climbing = new TrackingData<>(this,CLIMBING,false);
        this.crouching = new TrackingData<>(this,CROUCHING,false);
        this.crawling = new TrackingData<>(this,CRAWLING,false);

        this.spitting = new TrackingData<>(this,SPITTING,false);
        this.lunging = new TrackingData<>(this,LUNGING,false);

        this.state = new TrackingData<>(this,STATE,TheManState.STARE.ordinal());

        this.attackTimer = new Timer(TimeHelper.secToTick(this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED)),true,this::attemptAttack);
        this.aliveTimer = new Timer(TimeHelper.secToTick(RandomNum.next(30,120)),this::despawn);
        this.hitboxUpdateTimer = new Timer(10,true,this::updateHitbox);
        this.targetDetectTimer = new Timer(5,true,this::updateTarget);
        this.farAwayTimer = new Timer(TOO_FAR_AWAY_TICKS,this::teleportBehindTarget);
        this.forgetTimer = new Timer(() -> TimeHelper.secToTick(FogMod.CONFIG.behavior.forgetTime * 60L),true,this::forget);

        this.attackTimer.start();
        this.aliveTimer.start();
        this.hitboxUpdateTimer.start();
        this.targetDetectTimer.start();
        this.farAwayTimer.start();

        this.aliveTimer.pause();
        this.farAwayTimer.pause();

        this.stateMachine = new StateMachine<>(this);

        this.setStepHeight(1.0f);

        this.addStatusEffects();
        this.initStates();
        this.initPathfindingPenalties();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
    }

    /* Initialization */

    public void onSpawn(ServerWorld world) {
        this.setNoGravity(false);
        this.noClip = !this.isReal();

        if (this.isReal()) {
            WorldComponent.get(world).setTheManId(this.getId());

            var nbtCompound = this.writeNbt(new NbtCompound());

            if (nbtCompound.contains(STATE_NBT_KEY)) {
                this.setState(TheManState.values()[nbtCompound.getInt(STATE_NBT_KEY)]);
            } else {
                if (this.getTarget() != null) {
                    BlockHitResult hitResult = world.raycast(new BlockStateRaycastContext(this.getEyePos(), this.getTarget().getEyePos(), TheManPredicates.BLOCK_STATE_PREDICATE));
                    if (hitResult.getType() == HitResult.Type.MISS) {
                        this.setState(TheManState.STARE);
                    } else {
                        this.setState(TheManState.STALK);
                    }
                } else {
                    switch (RandomNum.next(0, 2)) {
                        case 0:
                            this.setState(TheManState.STARE);
                            break;
                        case 1:
                            this.setState(TheManState.STALK);
                            break;
                    }
                }
            }
        } else {
            this.startChase();
        }

    }

    public void initPathfindingPenalties() {
        this.setPathfindingPenalty(PathNodeType.LEAVES,0);
        this.setPathfindingPenalty(PathNodeType.UNPASSABLE_RAIL,0);
        this.setPathfindingPenalty(PathNodeType.DOOR_OPEN,0);
        this.setPathfindingPenalty(PathNodeType.DOOR_IRON_CLOSED,0);
        this.setPathfindingPenalty(PathNodeType.DOOR_WOOD_CLOSED,0);
        this.setPathfindingPenalty(PathNodeType.DANGER_OTHER,-1f);
    }

    public void initStates() {
        this.stateMachine.add(TheManState.CHASE,new ChaseState(this));
        this.stateMachine.add(TheManState.STARE,new StareState(this));
        this.stateMachine.add(TheManState.FLEE,new FleeState(this));
        this.stateMachine.add(TheManState.STALK,new StalkState(this));
        this.stateMachine.add(TheManState.PREPARE_LUNGE,new PrepareLungeState(this));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        var mobNavigation = new TheManNavigation(this,world);

        mobNavigation.setCanSwim(true);
        mobNavigation.setCanEnterOpenDoors(true);
        mobNavigation.setCanPathThroughDoors(true);

        return mobNavigation;
    }

    /* Attributes */
    public static DefaultAttributeContainer.Builder createAttributes() {
        return TheManEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH,400)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, FogMod.CONFIG.statusEffects.giveSpeed ? SPEED : SPEED_NO_STATUS_EFFECT)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,5.5)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK,3.5)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,0.4)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, CHASE_DISTANCE * 2)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,100)
                .add(EntityAttributes.GENERIC_ARMOR,7)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,5);
    }

    /* States */
    public StateMachine<TheManEntity,TheManState> getStateMachine() {
        return this.stateMachine;
    }

    public void setState(TheManState state) {
        this.state.set(state.ordinal());
        this.stateMachine.start();
    }

    public TheManState getState() {
        return TheManState.values()[this.state.get()];
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
    public boolean isClimbing() {
        return this.climbing.get();
    }

    public void damageShield(float damage) {
        if (!this.hasShield()) {
            return;
        }

        if (this.shieldHealth.get() - damage <= 0) {
            this.playShieldBreakSound();
        }

        this.shieldHealth.set(Math.max(0f,this.shieldHealth.get() - damage));
    }

    public boolean hasShield() {
        return this.shieldHealth.get() > 0;
    }

    /* NBT data */

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt(STATE_NBT_KEY,this.getState().ordinal());
        nbt.putLong(ALIVE_TICKS_NBT_KEY,this.aliveTimer.ticks());
        nbt.putFloat(SHIELD_HEALTH_NBT_KEY,this.shieldHealth.get());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains(STATE_NBT_KEY)) {
            this.setState(TheManState.values()[nbt.getInt(STATE_NBT_KEY)]);
        }
        if (nbt.contains(ALIVE_TICKS_NBT_KEY)) {
            this.aliveTimer.setTicks(nbt.getLong(ALIVE_TICKS_NBT_KEY));
        }
        if (nbt.contains(SHIELD_HEALTH_NBT_KEY)) {
            this.shieldHealth.set(nbt.getFloat(SHIELD_HEALTH_NBT_KEY));
        }
    }

    /* Animations */
    private PlayState movementAnimController(AnimationState<TheManEntity> event) {
        if (this.climbing.get()) {
            return event.setAndContinue(TheManAnimations.CLIMB);
        }

        if (event.isMoving()) {
            if (this.getState() == TheManState.STALK) {
                return event.setAndContinue(TheManAnimations.SNEAK_RUN);
            }

            if (this.crouching.get()) {
                return event.setAndContinue(TheManAnimations.CROUCH_RUN);
            }

            if (this.crawling.get()) {
                return event.setAndContinue(TheManAnimations.CRAWL_RUN);
            }

            return event.setAndContinue(TheManAnimations.RUN);
        }

        return event.setAndContinue(TheManAnimations.IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this,"Movement", 5,this::movementAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    /* Hallucinations */
    public boolean isHallucination() {
        return this instanceof TheManEntityHallucination;
    }

    public boolean isParanoia() {
        return this instanceof TheManEntityParanoia;
    }

    public boolean isReal() {
        return !this.isHallucination() && !this.isParanoia();
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

    public void playSpitSound() {
        this.playSound(ModSounds.MAN_SPIT,this.getSoundVolume(),this.getSoundPitch());
    }

    public void playLungeAttackSound() {
        this.playSound(ModSounds.MAN_LUNGE_ATTACK,this.getLoudSoundVolume(),this.getSoundPitch());
    }

    public void playShieldBreakSound() {
        this.playSound(ModSounds.SHIELD_BREAK,this.getLoudSoundVolume(),1f);
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

    public void playShieldBlockSound() {
        this.playSound(SoundEvents.ITEM_SHIELD_BLOCK,this.getLoudSoundVolume(),this.getSoundPitch());
    }

    /* Properties and Behavior */

    @Override
    public float getStepHeight() {
        return super.getStepHeight();
    }

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
        return true;
    }

    @Override
    public boolean isGlowing() {
        return FogMod.DEBUG;
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return effect.getEffectType().isBeneficial();
    }

    public static boolean canManSpawn(ServerWorld serverWorld) {
        return !(TheManUtils.manExists(serverWorld) || TheManUtils.hallucinationsExists(serverWorld));
    }

    public static boolean isInAllowedDimension(World world) {
        if (world.isClient()) {
            return world.getRegistryKey() == World.OVERWORLD || world.getRegistryKey() == ModDimensions.ENSHROUDED_LEVEL_KEY;
        }
        return true;
        //return FogMod.CONFIG.allowedDimensions.contains(world.getRegistryKey().getValue().toString()) || world.getRegistryKey() == ModDimensions.ENSHROUDED_LEVEL_KEY;
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return this.canSpawn(world);
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return canManSpawn(this.getServerWorld());
    }

    @Override
    public boolean collidesWithStateAtPos(BlockPos pos, BlockState state) {

        if (pos.getY() >= this.getBlockY() && state.isIn(BlockTags.LEAVES)) {
            return false;
        }

        return super.collidesWithStateAtPos(pos, state);
    }

    @Override
    public boolean collidesWith(Entity other) {
        return other.isPlayer() && other.isCollidable();
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        super.onBlockCollision(state);
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
        if ((WorldHelper.isDay(this.getWorld()) && !FogMod.CONFIG.spawning.spawnInDay) || !this.isReal()) {
            return;
        }
        this.dropStack(new ItemStack(Items.WITHER_ROSE,RandomNum.next(1,6)));

        if (this.getKilledCount() < 2) {
            if (RandomNum.nextDouble() < 0.45) {
                this.dropStack(new ItemStack(ModItems.CLAWS,1));
            } else {
                this.dropStack(new ItemStack(ModItems.TEAR_OF_THE_MAN,1));
            }
        }
    }

    @Override
    public int getXpToDrop() {
        return Math.round(52.0f * (1f - MathHelper.clamp((float) this.getKilledCount() / FogMod.CONFIG.spawning.dayAmountToStopSpawn,0f,1f)));
    }

    public float getLowHealthPercent() {
        return 1f - (this.getHealth() / this.getMaxHealth());
    }

    @Override
    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        return 0;
    }

    public void blockDamage(DamageSource source,float amount) {
        Entity attacker = source.getAttacker();

        if (attacker == null) {
            return;
        }

        if (attacker instanceof LivingEntity livingEntity) {
            if (amount > 0 && (source.getTypeRegistryEntry() == DamageTypes.PLAYER_ATTACK || source.getTypeRegistryEntry() == DamageTypes.MOB_ATTACK)) {
                livingEntity.damage(new DamageSource(source.getTypeRegistryEntry(),this),amount);
            }
            this.playShieldBlockSound();
        }
    }

    public void blockDamage(DamageSource source) {
        this.blockDamage(source,0);
    }

    @Override
    public void kill() {
        this.addToKilledCount();
        setDayKilled(this.getWorld(), TimeHelper.ticksToDays(this.getServerWorld().getTimeOfDay()));
        this.despawn();
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (this.getWorld().isClient()) return;

        if (!this.isReal()) {
            this.despawn(!this.isParanoia());
            return;
        }

        WorldComponent.get(this.getWorld()).setTheManHealth((float) createAttributes().build().getValue(EntityAttributes.GENERIC_MAX_HEALTH));

        this.addToKilledCount();
        setDayKilled(this.getWorld(), TimeHelper.ticksToDays(this.getServerWorld().getTimeOfDay()));

        super.onDeath(damageSource);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.getWorld().getRegistryKey() == ModDimensions.ENSHROUDED_LEVEL_KEY) {
            this.blockDamage(source);
            return false;
        }

        if (this.getState() == TheManState.STARE || this.getState() == TheManState.STALK) {
            this.blockDamage(source);
            return false;
        }

        if (WorldHelper.isBloodMoon(this.getWorld()) || WorldHelper.isSuperBloodMoon(this.getWorld())) {
            this.blockDamage(source);
            return false;
        }

        RegistryEntry<DamageType> damageType = source.getTypeRegistryEntry();

        if (damageType.getKey().isEmpty()) return false;

        boolean isBullet = damageType.getKey().get().getValue().getPath().contains("bullet");

        if (!ALLOWED_DAMAGE_TYPES.contains(damageType.getKey().get()) && !isBullet) {
            this.blockDamage(source);
            return false;
        }

        if (isBullet) {
            if (this.getHealth() < this.getMaxHealth() / 2) {
                this.blockDamage(source);
                return false;
            }
            amount = 0.2f;
        }

        if (this.hasShield()) {
            if (isBullet) {
                this.blockDamage(source);
                return false;
            }
            this.damageShield(amount);
            return true;
        }

        if (WorldHelper.isNight(this.getWorld()) || FogMod.CONFIG.spawning.spawnInDay) {

            Entity attacker = source.getAttacker();

            if (attacker instanceof LivingEntity livingEntity && !livingEntity.getMainHandStack().isOf(ModItems.CLAWS) && this.isReal()) {

                if (WorldHelper.isInLightSource(this.getWorld(),livingEntity.getBlockPos(),TheManPredicates.LANTERN_PREDICATE)) {
                    this.blockDamage(source);

                    return false;
                }

                if (!this.canAttack(livingEntity)) {
                    this.blockDamage(source);

                    return false;
                }

                if (attacker instanceof IronGolemEntity) {
                    this.blockDamage(source,amount * 2f);

                    return false;
                }

                BlockHitResult blockHitResult = this.getWorld().raycast(new BlockStateRaycastContext(
                        livingEntity.getEyePos(),
                        new Vec3d(this.getX(),livingEntity.getEyeY(),this.getZ()),
                        TheManPredicates.BLOCK_STATE_PREDICATE
                ));
                EntityHitResult entityHitResult = ProjectileUtil.raycast(
                        livingEntity,
                        livingEntity.getEyePos(),
                        new Vec3d(this.getX(),livingEntity.getEyeY(),this.getZ()),
                        Box.of(this.getPos(),10,10,10),
                        entity -> !(entity instanceof LivingEntity),
                        15
                );

                if (blockHitResult != null && blockHitResult.getType() == HitResult.Type.BLOCK) {
                    return false;
                }

                if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
                    return false;
                }

                if (this.blockChance < 0.9) {
                    this.blockChance += 0.1 * (1.0 + MathHelper.clamp((double) this.getKilledCount() / FogMod.CONFIG.spawning.dayAmountToStopSpawn,0.0,1.0));
                } else {
                    this.blockChance = 0.9;
                }

                if (RandomNum.nextDouble() < this.blockChance || RandomNum.nextDouble() < MathHelper.clamp(this.getLowHealthPercent(),0.1f,0.8f)) {
                    this.blockDamage(source,amount / 4f);

                    this.aliveTimer.setTicks(this.aliveTimer.ticks() - 20);

                    return false;
                }
            }

            this.aliveTimer.setTicks(this.aliveTimer.ticks() + 10);
        }

        return super.damage(source, amount);
    }

    public void addStatusEffects() {
        if (!this.isReal()) {
            return;
        }
        this.addStatusEffect(TheManStatusEffects.REGENERATION);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
    }

    public void despawn(boolean withLightning) {
        WorldComponent.get(this.getWorld()).setTheManHealth(this.getHealth());
        if (withLightning) {
            TheManUtils.doLightning(this.getServerWorld(),this);
        }
        this.discard();
    }

    public void despawn() {
        this.despawn(true);
    }

    @Override
    protected Vec3d getAttackPos() {
        return this.getPos();
    }

    public boolean canLunge() {
        return !this.lunging.get() && this.isOnGround();
    }

    public void lunge(double x, double y, double z, double verticalForce) {
        if (!this.canLunge()) return;

        var velX = (x - this.getX()) * 0.4;
        var velY = verticalForce;
        var velZ = (z - this.getZ()) * 0.4;

        this.playLungeSound();
        this.playLungeAttackSound();
        this.setVelocity(velX,velY,velZ);
        this.lunging.set(true);
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
        if (this.getState() != TheManState.CHASE || !this.isReal()) {
            return;
        }

        ServerWorld serverWorld = this.getServerWorld();

        if (TheManUtils.hallucinationsExists(serverWorld)) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            var xOffset = (RandomNum.nextBoolean() ? 1 : -1) * RandomNum.next(2,11);
            var zOffset = (RandomNum.nextBoolean() ? 1 : -1) * RandomNum.next(2,11);

            var hallucination = new TheManEntityHallucination(ModEntities.THE_MAN_HALLUCINATION,serverWorld);

            hallucination.setPosition(this.getPos().add(xOffset,0,zOffset));

            serverWorld.spawnEntity(hallucination);
        }
    }

    public static boolean isObstructed(World world, Vec3d origin, Vec3d target) {
        return world.raycast(new BlockStateRaycastContext(origin,target, BlockStatePredicate.ANY)).getType() != HitResult.Type.MISS;
    }

    public void chaseIfTooClose(double radius) {
        if (this.getTarget() != null && this.getTarget().isInRange(this,radius)) {
            var hitResult = this.getWorld().raycast(new BlockStateRaycastContext(
                    this.getEyePos(),
                    this.getTarget().getEyePos(),
                    TheManPredicates.LOOK_BLOCK_STATE_PREDICATE
            ));
            if (hitResult.getType() == HitResult.Type.MISS) {
                this.startChase();
            }
        }
    }

    public void chaseIfTooClose() {
        this.chaseIfTooClose(15);
    }

    public void handleSurroundingBlocks() {
        if (this.isDead() || !this.isReal()) {
            return;
        }

        var serverWorld = this.getServerWorld();

        for (var blockPos : BlockPos.iterateOutwards(this.getBlockPos().up(), 1, 1, 1)) {
            var blockState = serverWorld.getBlockState(blockPos);
            if (blockState.isAir() || blockState.isOf(Blocks.LAVA) || blockState.isOf(Blocks.WATER)) {
                continue;
            }

            if (blockState.isIn(BlockTags.DOORS) || blockState.isIn(BlockTags.TRAPDOORS)) {
                serverWorld.removeBlock(blockPos,false);
            }
        }
    }

    public final Predicate<BlockPos> SAFE_BLOCK_PREDICATE = blockPos -> {
        var blockState = this.getWorld().getBlockState(blockPos);
        return !blockState.isAir() && blockState.getFluidState().isEmpty();
    };

    @Nullable public BlockPos getSafeBlockPos() {
        BlockPos currentBlockPos = this.getBlockPos();
        BlockPos closestBlockPos = WorldHelper.getClosestBlockPos(
                currentBlockPos,
                BlockPos.iterateOutwards(this.getBlockPos(),2,2,2),
                this.SAFE_BLOCK_PREDICATE
        );

        while (closestBlockPos == null) {
            currentBlockPos = currentBlockPos.up(3);
            if (currentBlockPos.getY() >= this.getWorld().getTopY()) break;
            closestBlockPos = WorldHelper.getClosestBlockPos(
                    currentBlockPos,
                    BlockPos.iterateOutwards(this.getBlockPos(),2,2,2),
                    this.SAFE_BLOCK_PREDICATE
            );
        }
        
        return closestBlockPos == null ? null : closestBlockPos.up();
    }

    public boolean tryAttackTarget(Entity target) {
        if (target instanceof LivingEntity livingEntity && WorldHelper.isInLightSource(this.getWorld(),livingEntity.getBlockPos(),TheManPredicates.LANTERN_PREDICATE) && !WorldHelper.isSuperBloodMoon(this.getWorld())) return false;

        float attackDamage = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * (1f + MathHelper.clamp((float) this.getKilledCount() / FogMod.CONFIG.spawning.dayAmountToStopSpawn,0f,1f));
        float attackKnockback = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        if (target instanceof LivingEntity) {
            attackDamage += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), ((LivingEntity)target).getGroup());
            attackKnockback += (float)EnchantmentHelper.getKnockback(this);
        }

        if (WorldHelper.isBloodMoon(this.getWorld())) {
            attackDamage *= 2;
        }

        if (WorldHelper.isSuperBloodMoon(this.getWorld())) {
            attackDamage *= 4;
        }

        int fireAspectLevel = EnchantmentHelper.getFireAspect(this);
        if (fireAspectLevel > 0) {
            target.setOnFireFor(fireAspectLevel * 4);
        }

        boolean damaged = target.damage(this.getDamageSources().create(ModDamageTypes.MAN_ATTACK_DAMAGE_TYPE,this), attackDamage);
        if (damaged) {
            if (attackKnockback > 0.0F && target instanceof LivingEntity livingEntity) {
                livingEntity.takeKnockback(
                                attackKnockback * 0.5F,
                                MathHelper.sin(this.getYaw() * (float) (Math.PI / 180.0)),
                                -MathHelper.cos(this.getYaw() * (float) (Math.PI / 180.0))
                        );
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            }

            if (target instanceof PlayerEntity playerEntity && RandomNum.nextDouble() < SHIELD_DISABLE_CHANCE) {
                playerEntity.disableShield(playerEntity.isSprinting());
            }

            this.applyDamageEffects(this, target);
            this.onAttacking(target);
        }

        return damaged;
    }

    @Override
    public boolean tryAttack(Entity target) {
        if (!this.isReal()) {
            this.despawn();
            return false;
        }
        this.playAttackSound();
        this.playSlashSound();

        if (this.getState() == TheManState.STALK || this.lunging.get()) {
            target.damage(this.getDamageSources().create(DamageTypes.MOB_ATTACK,this),20f);
            this.startChase();
            return true;
        }

        return this.tryAttackTarget(target);
    }

    public void spitAt(LivingEntity target) {
        if (!this.isSilent()) {
            this.playSpitSound();
        }

        for (int i = 0; i < TheManSpitEntity.AMOUNT_PER_SPIT; i++) {
            var spitEntity = new TheManSpitEntity(this.getWorld(),this);
            var direction = target.getPos().subtract(this.getPos()).normalize();
            var speed = target.distanceTo(this) * 1.5f;
            spitEntity.setVelocity(direction.getX(),direction.getY(),direction.getZ(),speed,0.05f);
            this.getWorld().spawnEntity(spitEntity);
        }
    }

    @Override
    public double squaredAttackRange(LivingEntity target) {
        return (this.getWidth() * 2.0F * this.getWidth() * 2.0F + target.getWidth()) + 0.5;
    }

    public void attemptAttack() {
        if (this.getWorld().isClient()) return;
        var world = this.getServerWorld();
        var width = this.currentHitboxSize.width + ATTACK_HITBOX_SIZE;
        var players = world.getPlayers(
                TargetPredicate.DEFAULT,
                this,
                Box.of(this.getPos(),width,this.currentHitboxSize.height,width)
        );
        if (players.isEmpty()) return;
        if (!this.isReal()) {
            this.despawn();
            return;
        }
        this.swingHand(Hand.MAIN_HAND);
        players.forEach(this::tryAttack);
    }

    public void updateHitbox() {
        if (this.lunging.get()) {
            this.currentHitboxSize = LUNGE_HITBOX_SIZE;
            return;
        }

        if (this.climbing.get()) {
            this.currentHitboxSize = CLIMB_HITBOX_SIZE;
            return;
        }

        if (this.crouching.get()) {
            this.currentHitboxSize = CROUCH_HITBOX_SIZE;
            return;
        }

        if (this.crawling.get()) {
            this.currentHitboxSize = CRAWL_HITBOX_SIZE;
            return;
        }

        this.currentHitboxSize = HITBOX_SIZE;
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return this.currentHitboxSize;
    }

    @Override
    public float getEyeHeight(EntityPose pose) {
        return this.getEyeHeight(pose,this.getDimensions(pose));
    }

    public void findPath(double x, double y, double z) {
        if (this.getWorld().isClient()) return;
        if (!this.shouldComputePath) return;
        this.shouldComputePath = false;
        this.currentPath = this.getNavigation().findPathTo(x,y,z,1);
    }

    public void findPath(Vec3d position) {
        this.findPath(position.getX(),position.getY(),position.getZ());
    }

    public void stopMoving() {
        if (this.getNavigation().isFollowingPath()) {
            this.getNavigation().stop();
        }
        if (this.getMoveControl().isMoving()) {
            this.getMoveControl().moveTo(this.getX(),this.getY(),this.getZ(),1f);
        }
    }

    public float getSpeedModifier() {
        var modifier = FogMod.CONFIG.behavior.speedMultiplier;

        if (WorldHelper.isBloodMoon(this.getWorld())) return 2f * modifier;
        if (WorldHelper.isSuperBloodMoon(this.getWorld())) return 5f * modifier;

        if (this.crouching.get()) return 0.9f * modifier;
        if (this.crawling.get()) return 0.75f * modifier;

        return modifier;
    }

    public void moveTo(double x, double y, double z, double speed) {
        if (this.getWorld().isClient()) return;

        if (!this.isReal()) {
            this.moveControl.moveTo(x,y,z,speed * this.getSpeedModifier());
            return;
        }

        var world = this.getServerWorld();

        if (this.isReal() && !WorldHelper.isSuperBloodMoon(world)) {
            var lightSourcePos = WorldHelper.getLightSource(world,this.getBlockPos(),TheManPredicates.LANTERN_PREDICATE, i -> Math.max(1,Math.round(i / 1.2f)));
            if (lightSourcePos != null) {
                var dirToLight = this.getPos().subtract(lightSourcePos.toCenterPos()).normalize();
                dirToLight = new Vec3d(dirToLight.getX(),0,dirToLight.getZ());
                var moveToPos = this.getPos().add(dirToLight);
                this.moveControl.moveTo(moveToPos.getX(),moveToPos.getY(),moveToPos.getZ(),1.2f);
                return;
            }
        }

        //this.findPath(x,y,z);
        this.pathfindingAgent.setSpeed(speed);
        this.pathfindingAgent.findPathTo(x,y,z);
        /*if (this.currentPath != null) {
            this.getNavigation().startMovingAlong(this.currentPath,speed * this.getSpeedModifier());
        } else {
            this.moveControl.moveTo(x,y,z,speed * this.getSpeedModifier());
        }*/

        /*if (this.path != null) {
            this.getNavigation().startMovingAlong(this.path,speed * this.getSpeedModifier());
        } else {
            this.moveControl.moveTo(x,y,z,speed * this.getSpeedModifier());
        }*/
    }

    public void moveTo(Vec3d position, double speed) {
        this.moveTo(position.getX(),position.getY(),position.getZ(),speed);
    }

    public void moveTo(Entity target, double speed) {
        this.moveTo(target.getX(),target.getY(),target.getZ(),speed);
    }

    public boolean isMoving() {
        return this.getMoveControl().isMoving();
    }

    public void teleportBehindTarget() {
        if (this.getWorld().isClient() || this.getTarget() == null) return;
        var spawnPosition = WorldHelper.getRandomSpawnBehindDirection(this.getServerWorld(),this.getTarget().getPos(), GeometryHelper.calculateDirection(0,this.getTarget().getYaw(1.0f)));
        this.setPosition(spawnPosition);
        this.farAwayTimer.pause();
        this.farAwayTimer.setTicks(TOO_FAR_AWAY_TICKS);
    }

    @Override
    public boolean isSilent() {
        return this.getState() == TheManState.STALK;
    }

    public static boolean canAttack(LivingEntity livingEntity,World world) {
        return !(TrinketsApi.getTrinketComponent(livingEntity).isPresent() && TrinketsApi.getTrinketComponent(livingEntity).get().isEquipped(ModItems.EREBUS_ORB)) || WorldHelper.isSuperBloodMoon(world);
    }

    public boolean canAttack(LivingEntity livingEntity) {
        return canAttack(livingEntity,this.getWorld());
    }

    public LivingEntity getClosestPlayer() {
        double closestDistance = -1.0;
        LivingEntity target = null;

        for (var player : this.getWorld().getPlayers()) {
            if (!TheManPredicates.TARGET_PREDICATE.test(player)) continue;
            var playerUUID = player.getGameProfile().getId();
            if (!PlayerHelper.isHidden(player)) {
                this.targetablePlayers.add(playerUUID);
            }
            if (!this.targetablePlayers.contains(playerUUID)) continue;

            double distance = player.squaredDistanceTo(this.getX(),this.getY(),this.getZ());

            if (closestDistance == -1.0 || distance < closestDistance) {
                closestDistance = distance;
                target = player;
            }
        }

        return target;
    }

    public void updateTarget() {
        if (this.getWorld().isClient()) return;
        this.setTarget(this.getClosestPlayer());

        if (this.getTarget() != null) {
            this.lastKnownTargetPosition = this.getTarget().getPos();
        }
    }

    public void forget() {
        if (this.getWorld().isClient()) return;
        var it = this.targetablePlayers.iterator();
        while (it.hasNext()) {
            var uuid = it.next();
            var player = PlayerHelper.getPlayerById(this.getServerWorld().getServer(),uuid);
            if (player == null) continue;
            if (player.isDead() || PlayerHelper.isHidden(player)) {
                it.remove();
            }
        }
    }

    @Nullable
    public Vec3d getLastKnownTargetPosition() {
        return this.lastKnownTargetPosition;
    }

    /* Ticking */
    public ServerWorld getServerWorld() {
        if (this.getWorld().isClient()) {
            throw new Error("Attempt to get a ServerWorld in a Client thread");
        }
        return (ServerWorld) this.getWorld();
    }

    public void serverTick(ServerWorld serverWorld) {
        if (this.isAiDisabled()) return;

        this.pathfindingAgent.tick();

        this.attackTimer.tick();
        this.aliveTimer.tick();
        if (this.getState() == TheManState.CHASE && !WorldHelper.isBloodMoon(serverWorld) && !WorldHelper.isSuperBloodMoon(serverWorld)) {
            this.aliveTimer.resume();
        } else {
            this.aliveTimer.pause();
        }
        this.hitboxUpdateTimer.tick();
        this.targetDetectTimer.tick();
        this.farAwayTimer.tick();
        this.forgetTimer.tick();

        var oldSpeed = this.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        var newSpeed = FogMod.CONFIG.statusEffects.giveSpeed ? SPEED : SPEED_NO_STATUS_EFFECT;
        if (oldSpeed != newSpeed) {
            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(newSpeed);
        }

        if (this.isInsideWall()) {
            var safeBlockPos = this.getSafeBlockPos();
            if (safeBlockPos != null) {
                this.setPosition(safeBlockPos.toCenterPos());
            }
        }

        if (!this.shouldComputePath && --this.pathComputeTicks <= 0L) {
            this.shouldComputePath = true;
            this.pathComputeTicks = RandomNum.next(MIN_PATH_COMPUTE_TICKS,MAX_PATH_COMPUTE_TICKS);
        }

        if (this.getTarget() != null && this.distanceTo(this.getTarget()) > CHASE_DISTANCE) {
            this.farAwayTimer.resume();
        } else {
            this.farAwayTimer.pause();
            this.farAwayTimer.setTicks(TOO_FAR_AWAY_TICKS);
        }

        if (this.isAlive() && !this.isReal()) {
            this.setHealth(this.getHealth() - 4f);
        }

        if (WorldHelper.isDay(serverWorld) && !FogMod.CONFIG.spawning.spawnInDay) {
            this.despawn();
            return;
        }

        if (this.blockChance > BLOCK_CHANCE) {
            this.blockChance -= 0.01;
        }

        if (this.blockChance < BLOCK_CHANCE) {
            this.blockChance = BLOCK_CHANCE;
        }

        this.movementTick(serverWorld);
        this.getStateMachine().tick(serverWorld);

        if (this.lunging.get() && this.isOnGround()) {
            this.lunging.set(false);
        }

        // If we are right above target, we move with the same velocity as the target and also down
        if (this.lunging.get() && this.getTarget() != null) {
            var oldVelocity = this.getVelocity();
            var origin = new Vec3d(this.getX(),0,this.getZ());
            var target = new Vec3d(this.getTarget().getX(),0,this.getTarget().getZ());
            var targetVelocity = this.getTarget().getVelocity();

            this.setVelocity(
                    target.getX() - origin.getX(),
                    oldVelocity.getY(),
                    target.getZ() - origin.getZ()
            );

            if (target.isInRange(origin,10)) {
                this.setVelocity(targetVelocity.getX(),-2,targetVelocity.getZ());
                this.lunging.set(false);
            }
        }

        if (!this.isReal()) {
            var targetEntity = this.getTarget();
            if (targetEntity != null) {
                this.setPos(this.getX(),targetEntity.getY(),this.getZ());
            }

            var oldVelocity = this.getVelocity();
            this.setVelocity(oldVelocity.getX(),0,oldVelocity.getZ());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient()) {
            this.serverTick(this.getServerWorld());
        }
    }

    public void stop() {
        var verticalVelocity = this.getVelocity().getY();
        this.setVelocity(0,verticalVelocity,0);
    }

    @Nullable
    public BlockPos getPillarPos(BlockPos center) {
        var world = this.getWorld();
        for (BlockPos blockPos : BlockPos.iterateOutwards(center,1,0,1)) {
            if (blockPos.equals(center)) continue;

            if (TheManPredicates.CLIMBABLE_PREDICATE.test(world,blockPos)) {
                return blockPos;
            }
        }

        return null;
    }

    public int getPillarHeight(BlockPos blockPos) {
        var world = this.getWorld();

        int height = 0;

        var currentBlockPos = blockPos;

        while (TheManPredicates.CLIMBABLE_PREDICATE.test(world,currentBlockPos)) {
            currentBlockPos = currentBlockPos.up();
            height += 1;
            if (height > world.getTopY()) {
                break;
            }
        }

        if (height > 0) {
            height += Math.round(this.getHeight());
        }

        return height;
    }

    /*@Nullable BlockPos getClimbablePos(BlockPos center,BlockPos targetPosition) {
        var world = this.getWorld();

        var availablePosList = new ArrayList<BlockPos>();

        for (var pos : BlockPos.iterateOutwards(center,1,0,1)) {
            if (pos.equals(center)) continue;
            if (TheManPredicates.CLIMBABLE_BLOCK_PREDICATE.test(world,pos)) continue;

            availablePosList.add(pos);
        }

        if (availablePosList.isEmpty()) return null;

        @Nullable BlockPos bestPos = null;
        double bestDistance = -1.0;

        for (var pos : availablePosList) {
            var distance = pos.getSquaredDistance(targetPosition);

            if (bestDistance == -1.0 || distance < bestDistance) {
                bestDistance = distance;
                bestPos = pos;
            }
        }

        return bestPos;
    }*/

    @Nullable BlockPos getClimbablePos(BlockPos center,BlockPos targetPosition) {
        var world = this.getWorld();
        for (var pos : BlockPos.iterateOutwards(center,1,0,1)) {
            if (pos.equals(center)) continue;
            if (TheManPredicates.CLIMBABLE_PREDICATE.test(world,pos)) continue;
            if (TheManPredicates.CLIMBABLE_PREDICATE.test(world,pos.up())) continue;
            return pos;
        }
        return null;
    }

    public boolean isClimbable(BlockPos center,BlockPos targetPosition,int height) {
        for (int i = 0; i <= height; i++) {
            var blockPos = center.up(i);

            if (this.getClimbablePos(blockPos,targetPosition) == null) return false;
        }

        return true;
    }

    private void resetClimbing() {

    }

    private boolean shouldClimb() {
        if (this.currentPath == null) return false;

        var currentIndex = Math.min(this.currentPath.getCurrentNodeIndex(),this.currentPath.getLength() - 1);
        var nextIndex = Math.min(this.currentPath.getCurrentNodeIndex() + 1,this.currentPath.getLength() - 1);
        var currentNode = this.currentPath.getNode(currentIndex);
        var nextNode = this.currentPath.getNode(nextIndex);

        //Console.writeln("Current node height: %d",currentNode.getBlockPos().getY());
        //Console.writeln("Next node height: %d",nextNode.getBlockPos().getY());

        return (currentNode.type == ModPathNodeTypes.WALL_HUGGING || nextNode.type == ModPathNodeTypes.WALL_HUGGING) && nextNode.getBlockPos().getY() > currentNode.getBlockPos().getY();
    }

    public void climbTick(ServerWorld serverWorld) {

        this.climbing.set(this.shouldClimb());

        if (this.climbing.get() && this.currentPath != null) {
            System.out.println("climb");
            var currentIndex = Math.min(this.currentPath.getCurrentNodeIndex(),this.currentPath.getLength() - 1);
            var currentNode = this.currentPath.getNode(currentIndex);
            var velocity = currentNode.getPos().subtract(this.getPos()).normalize().multiply(CLIMB_SPEED);
            this.setVelocity(velocity);
        }

        //this.climbing.set(this.navigator.isOnClimbableNode());
        /*if (this.desiredPosition != null) {
            var direction = this.desiredPosition.subtract(this.getPos()).normalize();
            this.setPosition(this.getPos().add(direction.multiply(MAN_CLIMB_SPEED)));
        }

        if (this.getTarget() == null) {
            this.resetClimbing();
            this.setClimbing(false);
            return;
        }

        this.setClimbing(this.shouldClimb(this.getTarget().getBlockY()));

        if (!this.isClimbing()) {
            this.resetClimbing();
            return;
        }

        this.pillarBlockPos = this.getPillarPos(this.getBlockPos());

        if (this.pillarBlockPos == null) {
            this.resetClimbing();
            this.setClimbing(false);
            return;
        }

        var height = this.getPillarHeight(this.pillarBlockPos);

        if (height < 2 || !this.isClimbable(this.pillarBlockPos,this.getTarget().getBlockPos(),height)) {
            this.resetClimbing();
            this.setClimbing(false);
            return;
        }

        var pillarPos = this.pillarBlockPos.toCenterPos();
        var nextOrigin = this.pillarBlockPos.up();

        this.getLookControl().lookAt(pillarPos.getX(),this.getY(),pillarPos.getZ(),360f,30f);
        this.setVelocity(0.0,MAN_CLIMB_SPEED,0.0);

        var bestBlockPos = this.navigator.getPos();
        if (bestBlockPos != null) {
            this.desiredPosition = bestBlockPos.toCenterPos();
        } else {
            this.resetClimbing();
            this.setClimbing(false);
        }*/
    }

    public void movementTick(ServerWorld serverWorld) {
        if (this.isSubmergedInWater() && (this.getTarget() == null || this.getTarget().getBlockY() >= this.getBlockY())) {
            Vec3d oldVelocity = this.getVelocity().normalize();
            this.setVelocity(oldVelocity.getX() * this.getMovementSpeed(),0.5,oldVelocity.getZ() * this.getMovementSpeed());
        }

        this.climbTick(serverWorld);

        if (this.currentPath != null) {
            var currentIndex = Math.min(this.currentPath.getCurrentNodeIndex(),this.currentPath.getLength() - 1);
            var currentNode = this.currentPath.getNode(currentIndex);
            this.crouching.set(currentNode.type == ModPathNodeTypes.CROUCHABLE);
            this.crawling.set(currentNode.type == ModPathNodeTypes.CRAWLABLE);
        }

        this.calculateDimensions();
    }

    /* Other */

    public void addEffectToTarget(StatusEffectInstance statusEffectInstance) {
        if (!this.isReal() || this.getTarget() == null) {
            return;
        }
        var target = this.getTarget();

        if (!this.getPos().isInRange(target.getPos(),10)) return;

        var statusEffect = target.getStatusEffect(statusEffectInstance.getEffectType());

        if (statusEffect == null || statusEffect.getDuration() < STATUS_EFFECT_DURATION / 2) {
            target.addStatusEffect(new StatusEffectInstance(statusEffectInstance));
        }
    }

    public int getKilledCount() { return getKilledCount(this.getWorld()); }

    public void addToKilledCount() {
        addToKilledCount(this.getWorld());
    }

    public void resetKilledCount() {
        resetKilledCount(this.getWorld());
    }

    // Static overloads for easier life

    public static int getKilledCount(World world) {
        return WorldComponent.get(world).killCount();
    }

    public static void addToKilledCount(World world) {
        WorldComponent.get(world).setKillCount(getKilledCount(world) + 1);
    }

    public static void resetKilledCount(World world) {
        WorldComponent.get(world).setKillCount(0);
    }

    public static long getDayKilled(World world) {
        return WorldComponent.get(world).dayKilled();
    }

    public static void setDayKilled(World world, long day) {
        WorldComponent.get(world).setDayKilled(day);
    }

    @Override
    public HashSet<UUID> getPlayersWithLOS() {
        return this.playersWithLineOfSight;
    }
}

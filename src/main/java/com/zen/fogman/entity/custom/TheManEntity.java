package com.zen.fogman.entity.custom;

import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.goals.custom.BreakDoorInstantGoal;
import com.zen.fogman.item.ModItems;
import com.zen.fogman.other.MathUtils;
import com.zen.fogman.sounds.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
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
import net.minecraft.util.Hand;
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
import java.util.Random;

public class TheManEntity extends HostileEntity implements GeoEntity {
    public static final double MAN_SPEED = 0.66;
    public static final double MAN_CLIMB_SPEED = 1.2;
    public static final double MAN_MAX_SCAN_DISTANCE = 100000.0;
    public static final int MAN_CHASE_DISTANCE = 200;

    private static final TrackedData<Boolean> MAN_CLIMBING_FLAG = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> MAN_STATE_FLAG = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.INTEGER);

    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation CLIMB_ANIM = RawAnimation.begin().thenLoop("climb");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public final Random random2 = new Random();
    public long aliveTime;
    private long lastTime;

    private long lastHallucinationTime;

    private int targetFOV = 90;

    private ManState state;

    public EntityTrackingSoundInstance chaseSoundInstance;

    // Chase stuff
    private int cooldown;
    private long lastMoveTime;
    private long lastLungeTime;
    private boolean didLunge = false;

    // Stare stuff
    private long stareTime;

    public TheManEntity(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType, world);

        this.lastMoveTime = MathUtils.tickToSec(this.getWorld().getTime());
        this.lastLungeTime = MathUtils.tickToSec(this.getWorld().getTime());
        this.stareTime = MathUtils.tickToSec(this.getWorld().getTime());

        this.lastTime = MathUtils.tickToSec(this.getWorld().getTime());
        this.lastHallucinationTime = MathUtils.tickToSec(this.getWorld().getTime());
        this.aliveTime = this.random2.nextLong(30,60);

        this.setState(ManState.STARE);
        this.chaseSoundInstance = new EntityTrackingSoundInstance(ModSounds.MAN_CHASE,SoundCategory.HOSTILE,0.6f,1.0f,this,this.getWorld().getTime());
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
        return super.getSoundCategory();
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
        this.playSpottedSound();
        TheManEntity.doLightning(serverWorld,this.getPos());
    }

    public void addEffectsToClosePlayers(ServerWorld world, Vec3d pos, @Nullable Entity entity, int range) {
        StatusEffectInstance darknessInstance = new StatusEffectInstance(StatusEffects.DARKNESS, 260, 2, false, false);
        StatusEffectUtil.addEffectToPlayersWithinDistance(world, entity, pos, range, darknessInstance, 200);
        StatusEffectInstance nightVisionInstance = new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, false, false);
        StatusEffectUtil.addEffectToPlayersWithinDistance(world, entity, pos, range, nightVisionInstance, 200);
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
        nav.setSpeed(MAN_SPEED * 2);
        return nav;
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
        return this.isChasing() && this.getTarget() != null && MathUtils.distanceTo(this,this.getTarget()) <= MAN_CHASE_DISTANCE / 4.0;
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

        for (BlockPos blockPos : BlockPos.iterateOutwards(this.getBlockPos(), 0, 2, 0)) {
            BlockState blockState = serverWorld.getBlockState(blockPos);
            if (blockState.isAir() || this.getBlockPos().getY() >= blockPos.getY()) {
                continue;
            }

            if (blockState.getBlock().getHardness() <= 2.0 && blockState.getBlock().getHardness() >= 1.5) {
                serverWorld.breakBlock(blockPos, true);
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

    public void clientTick(MinecraftClient client) {
        this.updateTargetFov(client);
        if (this.isChasing()) {
            this.playChaseSound(client);
        }
    }

    public void idleTick(ServerWorld serverWorld) {

    }

    public void chaseTick(ServerWorld serverWorld) {

        this.breakBlocksAround(serverWorld);

        LivingEntity target = this.getTarget();

        if (target == null) {
            return;
        }

        double d = this.getSquaredDistanceToAttackPosOf(target);

        if (this.random2.nextFloat(0f,1f) < 0.25 && !didLunge) {
            didLunge = true;
            doLunge(target);
        }

        if (didLunge) {
            if (MathUtils.tickToSec(this.getWorld().getTime()) - this.lastLungeTime > 20) {
                didLunge = false;
                this.lastLungeTime = MathUtils.tickToSec(this.getWorld().getTime());
            }
        } else {
            this.lastLungeTime = MathUtils.tickToSec(this.getWorld().getTime());
        }

        this.getLookControl().lookAt(target, 30.0f, 30.0f);

        if (MathUtils.tickToSec(this.getWorld().getTime()) - this.lastMoveTime > 0.05) {
            this.getNavigation().startMovingTo(target, 1);
            this.lastMoveTime = MathUtils.tickToSec(this.getWorld().getTime());
        }

        this.cooldown = Math.max(this.cooldown - 1, 0);
        this.attack(target,d);
        this.addEffectsToClosePlayers((ServerWorld) this.getWorld(),this.getPos(),this,MAN_CHASE_DISTANCE);
    }

    public void stalkTick(ServerWorld serverWorld) {
        LivingEntity livingEntity = this.getTarget();

        if (livingEntity == null) {
            return;
        }

        this.getLookControl().lookAt(livingEntity, 30.0f, 30.0f);

        if (MathUtils.tickToSec(this.getWorld().getTime()) - this.lastMoveTime > 0.05) {
            this.getNavigation().startMovingTo(livingEntity, 0.9);
            this.lastMoveTime = MathUtils.tickToSec(this.getWorld().getTime());
        }

        this.chaseIfTooClose(serverWorld);
    }

    public void stareTick(ServerWorld serverWorld) {
        LivingEntity livingEntity = this.getTarget();

        if (livingEntity == null) {
            return;
        }

        if (this.isLookedAt()) {
            if (MathUtils.tickToSec(this.getWorld().getTime()) - this.stareTime > 7.0) {
                this.startChase(serverWorld);
            }
        } else {
            this.stareTime = MathUtils.tickToSec(this.getWorld().getTime());
        }

        this.getLookControl().lookAt(livingEntity, 30.0f, 30.0f);

        this.chaseIfTooClose(serverWorld);
    }

    public void serverTick() {
        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        switch (this.state) {
            case IDLE:
                this.idleTick(serverWorld);
                break;
            case CHASE:
                this.chaseTick(serverWorld);
                break;
            case STARE:
                this.stareTick(serverWorld);
                break;
            case STALK:
                this.stalkTick(serverWorld);
                break;
        }

        this.dataTracker.set(
                MAN_CLIMBING_FLAG,
                this.horizontalCollision && this.getTarget() != null && this.getTarget().getBlockY() > this.getBlockY()
        );
        this.dataTracker.set(MAN_STATE_FLAG,this.state.ordinal());

        this.random2.setSeed(serverWorld.getTime());

        this.setTarget(this.getWorld().getClosestPlayer(this.getX(),this.getY(),this.getZ(),MAN_MAX_SCAN_DISTANCE,true));

        if (this.isSubmergedInWater()) {
            Vec3d oldVelocity = this.getVelocity();
            this.setVelocity(oldVelocity.getX(),0.5,oldVelocity.getZ());
        }

        if (this.isDead()) {
            this.stopSounds();
            return;
        }

        if (MathUtils.tickToSec(this.getWorld().getTime()) - this.lastHallucinationTime > 5) {
            if (this.random2.nextFloat(0f,1f) < 0.2) {
                this.spawnHallucinations();
            }
            this.lastHallucinationTime = MathUtils.tickToSec(this.getWorld().getTime());
        }

        if (this.isClimbing() && this.getTarget() != null) {

            if (this.isOnGround()) {
                this.jump();
            }

            Vec3d toPlayer = this.getTarget().getPos().subtract(this.getPos()).normalize().multiply(0.2);
            setVelocity(new Vec3d(toPlayer.getX(),1,toPlayer.getZ()).multiply(MAN_CLIMB_SPEED));
        }

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

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient) {
            this.chaseSoundInstance.tick();
            this.serverTick();
        }
    }

    // Sounds
    public void playSpottedSound() {
        this.playSound(ModSounds.MAN_SPOT,this.getSoundVolume(),this.getSoundPitch());
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
            return;
        }

        if (MathUtils.distanceTo(this,client.player) <= MAN_CHASE_DISTANCE) {
            if (!client.getSoundManager().isPlaying(this.chaseSoundInstance)) {
                client.getSoundManager().play(this.chaseSoundInstance);
            }
        } else {
            client.getSoundManager().stop(this.chaseSoundInstance);
        }
    }

    /**
     * Stops all sounds being played by The Man
     */
    public void stopSounds() {
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_ATTACK_ID,this.getSoundCategory());
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_CHASE_ID,this.getSoundCategory());
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_IDLECALM_ID,this.getSoundCategory());
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_SPOT_ID,this.getSoundCategory());
    }

    // Attacking and damaging

    protected double getSquaredMaxAttackDistance(LivingEntity entity) {
        return this.getWidth() * 2.0f * (this.getWidth() * 2.0f) + entity.getWidth();
    }

    protected void attack(LivingEntity target, double squaredDistance) {
        double d = this.getSquaredMaxAttackDistance(target);
        if (squaredDistance <= d && this.cooldown <= 0) {
            this.cooldown = MathUtils.toGoalTicks(20);
            this.swingHand(Hand.MAIN_HAND);
            this.tryAttack(target);
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        float damage = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float knockback = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        this.playSound(ModSounds.MAN_ATTACK,this.getSoundVolume(),this.getSoundPitch());
        if (knockback > 0.0f && target instanceof LivingEntity) {
            ((LivingEntity)target).takeKnockback(knockback * 0.5f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
            this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
        }
        if (target instanceof LivingEntity) {
            ((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS,50));
        }
        return target.damage(this.getDamageSources().mobAttack(this), damage);
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
                return e > Math.cos(Math.toRadians(this.targetFOV)) && this.getWorld().raycast(new RaycastContext(new Vec3d(this.getX(), this.getEyeY(), this.getZ()), new Vec3d(player.getX(), player.getEyeY(), player.getZ()), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this)).getType() == HitResult.Type.MISS;
            }
        }
        return false;
    }
}

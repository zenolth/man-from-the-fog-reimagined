package com.zen.fogman.entity.custom;

import com.zen.fogman.ManFromTheFog;
import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.goals.custom.BreakDoorInstantGoal;
import com.zen.fogman.goals.custom.ManChaseGoal;
import com.zen.fogman.goals.custom.ManStalkGoal;
import com.zen.fogman.goals.custom.ManStareGoal;
import com.zen.fogman.item.ModItems;
import com.zen.fogman.other.MathUtils;
import com.zen.fogman.sounds.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
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
import java.util.Objects;
import java.util.Random;

public class TheManEntity extends HostileEntity implements GeoEntity, ClientTickEvents.EndTick {

    public enum ManState {
        IDLE,
        CHASE,
        STARE,
        STALK
    }

    public static final double MAN_SPEED = 0.56;
    public static final double MAN_MAX_SCAN_DISTANCE = 100000.0;
    public static final int MAN_CHASE_DISTANCE = 200;

    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation CLIMB_ANIM = RawAnimation.begin().thenLoop("climb");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public final Random random2 = new Random();
    public long aliveTime;
    private long lastTime;

    private long lastHallucinationTime;

    private int targetFOV = 90;

    public ManState state = ManState.IDLE;

    public EntityTrackingSoundInstance chaseSoundInstance;

    public TheManEntity(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType, world);

        this.lastTime = MathUtils.tickToSec(this.getWorld().getTime());
        this.lastHallucinationTime = MathUtils.tickToSec(this.getWorld().getTime());
        this.aliveTime = this.random2.nextLong(30,60);

        this.updateState(ManState.values()[random2.nextInt(2,3)]);
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        this.chaseSoundInstance = new EntityTrackingSoundInstance(ModSounds.MAN_CHASE,SoundCategory.MASTER,0.6f,1.0f,this,this.getWorld().getTime());
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
        return !serverWorld.getEntitiesByType(ModEntities.THE_MAN,EntityPredicates.VALID_LIVING_ENTITY).isEmpty();
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

    /**
     * Changes The Man's state to one of the ManStates
     * @param newState The new state for The Man
     */
    public void updateState(ManState newState) {
        if (newState == ManState.CHASE) {
            this.playSpottedSound();
            this.doLightning();
        }
        this.state = newState;
        ManFromTheFog.LOGGER.info("The Man is now in {} state", newState);
        this.goalSelector.tick();
    }

    public ManState getState() {
        return this.state;
    }

    public void addEffectsToClosePlayers(ServerWorld world, Vec3d pos, @Nullable Entity entity, int range) {
        StatusEffectInstance darknessInstance = new StatusEffectInstance(StatusEffects.DARKNESS, 260, 0, false, false);
        StatusEffectUtil.addEffectToPlayersWithinDistance(world, entity, pos, range, darknessInstance, 200);
        StatusEffectInstance speedInstance = new StatusEffectInstance(StatusEffects.SPEED, 460, 1, false, false);
        StatusEffectUtil.addEffectToPlayersWithinDistance(world, entity, pos, range, speedInstance, 400);
    }

    @Override
    protected void initGoals() {
        // Goals
        this.goalSelector.add(1, new ManChaseGoal(this, 1.0));
        this.goalSelector.add(1, new ManStalkGoal(this, 0.65));
        this.goalSelector.add(1, new ManStareGoal(this));

        this.goalSelector.add(2, new BreakDoorInstantGoal(this));

        // Targets
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, false));

        //this.targetSelector.add(7, new ActiveTargetGoal<>(this, AnimalEntity.class, false));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
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
    public void doLightning() {
        if (!this.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
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
        if (this.getWorld().isDay()) {
            return false;
        }
        if (!this.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            return TheManEntity.manExist((ServerWorld) this.getWorld());
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
    protected void dropXp() {
        if (this.getWorld().isDay()) {
            return;
        }
        super.dropXp();
    }

    /**
     * Despawns The Man, spawns a lightning and stops all sounds
     */
    public void begone() {
        this.doLightning();
        this.stopSounds();
        this.discard();
    }

    // Other mechanics
    @Override
    public boolean isClimbing() {
        return this.horizontalCollision && this.getTarget() != null && this.getTarget().getBlockY() > this.getBlockY();
    }

    /**
     * Spawns hallucinations randomly around The Man
     */
    public void spawnHallucinations() {
        if (this.getState() != ManState.CHASE) {
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

    @Override
    public void onEndTick(MinecraftClient client) {
        ClientWorld world = client.world;

        if (world == null) {
            return;
        }

        ClientPlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        this.clientTick(client);
    }

    // Ticks
    public void clientTick(MinecraftClient client) {
        if (this.getTarget() != null && this.getTarget() instanceof PlayerEntity) {
            if (Objects.equals(client.getName(), this.getTarget().getEntityName()) && this.targetFOV != client.options.getFov().getValue()) {
                this.targetFOV = MinecraftClient.getInstance().options.getFov().getValue();
            }
        }

        this.playChaseSound(client);
    }
    
    public void serverTick() {
        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        this.random2.setSeed(serverWorld.getTime());

        this.setTarget(this.getWorld().getClosestPlayer(this.getX(),this.getY(),this.getZ(),MAN_MAX_SCAN_DISTANCE,true));

        if (this.getState() == ManState.CHASE && this.getTarget() != null) {
            this.addEffectsToClosePlayers(serverWorld,this.getPos(),this,MAN_CHASE_DISTANCE);
        }

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

        if (this.isClimbing()) {
            Vec3d newVelocity = new Vec3d(0,0.5,0);
            setVelocity(newVelocity);
        }

        if (this.isAlive() && ((aliveTime > 0 && MathUtils.tickToSec(this.getWorld().getTime()) - lastTime > aliveTime) || (this.getTarget() != null && this.getTarget().isDead()))) {
            this.begone();
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

        if (this.getTarget() != null && this.getTarget().isPlayer() && (this.getState() == ManState.STALK || this.getState() == ManState.STARE) && MathUtils.distanceTo(this,this.getTarget()) <= 15) {
            this.updateState(ManState.CHASE);
        }
    }

    @Override
    protected void mobTick() {
        this.chaseSoundInstance.tick();
        //this.clientTick();
        this.serverTick();
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

        ClientPlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        if (MathUtils.distanceTo(this,player) <= MAN_CHASE_DISTANCE && this.getState() == ManState.CHASE) {
            if (!client.getSoundManager().isPlaying(chaseSoundInstance)) {
                client.getSoundManager().play(chaseSoundInstance);
            }
        } else {
            if (client.getSoundManager().isPlaying(chaseSoundInstance)) {
                client.getSoundManager().stop(chaseSoundInstance);
            }
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

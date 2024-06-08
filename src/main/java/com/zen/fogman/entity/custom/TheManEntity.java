package com.zen.fogman.entity.custom;

import com.zen.fogman.ManFromTheFog;
import com.zen.fogman.damage_type.ModDamageTypes;
import com.zen.fogman.goals.ManGoalSelector;
import com.zen.fogman.goals.custom.BreakDoorInstantGoal;
import com.zen.fogman.goals.custom.ManChaseGoal;
import com.zen.fogman.goals.custom.ManStalkGoal;
import com.zen.fogman.goals.custom.ManStareGoal;
import com.zen.fogman.item.ModItems;
import com.zen.fogman.other.MathUtils;
import com.zen.fogman.sounds.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.*;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
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
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
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
import java.util.List;
import java.util.Random;

public class TheManEntity extends HostileEntity implements GeoEntity {

    public static final double MAN_SPEED = 0.7;

    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation CLIMB_ANIM = RawAnimation.begin().thenLoop("climb");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private boolean didTarget = false;

    public final Random timeRandom = new Random();
    private long aliveTime;
    private long lastTime;

    private int targetFOV = 90;

    private int ticksUntilChaseSound = 0;

    public ManState state = ManState.STALK;

    public TheManEntity(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType, world);
    }

    public void onSpawn() {
        this.doLightning();
        this.setLastTime(MathUtils.tickToSec(getWorld().getTime()));
        this.setAliveTime(this.timeRandom.nextLong(30,60));

        switch(random.nextBetween(0,2)) {
            case 0:
                this.updateState(ManState.STARE);
            case 1:
                this.updateState(ManState.STALK);
            case 2:
                this.updateState(ManState.STALK);
        }
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        if (packet.getEntityData() == 0) {
            this.onSpawn();
        }
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        ItemStack itemStack = new ItemStack(Items.LEATHER_BOOTS,1);
        itemStack.addEnchantment(Enchantments.DEPTH_STRIDER,3);
        DyeableArmorItem item = (DyeableArmorItem) itemStack.getItem();


        return Collections.singleton(itemStack);
    }

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

    public static void addEffectsToClosePlayers(ServerWorld world, Vec3d pos, @Nullable Entity entity, int range) {
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
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE,300000)
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
        if (isClimbing()) {
            return PlayState.STOP;
        }
        if (!event.isMoving()) {
            return event.setAndContinue(IDLE_ANIM);
        }
        return PlayState.STOP;
    }

    protected <E extends TheManEntity> PlayState runAnimController(final AnimationState<E> event) {
        if (isClimbing()) {
            return PlayState.STOP;
        }
        if (event.isMoving() && !isClimbing()) {
            return event.setAndContinue(RUN_ANIM);
        }
        return PlayState.STOP;
    }

    protected <E extends TheManEntity> PlayState climbAnimController(final AnimationState<E> event) {
        if (isClimbing()) {
            return event.setAndContinue(CLIMB_ANIM);
        }
        return PlayState.STOP;
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this,"Standing",5,this::idleAnimController));
        controllers.add(new AnimationController<>(this,"Running",5,this::runAnimController));
        controllers.add(new AnimationController<>(this,"Climbing",5,this::climbAnimController));
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
        this.stopSounds();
        this.discard();
    }

    @Override
    public boolean isClimbing() {
        return this.horizontalCollision && getTarget() != null && getTarget().getBlockY() > getBlockY();
    }

    public void playChaseSound() {
        if (this.isDead()) {
            return;
        }
        if (getTarget() != null && getState() == ManState.CHASE) {
            if (this.ticksUntilChaseSound <= 0) {
                this.playSound(ModSounds.MAN_CHASE,4.0f,1.0f);
                this.ticksUntilChaseSound = 2740;
            }
            this.ticksUntilChaseSound--;
        } else {
            MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_CHASE_ID,this.getSoundCategory());
            this.ticksUntilChaseSound = 0;
        }
    }

    public void stopSounds() {
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_ATTACK_ID,this.getSoundCategory());
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_CHASE_ID,this.getSoundCategory());
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_IDLECALM_ID,this.getSoundCategory());
        MinecraftClient.getInstance().getSoundManager().stopSounds(ModSounds.MAN_SPOT_ID,this.getSoundCategory());
    }

    public void clientTick() {
        if (getTarget() != null && getTarget() instanceof PlayerEntity) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getName() == getTarget().getEntityName() && this.targetFOV != client.options.getFov().getValue()) {
                this.targetFOV = MinecraftClient.getInstance().options.getFov().getValue();
            }
        }
    }

    public void serverTick() {

        if (this.isDead()) {
            this.stopSounds();
            return;
        }

        playChaseSound();

        if (isClimbing()) {
            //Vec3d oldVelocity = getVelocity();
            Vec3d newVelocity = new Vec3d(0,0.5,0);
            setVelocity(newVelocity);
        }

        if (isAlive() && ((aliveTime > 0 && MathUtils.tickToSec(getWorld().getTime()) - lastTime > aliveTime && !isLookedAt()) || (getTarget() != null && getTarget().isDead()))) {
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

        if (getTarget() != null && getTarget().isPlayer() && (getState() == ManState.STALK || getState() == ManState.STARE) && MathUtils.distanceTo(this,getTarget()) <= 15) {
            updateState(ManState.CHASE);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient()) {
            this.clientTick();
        } else {
            this.serverTick();
        }
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (getWorld().isClient()) {
            return;
        }
        addEffectsToClosePlayers((ServerWorld) getWorld(),this.getPos(),this,20);
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

    /*@Override
    public boolean tryAttack(Entity target) {
        this.playSound(ModSounds.MAN_ATTACK,this.getSoundVolume(),this.getSoundPitch());
        return super.tryAttack(target);
    }*/

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

    public boolean isLookedAt() {
        if (getTarget() == null) {
            return false;
        }
        if (getTarget() instanceof PlayerEntity player) {
            if (!getWorld().isClient()) {
                
                Vec3d lookVector = player.getRotationVec(1.0f).normalize();
                Vec3d direction = new Vec3d(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
                double e = lookVector.dotProduct(direction.normalize());
                return e > Math.cos(Math.toRadians(this.targetFOV));
            }
        }
        return false;
    }
}

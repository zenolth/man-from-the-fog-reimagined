package com.zen.fogman.entity.custom;

import com.zen.fogman.ManFromTheFog;
import com.zen.fogman.goals.custom.BreakDoorInstantGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TheManEntity extends HostileEntity implements GeoEntity {

    public static final double MAN_SPEED = 0.6;
    public static final double MAN_CLIMB_SPEED = 0.6;

    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.man.idle");
    protected static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("animation.man.crawlrun");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public TheManEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        SpiderNavigation nav = new SpiderNavigation(this,world);
        nav.setCanEnterOpenDoors(true);
        nav.setCanWalkOverFences(true);
        nav.setCanEnterOpenDoors(true);
        nav.setCanSwim(true);
        nav.setSpeed(MAN_SPEED * 2);
        return nav;
    }

    public boolean canBreakDoors() {
        return true;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(1, new BreakDoorInstantGoal(this));
        this.goalSelector.add(2, new WanderNearTargetGoal(this, 0.7, 100.0f));
        this.goalSelector.add(3, new AvoidSunlightGoal(this));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(3, new MoveThroughVillageGoal(this, 1.0, false, 4, this::canBreakDoors));
        this.goalSelector.add(4, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, GolemEntity.class, false));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PatrolEntity.class, false));
        this.targetSelector.add(7, new ActiveTargetGoal<>(this, AnimalEntity.class, false));
        this.targetSelector.add(7, new ActiveTargetGoal<>(this, ZombieEntity.class, false));
        this.targetSelector.add(7, new ActiveTargetGoal<>(this, AbstractSkeletonEntity.class, false));
    }

    public static DefaultAttributeContainer.Builder createManAttributes() {
        return TheManEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH,350)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,MAN_SPEED)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,3)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK,2)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,0.9)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE,30)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,100)
                .add(EntityAttributes.GENERIC_ARMOR,7)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,5);
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this,"Standing",5,this::idleAnimController));
        controllers.add(new AnimationController<>(this,"Running",5,this::runAnimController));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            LivingEntity target = getTarget();
            ManFromTheFog.LOGGER.info(String.valueOf(this.lookDirection));
            if (this.horizontalCollision && target != null && target.getBlockY() > getBlockY()) {
                //Vec3d lookDir = new Vec3d(lookControl.getLookX(),lookControl.getLookY(),lookControl.getLookZ()).normalize();
                Vector3f moveDir = this.getMovementDirection().getUnitVector().normalize();
                Vec3d newVelocity;
                if (this.verticalCollision) {
                    newVelocity = new Vec3d(0,MAN_CLIMB_SPEED * 2,-moveDir.z);
                } else {
                    newVelocity = new Vec3d(0,MAN_CLIMB_SPEED,0);
                }
                setVelocity(newVelocity);
            }
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
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}

package dev.zenolth.the_fog.common.entity.the_man;

import dev.zenolth.the_fog.common.entity.ModEntities;
import dev.zenolth.the_fog.common.particles.ModParticles;
import dev.zenolth.the_fog.common.status_effect.TheManStatusEffects;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TheManSpitEntity extends ProjectileEntity {
    public static final int AMOUNT_PER_SPIT = 8;

    public TheManSpitEntity(EntityType<? extends TheManSpitEntity> entityType, World world) {
        super(entityType, world);
    }

    public TheManSpitEntity(World world,TheManEntity owner) {
        super(ModEntities.THE_MAN_SPIT, world);
        this.setOwner(owner);
        this.setPosition(
                owner.getX() - (double)(owner.getWidth() + 1.0F) * 0.5 * (double) MathHelper.sin(owner.bodyYaw * (float) (Math.PI / 180.0)),
                owner.getEyeY() - 0.1F,
                owner.getZ() + (double)(owner.getWidth() + 1.0F) * 0.5 * (double)MathHelper.cos(owner.bodyYaw * (float) (Math.PI / 180.0))
        );
    }

    public TheManSpitEntity(World world,double x, double y, double z) {
        super(ModEntities.THE_MAN_SPIT, world);
        this.setPosition(x,y,z);
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        double d = packet.getVelocityX();
        double e = packet.getVelocityY();
        double f = packet.getVelocityZ();

        for(int i = 0; i < Math.round(32f / AMOUNT_PER_SPIT); ++i) {
            double g = 0.4 + 0.1 * (double)i;
            this.getWorld().addParticle(ModParticles.THE_MAN_SPIT_PARTICLE, this.getX(), this.getY(), this.getZ(), d * g, e, f * g);
        }

        this.setVelocity(d, e, f);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity owner = this.getOwner();

        if (owner == null) {
            return;
        }

        if (owner instanceof LivingEntity theMan && entityHitResult.getEntity() instanceof PlayerEntity target) {
            target.damage(this.getDamageSources().mobProjectile(this,theMan), (float) theMan.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) / 2);
            target.addStatusEffect(TheManStatusEffects.POISON,this);
            target.addStatusEffect(TheManStatusEffects.SLOWNESS,this);

            var vehicle = target.getVehicle();
            if (vehicle != null) {
                if (!vehicle.isLiving()) {
                    vehicle.kill();
                }
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient) {
            this.discard();
        }
    }

    @Override
    public boolean collidesWith(Entity other) {
        return super.collidesWith(other) && other.isLiving();
    }

    @Override
    public boolean collidesWithStateAtPos(BlockPos pos, BlockState state) {
        return super.collidesWithStateAtPos(pos, state) && state.isFullCube(this.getWorld(),pos);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3d velocity = this.getVelocity();
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.onCollision(hitResult);
        double x = this.getX() + velocity.x;
        double y = this.getY() + velocity.y;
        double z = this.getZ() + velocity.z;
        this.updateRotation();
        if (this.getWorld().getStatesInBox(this.getBoundingBox()).noneMatch(AbstractBlock.AbstractBlockState::isAir)) {
            this.discard();
        } else if (this.isInsideWaterOrBubbleColumn()) {
            this.discard();
        } else {
            this.setVelocity(velocity.multiply(0.99F));
            if (!this.hasNoGravity()) {
                this.setVelocity(this.getVelocity().add(0.0, -0.06F, 0.0));
            }
            this.setPosition(x,y,z);
        }
    }
}

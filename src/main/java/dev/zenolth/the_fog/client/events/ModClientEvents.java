package dev.zenolth.the_fog.client.events;

import dev.zenolth.the_fog.client.sound.DynamicSoundInstance;
import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.entity.MonitorPlayerLineOfSight;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.predicate.TheManPredicates;
import dev.zenolth.the_fog.common.state_machine.states.TheManState;
import dev.zenolth.the_fog.common.util.GeometryHelper;
import dev.zenolth.the_fog.common.sounds.ModSounds;
import dev.zenolth.the_fog.common.util.WorldHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.sound.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockStateRaycastContext;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ModClientEvents implements ClientTickEvents.EndTick {

    public static final int LOS_CHECK_TICKS = 5;
    public static final int MAX_LOS_CHECK_DISTANCE = TheManEntity.CHASE_DISTANCE * 4;

    private static ModClientEvents INSTANCE;

    public static float CHASE_VOLUME = 1.4f;
    public static float CHASE_FADE_OUT_SPEED = 0.07f;

    public static float NIGHT_AMBIENCE_VOLUME = 0.15f;
    public static float CAVE_AMBIENCE_VOLUME = 0.15f;

    public static float FADE_IN_SPEED = 0.1f;
    public static float FADE_OUT_SPEED = 0.1f;

    private boolean thickFog = false;

    public DynamicSoundInstance chaseTheme;
    public PositionedSoundInstance horrorSound;
    public DynamicSoundInstance nightAmbience;
    public DynamicSoundInstance caveAmbience;

    private boolean isChased = false;
    private boolean didChase = false;

    private long losCheckTicks = LOS_CHECK_TICKS;

    private ModClientEvents() {
        this.chaseTheme = DynamicSoundInstance.loop(ModSounds.MAN_CHASE,0f, 1f);
        this.horrorSound = PositionedSoundInstance.master(ModSounds.HORROR,1f,0.8f);
        this.nightAmbience = DynamicSoundInstance.master(ModSounds.NIGHT_AMBIENCE,0f,1f);
        this.caveAmbience = DynamicSoundInstance.loop(ModSounds.CAVE_AMBIENCE,0f,1f);
    }

    public static ModClientEvents getInstance() {
        if (INSTANCE == null) INSTANCE = new ModClientEvents();
        return INSTANCE;
    }

    public boolean hasThickFog() {
        return this.thickFog;
    }

    public void setThickFog(boolean thickFog) {
        this.thickFog = thickFog;
    }

    public void stopSounds(SoundManager soundManager) {
        if (soundManager.isPlaying(this.nightAmbience)) {
            soundManager.stop(this.nightAmbience);
        }
        if (soundManager.isPlaying(this.caveAmbience)) {
            soundManager.stop(this.caveAmbience);
        }
        if (soundManager.isPlaying(this.chaseTheme)) {
            soundManager.stop(this.chaseTheme);
        }
    }

    public void handleEntity(MinecraftClient client, LivingEntity entity) {
        if (entity instanceof TheManEntity theMan && !theMan.isReal()) return;
        if (!(entity instanceof MonitorPlayerLineOfSight los)) return;

        if (client.world == null || client.player == null) {
            this.isChased = false;
            return;
        }

        if (client.player.isCreative() || !client.player.getPos().isInRange(entity.getPos(),MAX_LOS_CHECK_DISTANCE)) {
            los.setPlayerLOS(false);
            return;
        }

        var camera = client.gameRenderer.getCamera();
        //var cameraEntity = client.getCameraEntity();

        var cameraLookVector = GeometryHelper.calculateDirection(camera.getPitch(),camera.getYaw()).normalize();

        BlockHitResult result = client.world.raycast(
                new BlockStateRaycastContext(
                        new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ()),
                        camera.getPos(),
                        TheManPredicates.LOOK_BLOCK_STATE_PREDICATE
                )
        );

        if (result.getType() != HitResult.Type.MISS) {
            los.setPlayerLOS(false);
        } else {
            float fov = client.options.getFov().getValue() * client.player.getFovMultiplier();

            Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(fov / 2f);
            Matrix4f viewMatrix = new Matrix4f();
            viewMatrix = viewMatrix.lookAt(
                    camera.getPos().toVector3f(),
                    camera.getPos().toVector3f().add(cameraLookVector.toVector3f()),
                    cameraLookVector.rotateX((float) Math.toRadians(90)).toVector3f()
            );

            Frustum frustum = new Frustum(viewMatrix,projectionMatrix);
            var isLooking = frustum.isVisible(TheManEntity.HITBOX_SIZE.getBoxAt(entity.getPos()));

            /*if (isLooking && theMan.getState() == TheManState.STARE) {
                var rot = Util.getRotationBetween(camera.getPos(),theMan.getEyePos());
                if (cameraEntity != null) {
                    cameraEntity.setPitch(rot.getLeft());
                    cameraEntity.setYaw(rot.getRight());
                }
            }*/

            los.setPlayerLOS(isLooking);
        }
    }

    private void checkLineOfSight(MinecraftClient client) {
        if (client.world == null || client.player == null) return;
        var entities = client.world.getOtherEntities(
                client.player,
                Box.of(client.player.getPos(),MAX_LOS_CHECK_DISTANCE,MAX_LOS_CHECK_DISTANCE,MAX_LOS_CHECK_DISTANCE),
                (entity) -> entity instanceof MonitorPlayerLineOfSight && entity.isLiving()
        );
        entities.forEach(entity -> this.handleEntity(client,(LivingEntity) entity));
    }

    public void tick(MinecraftClient client) {
        var soundManager = client.getSoundManager();

        if (client.world == null) {
            this.stopSounds(soundManager);
            return;
        }

        if (client.player == null) {
            return;
        }

        if (TheManEntity.isInAllowedDimension(client.world) && !this.isChased) {
            if (WorldHelper.isOnSurface(client.world, client.player)) {
                if (WorldHelper.isNight(client.world)) {
                    this.nightAmbience.setVolume(GeometryHelper.interpolate(this.nightAmbience.getVolume(),NIGHT_AMBIENCE_VOLUME,FADE_IN_SPEED));
                } else {
                    this.nightAmbience.setVolume(GeometryHelper.interpolate(this.nightAmbience.getVolume(),0f,FADE_OUT_SPEED));
                }

                this.caveAmbience.setVolume(GeometryHelper.interpolate(this.caveAmbience.getVolume(),0f,FADE_OUT_SPEED));
            } else {
                this.nightAmbience.setVolume(GeometryHelper.interpolate(this.nightAmbience.getVolume(),0f,FADE_OUT_SPEED));

                this.caveAmbience.setVolume(GeometryHelper.interpolate(this.caveAmbience.getVolume(),CAVE_AMBIENCE_VOLUME,FADE_IN_SPEED));
            }
        } else {
            this.nightAmbience.setVolume(0f);
            this.caveAmbience.setVolume(0f);
        }

        if (this.chaseTheme.getVolume() <= 0 && soundManager.isPlaying(this.chaseTheme)) {
            soundManager.stop(this.chaseTheme);
            this.chaseTheme.finish();
        }
        if (this.chaseTheme.getVolume() > 0 && !soundManager.isPlaying(this.chaseTheme)) {
            this.chaseTheme.reset();
            soundManager.play(this.chaseTheme);
        }

        if (this.nightAmbience.getVolume() <= 0 && soundManager.isPlaying(this.nightAmbience)) {
            soundManager.stop(this.nightAmbience);
            this.nightAmbience.finish();
        }
        if (this.nightAmbience.getVolume() > 0 && !soundManager.isPlaying(this.nightAmbience)) {
            this.nightAmbience.reset();
            soundManager.play(this.nightAmbience);
        }

        if (this.caveAmbience.getVolume() <= 0 && soundManager.isPlaying(this.caveAmbience)) {
            soundManager.stop(this.caveAmbience);
            this.caveAmbience.finish();
        }
        if (this.caveAmbience.getVolume() > 0 && !soundManager.isPlaying(this.caveAmbience)) {
            this.caveAmbience.reset();
            soundManager.play(this.caveAmbience);
        }

        if (--this.losCheckTicks <= 0L) {
            this.checkLineOfSight(client);
            this.losCheckTicks = LOS_CHECK_TICKS;
        }

        var theMan = FogMod.getTheMan(client.world);

        if (theMan != null) {
            this.isChased = theMan.getState() == TheManState.CHASE && theMan.isInRange(client.player, TheManEntity.CHASE_DISTANCE);

            if (this.isChased) {
                client.player.the_fog_is_coming$setGlitchMultiplier(Math.max(0f,Math.min(1f,1f - (client.player.distanceTo(theMan) / 20f))));
            }
        } else {
            this.isChased = false;
        }

        if (this.isChased) {
            if (!this.didChase && !soundManager.isPlaying(this.horrorSound)) {
                this.didChase = true;
                soundManager.play(this.horrorSound);
            }

            this.chaseTheme.setVolume(CHASE_VOLUME);
        } else {
            if (this.didChase) {
                this.didChase = false;
            }
            client.player.the_fog_is_coming$setGlitchMultiplier(0f);
            this.chaseTheme.setVolume(GeometryHelper.interpolate(this.chaseTheme.getVolume(),0f,CHASE_FADE_OUT_SPEED));
        }
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        this.tick(client);
    }
}

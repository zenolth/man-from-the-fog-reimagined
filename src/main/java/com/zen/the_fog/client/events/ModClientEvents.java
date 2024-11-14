package com.zen.the_fog.client.events;

import com.zen.the_fog.client.mixin_interfaces.ClientPlayerEntityInterface;
import com.zen.the_fog.common.ManFromTheFog;
import com.zen.the_fog.common.entity.ModEntities;
import com.zen.the_fog.common.entity.the_man.TheManEntity;
import com.zen.the_fog.common.entity.the_man.TheManPackets;
import com.zen.the_fog.common.entity.the_man.TheManPredicates;
import com.zen.the_fog.common.entity.the_man.TheManState;
import com.zen.the_fog.common.other.Util;
import com.zen.the_fog.common.sounds.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.sound.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockStateRaycastContext;
import org.joml.Matrix4f;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ModClientEvents implements ClientTickEvents.EndTick {

    public static final double MAN_DETECT_RANGE = 1024;

    public static Identifier VIGNETTE_TEXTURE = new Identifier(ManFromTheFog.MOD_ID,"textures/effects/white_vignette.png");

    public PositionedSoundInstance chaseTheme;
    public PositionedSoundInstance horrorSound;
    public PositionedSoundInstance nightAmbience;

    private boolean isChased = false;
    private boolean didChase = false;

    private long lookTick = 2;

    public static PositionedSoundInstance createMusicLoop(SoundEvent sound, float pitch, float volume) {
        return new PositionedSoundInstance(sound.getId(), SoundCategory.MASTER, volume, pitch, SoundInstance.createRandom(), true, 0, SoundInstance.AttenuationType.NONE, 0.0, 0.0, 0.0, true);
    }

    public ModClientEvents() {
        this.chaseTheme = createMusicLoop(ModSounds.MAN_CHASE,1f, 1.4f);
        this.horrorSound = PositionedSoundInstance.master(ModSounds.HORROR,1f,0.8f);
        this.nightAmbience = PositionedSoundInstance.master(ModSounds.NIGHT_AMBIENCE,1f,0.15f);
    }

    public void stopSounds(SoundManager soundManager) {
        if (soundManager.isPlaying(this.nightAmbience)) {
            soundManager.stop(this.nightAmbience);
        }
        if (soundManager.isPlaying(this.chaseTheme)) {
            soundManager.stop(this.chaseTheme);
        }
    }

    public void cameraTick(MinecraftClient client, TheManEntity theMan) {
        if (--this.lookTick > 0L) {
            return;
        }

        this.lookTick = 2;

        if (client.world == null) {
            this.isChased = false;
            return;
        }

        if (client.player == null) {
            this.isChased = false;
            return;
        }

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraLookVector = Util.getRotationVector(camera.getPitch(),camera.getYaw()).normalize();

        BlockHitResult result = client.world.raycast(
                new BlockStateRaycastContext(
                        new Vec3d(theMan.getX(), theMan.getEyeY(), theMan.getZ()),
                        camera.getPos(),
                        TheManPredicates.BLOCK_STATE_PREDICATE
                )
        );

        if (result.getType() != HitResult.Type.MISS) {
            theMan.updatePlayerLookedAt(false);
        } else {
            float fov = client.options.getFov().getValue() * client.player.getFovMultiplier();

            Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(fov);
            Matrix4f viewMatrix = new Matrix4f();
            viewMatrix = viewMatrix.lookAt(
                    camera.getPos().toVector3f(),
                    camera.getPos().toVector3f().add(cameraLookVector.toVector3f()),
                    cameraLookVector.rotateX((float) Math.toRadians(90)).toVector3f()
            );

            Frustum frustum = new Frustum(viewMatrix,projectionMatrix);

            theMan.updatePlayerLookedAt(frustum.isVisible(TheManEntity.HITBOX_SIZE.getBoxAt(theMan.getPos())));
        }
    }

    public void tick(MinecraftClient client) {
        SoundManager soundManager = client.getSoundManager();

        if (client.world == null) {
            this.stopSounds(soundManager);
            return;
        }

        if (client.player == null) {
            return;
        }

        if (Util.isNight(client.world) && !soundManager.isPlaying(this.nightAmbience) && !soundManager.isPlaying(this.chaseTheme) && TheManEntity.isInAllowedDimension(client.world)) {
            soundManager.play(this.nightAmbience);
        }

        List<TheManEntity> theManEntities = client.world.getEntitiesByType(
                ModEntities.THE_MAN,
                Box.of(
                        client.player.getPos(),
                        MAN_DETECT_RANGE,
                        MAN_DETECT_RANGE,
                        MAN_DETECT_RANGE
                ),
                TheManPredicates.VALID_MAN
        );

        if (!theManEntities.isEmpty()) {

            TheManEntity theMan = theManEntities.get(0);

            this.isChased = theMan.getState() == TheManState.CHASE && theMan.isInRange(client.player, TheManEntity.MAN_CHASE_DISTANCE);

            if (this.isChased) {
                client.player.the_fog_is_coming$setGlitchMultiplier(Math.max(0f,Math.min(1f,1f - (client.player.distanceTo(theMan) / 20f))));
            }

            this.cameraTick(client,theMan);
        } else {
            this.isChased = false;
        }

        if (this.isChased) {
            if (!this.didChase && !soundManager.isPlaying(this.horrorSound)) {
                this.didChase = true;
                soundManager.play(this.horrorSound);
            }

            if (!soundManager.isPlaying(this.chaseTheme)) {
                soundManager.play(this.chaseTheme);
            }
        } else {
            if (this.didChase) {
                this.didChase = false;
            }
            client.player.the_fog_is_coming$setGlitchMultiplier(0f);
            if (soundManager.isPlaying(this.chaseTheme)) {
                soundManager.stop(this.chaseTheme);
            }
        }
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        this.tick(client);
    }
}

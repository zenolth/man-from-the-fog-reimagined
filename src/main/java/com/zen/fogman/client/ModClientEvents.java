package com.zen.fogman.client;

import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.entity.the_man.TheManEntity;
import com.zen.fogman.mixininterfaces.AbstractClientPlayerInterface;
import com.zen.fogman.sounds.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ModClientEvents implements ClientTickEvents.EndTick {

    public static int NIGHT_TICK = 13000;

    public PositionedSoundInstance nightAmbience;

    public ModClientEvents() {
        this.nightAmbience = PositionedSoundInstance.master(ModSounds.NIGHT_AMBIENCE,1f,0.15f);
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        SoundManager soundManager = client.getSoundManager();

        if (client.world == null) {
            if (soundManager.isPlaying(this.nightAmbience)) {
                soundManager.stop(this.nightAmbience);
            }
            return;
        }

        if (client.player == null) {
            return;
        }

        if (client.world.getTimeOfDay() >= NIGHT_TICK && !soundManager.isPlaying(this.nightAmbience)) {
            soundManager.play(this.nightAmbience);
        }

        List<TheManEntity> theManEntities = client.world.getEntitiesByType(
                ModEntities.THE_MAN,
                Box.of(
                        client.player.getPos(),
                        TheManEntity.MAN_CHASE_DISTANCE * 1.5,
                        TheManEntity.MAN_CHASE_DISTANCE * 1.5,
                        TheManEntity.MAN_CHASE_DISTANCE * 1.5
                ),
                EntityPredicates.VALID_ENTITY
        );

        if (!theManEntities.isEmpty()) {
            theManEntities.forEach(theManEntity -> {
                theManEntity.clientTick(client);
            });
        } else {
            // Reset the fov when the man is not present
            if (((AbstractClientPlayerInterface) client.player).getFovModifier() != 1.0) {
                ((AbstractClientPlayerInterface) client.player).setFovModifier(1.0f);
            }
        }
    }
}

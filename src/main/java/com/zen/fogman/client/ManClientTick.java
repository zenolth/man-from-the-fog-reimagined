package com.zen.fogman.client;

import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.entity.the_man.TheManEntity;
import com.zen.fogman.mixininterfaces.AbstractClientPlayerInterface;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;

import java.util.List;

public class ManClientTick implements ClientTickEvents.EndTick {
    @Override
    public void onEndTick(MinecraftClient client) {
        if (client.world == null) {
            return;
        }

        if (client.player == null) {
            return;
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

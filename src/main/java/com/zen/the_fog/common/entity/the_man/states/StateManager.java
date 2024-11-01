package com.zen.the_fog.common.entity.the_man.states;

import com.zen.the_fog.common.entity.the_man.TheManEntity;
import com.zen.the_fog.common.entity.the_man.TheManState;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;

public class StateManager {

    private final TheManEntity mob;
    private final HashMap<TheManState,AbstractState> states = new HashMap<>();

    public StateManager(TheManEntity mob) {
        this.mob = mob;
    }

    public void add(TheManState state,AbstractState object) {
        this.states.put(state,object);
    }

    public void tick(ServerWorld serverWorld) {
        if (this.states.containsKey(this.mob.getState())) {
            this.states.get(this.mob.getState()).tick(serverWorld);
        }
    }
}

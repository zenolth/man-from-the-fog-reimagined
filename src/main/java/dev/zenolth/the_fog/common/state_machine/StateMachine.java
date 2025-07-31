package dev.zenolth.the_fog.common.state_machine;

import dev.zenolth.the_fog.common.state_machine.states.AbstractState;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;

public class StateMachine<T extends StateMachineEntity<E>,E extends Enum<E>> {

    private final T mob;
    private final HashMap<E, AbstractState<T,E>> states = new HashMap<>();

    public StateMachine(T mob) {
        this.mob = mob;
    }

    public void add(E state,AbstractState<T,E> object) {
        this.states.put(state,object);
    }

    public void start() {
        if (this.states.containsKey(this.mob.getState())) {
            this.states.get(this.mob.getState()).start();
        }
    }

    public void tick(ServerWorld serverWorld) {
        if (this.states.containsKey(this.mob.getState())) {
            this.states.get(this.mob.getState()).tick(serverWorld);
        }
    }
}

package dev.zenolth.the_fog.common.state_machine.states;

import dev.zenolth.the_fog.common.state_machine.StateMachineEntity;
import net.minecraft.server.world.ServerWorld;

public abstract class AbstractState<T extends StateMachineEntity<E>,E extends Enum<E>> {
    public T mob;

    public AbstractState(T entity) {
        this.mob = entity;
    }

    public void start() {

    }

    public void tick(ServerWorld serverWorld) {

    }
}

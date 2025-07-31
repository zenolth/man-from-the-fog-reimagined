package dev.zenolth.the_fog.common.state_machine.states.mimic;

import dev.zenolth.the_fog.common.entity.mimic.MimicEntity;
import dev.zenolth.the_fog.common.state_machine.states.AbstractState;
import dev.zenolth.the_fog.common.state_machine.states.MimicState;

public class PrepareForRevealState extends AbstractState<MimicEntity, MimicState> {
    public PrepareForRevealState(MimicEntity entity) {
        super(entity);
    }
}

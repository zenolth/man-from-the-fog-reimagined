package dev.zenolth.the_fog.common.goals;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.util.profiler.Profiler;

import java.util.function.Supplier;

public class ManGoalSelector extends GoalSelector {
    public ManGoalSelector(Supplier<Profiler> profiler) {
        super(profiler);
    }

    @Override
    public void tick() {
        this.tickGoals(true);
    }
}

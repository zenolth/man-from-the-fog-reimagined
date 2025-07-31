package dev.zenolth.the_fog.common.state_machine;

public interface StateMachineEntity<T extends Enum<T>> {
    T getState();
    void setState(T state);
    StateMachine<? extends StateMachineEntity<T>,T> getStateMachine();
}

package dev.zenolth.the_fog.common.mixin_interfaces;

public interface LookingAtManInterface {
    default boolean the_fog_is_coming$isLookingAtMan() {
        return false;
    }

    default void the_fog_is_coming$setLookingAtMan(boolean lookingAtMan) {

    }
}

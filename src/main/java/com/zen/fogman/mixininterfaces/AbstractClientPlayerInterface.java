package com.zen.fogman.mixininterfaces;

public interface AbstractClientPlayerInterface {
    default void setFovModifier(float fovMod) {

    }

    default float getFovModifier() {
        return 1.0f;
    }
}

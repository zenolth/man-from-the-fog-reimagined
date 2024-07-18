package com.zen.fogman.common.entity.the_man;

import software.bernie.geckolib.core.animation.RawAnimation;

public class TheManAnimations {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    public static final RawAnimation CLIMB = RawAnimation.begin().thenLoop("climb");
}

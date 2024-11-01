package com.zen.the_fog.common.entity.the_man;

import com.zen.the_fog.common.ManFromTheFog;
import net.minecraft.util.Identifier;

public class TheManPackets {
    public static final Identifier LOOKED_AT_PACKET_ID = new Identifier(ManFromTheFog.MOD_ID,"man_looked_at_packet");
    public static final Identifier REMOVE_PLAYER_FROM_MAP_PACKET_ID = new Identifier(ManFromTheFog.MOD_ID,"man_remove_player_from_map_packet");
    public static final Identifier UPDATE_FOG_DENSITY = new Identifier(ManFromTheFog.MOD_ID,"update_fog_density");
}

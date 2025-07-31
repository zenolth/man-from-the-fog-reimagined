package dev.zenolth.the_fog.common.block;

import dev.zenolth.the_fog.common.FogMod;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntityTypes {
    public static final BlockEntityType<ErebusLanternBlockEntity> EREBUS_LANTERN = register(
            "erebus_lantern",
            FabricBlockEntityTypeBuilder.create(ErebusLanternBlockEntity::new,ModBlocks.EREBUS_LANTERN).build()
    );

    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, FogMod.id(path),blockEntityType);
    }

    public static void initialize() {

    }
}

package dev.zenolth.the_fog.common.block;

import dev.zenolth.the_fog.common.util.RandomNum;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ErebusLanternBlock extends LanternBlock implements BlockEntityProvider {
    public static final IntProperty POWER = IntProperty.of("power",0,15);

    public ErebusLanternBlock() {
        super(FabricBlockSettings.create()
                .mapColor(MapColor.IRON_GRAY)
                .solid()
                .requiresTool()
                .strength(3.5f)
                .hardness(1.5f)
                .sounds(BlockSoundGroup.LANTERN)
                .luminance(state -> state.get(POWER))
                .nonOpaque()
                .pistonBehavior(PistonBehavior.DESTROY)
        );

        this.setDefaultState(this.getDefaultState().with(POWER,0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWER);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (state.get(POWER) >= 15) return ActionResult.FAIL;

        var stack = player.getStackInHand(hand);
        var count = stack.getCount();

        if (count <= 0 || stack.streamTags().noneMatch((t) -> t.equals(ItemTags.COALS))) return ActionResult.FAIL;

        stack.decrement(1);
        world.setBlockState(pos,state.with(POWER,state.get(POWER) + 1));
        world.playSound(player,pos, SoundEvents.BLOCK_LANTERN_HIT, SoundCategory.BLOCKS, 1.0F, RandomNum.next(0.9f,1.1f));

        return ActionResult.success(world.isClient);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ErebusLanternBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockWithEntity.checkType(type,ModBlockEntityTypes.EREBUS_LANTERN, ErebusLanternBlockEntity::tick);
    }
}

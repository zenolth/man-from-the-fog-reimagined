package dev.zenolth.the_fog.common.block;

import dev.zenolth.the_fog.common.util.RandomNum;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ErebusLanternBlockEntity extends BlockEntity {
    public static final int BURNOUT_TICKS = 6000;
    public static final String BURNOUT_TICKS_NBT_KEY = "BurnoutTicks";

    private long burnoutTicks = BURNOUT_TICKS;

    public ErebusLanternBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.EREBUS_LANTERN, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(BURNOUT_TICKS_NBT_KEY, NbtElement.LONG_TYPE)) {
            this.burnoutTicks = nbt.getLong(BURNOUT_TICKS_NBT_KEY);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong(BURNOUT_TICKS_NBT_KEY,this.burnoutTicks);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ErebusLanternBlockEntity blockEntity) {
        if (world.isClient()) return;
        var power = state.get(ErebusLanternBlock.POWER);
        if (power <= 0) return;

        if (--blockEntity.burnoutTicks <= 0L) {
            if (RandomNum.nextFloat() < 0.7) {
                world.setBlockState(pos,state.with(ErebusLanternBlock.POWER,power - 1));
                world.playSound(null,pos, SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            blockEntity.burnoutTicks = BURNOUT_TICKS;
        }
    }
}
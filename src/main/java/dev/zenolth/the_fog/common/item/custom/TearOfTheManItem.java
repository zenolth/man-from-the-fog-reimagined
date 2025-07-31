package dev.zenolth.the_fog.common.item.custom;

import dev.zenolth.the_fog.common.block.ModBlocks;
import dev.zenolth.the_fog.common.util.TimeHelper;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class TearOfTheManItem extends ToolItem {

    private boolean hasEffects = false;

    public TearOfTheManItem(Settings settings) {
        super(ToolMaterials.NETHERITE, settings);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (!blockState.isAir() && blockState.isOf(Blocks.CRYING_OBSIDIAN)) {
            if (player instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, blockPos, context.getStack());
            }

            world.setBlockState(blockPos, ModBlocks.BLEEDING_OBSIDIAN.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(player,ModBlocks.BLEEDING_OBSIDIAN.getDefaultState()));
            world.playSound(player, blockPos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS);

            if (player != null) {
                context.getStack().damage(1,player,playerEntity -> playerEntity.sendToolBreakStatus(context.getHand()));
            }

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            if (entity.isPlayer() && entity instanceof ServerPlayerEntity player) {
                if (selected || player.getOffHandStack() == stack) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, TimeHelper.secToTick(12)));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, TimeHelper.secToTick(12)));
                }
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}

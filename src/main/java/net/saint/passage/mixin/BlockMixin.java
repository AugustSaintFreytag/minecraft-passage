package net.saint.passage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.passage.Mod;
import net.saint.passage.data.block.BlockDegradationUtil;

@Mixin(Block.class)
public abstract class BlockMixin {

	@Inject(method = { "onSteppedOn" }, at = { @At("HEAD") })
	private void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo callbackInfo) {
		if (world.isClient()) {
			return;
		}

		BlockDegradationUtil.handleBlockStep(world, pos, state);
	}

	@Inject(method = { "onPlaced" }, at = { @At("HEAD") })
	private void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack,
			CallbackInfo callbackInfo) {
		if (world.isClient()) {
			return;
		}

		if (Mod.CHUNK_DATA_MANAGER == null) {
			return;
		}

		Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(pos);

	}

}

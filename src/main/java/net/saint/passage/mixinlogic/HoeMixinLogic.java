package net.saint.passage.mixinlogic;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.saint.passage.Mod;
import net.saint.passage.config.BlockDegradationConfig;

public interface HoeMixinLogic {

	// Configuration

	static final SoundEvent SOUND_EVENT = SoundEvents.ITEM_BRUSH_BRUSHING_GRAVEL;

	// Interaction

	default void passage$$useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> callbackInfo) {
		var world = context.getWorld();

		if (world.isClient()) {
			return;
		}

		var player = context.getPlayer();
		var position = context.getBlockPos();
		var blockState = world.getBlockState(position);
		var blockId = Registries.BLOCK.getId(blockState.getBlock());

		if (!BlockDegradationConfig.isBlockDegradable(blockId)) {
			return;
		}

		if (player.isSneaking()) {
			// Raking operation, add negative steps to keep block pristine for longer.

			Mod.CHUNK_DATA_MANAGER.addNumberOfSteps(position, -Mod.CONFIG.blockRakeStepRemovalAmount, world.getTime());
			context.getStack().damage(1, player, null);
			world.playSound(null, position, SOUND_EVENT, SoundCategory.BLOCKS, 1.0f, (float) world.getRandom().nextTriangular(0.5, 0.05));

			callbackInfo.setReturnValue(ActionResult.SUCCESS);
			return;
		}

		// Normal hoe operations, reset steps and let vanilla proceed.
		Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(position);
	}

}

package net.saint.passage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.saint.passage.Mod;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

	@Shadow
	protected ServerWorld world;

	@Inject(method = "tryBreakBlock", at = @At("HEAD"))
	private void passage$tryBreakBlock(BlockPos blockPosition, CallbackInfoReturnable<Boolean> callbackInfo) {
		Mod.CHUNK_DATA_MANAGER.resetNumberOfSteps(blockPosition);
	}
}
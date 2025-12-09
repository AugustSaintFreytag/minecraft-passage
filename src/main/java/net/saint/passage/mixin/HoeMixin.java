package net.saint.passage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.saint.passage.mixinlogic.HoeMixinLogic;

@Mixin(HoeItem.class)
public abstract class HoeMixin implements HoeMixinLogic {

	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void patchassortment$useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> callbackInfo) {
		passage$$useOnBlock(context, callbackInfo);
	}

}

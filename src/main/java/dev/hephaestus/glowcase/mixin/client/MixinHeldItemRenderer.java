package dev.hephaestus.glowcase.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.hephaestus.glowcase.client.render.item.ItemHandRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {
	@Inject(method = "renderFirstPersonMap", at = @At("HEAD"), cancellable = true)
	void glowcase$renderFirstPersonTablet(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, CallbackInfo ci) {
		if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null) return;

		@Nullable ItemHandRenderer renderer = ItemHandRenderer.getRenderer(stack);
		if (renderer == null) return;

		renderer.render(matrices, vertexConsumers, light, stack);
		ci.cancel();
	}

	@ModifyExpressionValue(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;contains(Lnet/minecraft/component/ComponentType;)Z", ordinal = 0))
	private boolean glowcase$enableFirstPersonTabletRendering(boolean original, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		@Nullable ItemHandRenderer renderer = ItemHandRenderer.getRenderer(stack);
		return original || (renderer != null && renderer.visible(stack));
	}
}

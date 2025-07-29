package dev.hephaestus.glowcase.mixin.client;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.client.GlowcaseClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
	@ModifyArg(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0))
	public Identifier usePickupCrosshair(Identifier original) {
		if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr && MinecraftClient.getInstance().world.getBlockState(bhr.getBlockPos()).isOf(Glowcase.ITEM_PROVIDER_BLOCK.get()) && Glowcase.ITEM_PROVIDER_BLOCK.get().canPickup(MinecraftClient.getInstance().player, bhr.getBlockPos())) {
			return GlowcaseClient.PROVIDER_CROSSHAIR_TEXTURE;
		}

		return original;
	}
}

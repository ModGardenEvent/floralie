package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.client.util.BlockEntityRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public record ItemDisplayBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<ItemDisplayBlockEntity> {
	public static Identifier ITEM_TEXTURE = Glowcase.id("textures/item/item_display_block.png");

	@Override
	public void render(ItemDisplayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
		if (entity.getWorld() == null || entity.getWorld().getBlockState(entity.getPos()).isAir()) return;

		boolean renderAsBlock = entity.getRenderAsBlock();
		matrices.push();
		
		if (renderAsBlock && entity.getStack().getItem() instanceof BlockItem blockItem) {
			matrices.translate(0.5D, 0.5D, 0.5D);

			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F + entity.getYaw()));
			matrices.translate(entity.getOffset().x(), entity.getOffset().y(), entity.getOffset().z());

			matrices.translate(-0.5D, -0.5D, -0.5D);

			matrices.scale(entity.getScale().x(), entity.getScale().y(), entity.getScale().z());
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch()));

			MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(blockItem.getBlock().getDefaultState(), matrices, vertexConsumers, light, overlay);
		} else {
			matrices.translate(0.5D, 0D, 0.5D);

			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F + entity.getYaw()));
			matrices.translate(entity.getOffset().x(), entity.getOffset().y(), entity.getOffset().z());

			matrices.translate(0D, 0.5D, 0D);

			matrices.scale(entity.getScale().x(), entity.getScale().y(), entity.getScale().z());
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch()));

			context.getItemRenderer().renderItem(entity.getStack(), ItemDisplayContext.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
		}
		
		matrices.pop();

		if (entity.matchesStack(ItemStack.EMPTY) || BlockEntityRenderUtil.shouldRenderPlaceholder(entity.getPos())) BlockEntityRenderUtil.renderCenteredPlaceholder(entity, ITEM_TEXTURE, 1.0F, RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw()), matrices, vertexConsumers);
	}
}

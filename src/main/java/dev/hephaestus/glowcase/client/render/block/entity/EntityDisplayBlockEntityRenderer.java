package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.EntityDisplayBlockEntity;
import dev.hephaestus.glowcase.client.util.BlockEntityRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public record EntityDisplayBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<EntityDisplayBlockEntity> {
	public static Identifier ITEM_TEXTURE = Glowcase.id("textures/item/entity_display_block.png");

	@Override
	public void render(EntityDisplayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
		if (entity.getWorld() == null || entity.getWorld().getBlockState(entity.getPos()).isAir()) return;
		Entity camera = MinecraftClient.getInstance().getCameraEntity();

		if (camera == null) return;

		matrices.push();
		matrices.translate(0.5D, 0D, 0.5D);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw()));
		matrices.translate(entity.getOffset().x(), entity.getOffset().y(), entity.getOffset().z());
		matrices.scale(entity.getScale().x(), entity.getScale().y(), entity.getScale().z());
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch()));
		Entity renderEntity = entity.getDisplayEntity();
		if (renderEntity != null) {
			EntityRenderer<? super Entity, ?> entityRenderer = context.getEntityRenderDispatcher().getRenderer(renderEntity);
			EntityRenderState renderState = entityRenderer.getAndUpdateRenderState(renderEntity, tickDelta);
			// please don't crash
			MinecraftClient.getInstance().getEntityRenderDispatcher().render(
				renderState,
				0.0,
				0.0,
				0.0,
				matrices,
				vertexConsumers,
				light
			);
		}

		matrices.pop();

		if (entity.matchesStack(ItemStack.EMPTY) || BlockEntityRenderUtil.shouldRenderPlaceholder(entity.getPos())) BlockEntityRenderUtil.renderCenteredPlaceholder(entity, ITEM_TEXTURE, 1.0F, RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw()), matrices, vertexConsumers);
	}
}

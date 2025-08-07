package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.block.entity.TextBlockEntity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.RecipeBlockEntity;
import dev.hephaestus.glowcase.client.GlowcaseClient;
import dev.hephaestus.glowcase.client.util.BlockEntityRenderUtil;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record RecipeBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<RecipeBlockEntity> {
	private static final Identifier ITEM_TEXTURE = Glowcase.id("textures/item/recipe_block.png");

	public void render(RecipeBlockEntity entity, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
//		if (GlowcaseClient.EMI_LOADED) {
//			matrices.push();
//			matrices.translate(0.5D, 0.5D, 0.5D);
//
//			float rotation = -(entity.getCachedState().get(Properties.ROTATION) * 360) / 16.0F;
//
//			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
//			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
//			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.rotationY));
//			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.rotationX));
//
//			switch (entity.zOffset) {
//				case FRONT -> matrices.translate(0D, 0D, -0.4D);
//				case BACK -> matrices.translate(0D, 0D, 0.4D);
//				case CENTER -> matrices.translate(0D, 0D, 0D);
//			}
//
//			boolean rendered = EmiWorldRenderUtils.renderRecipe(matrices, entity.recipe, entity.getPos());
//			matrices.pop();
//			if (rendered) return;

		if (BlockEntityRenderUtil.shouldRenderPlaceholder(entity.getPos())) {
			BlockEntityRenderUtil.renderPlaceholderWithBlockRotation(entity, ITEM_TEXTURE, 1F, matrices, vertexConsumers, entity.zOffset == TextBlockEntity.ZOffset.CENTER ? 0.01F : entity.zOffset == TextBlockEntity.ZOffset.FRONT ? 0.4F : -0.4F);
		}
	}
}

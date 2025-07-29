package dev.hephaestus.glowcase.client.util;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.stream.IntStream;

public class BlockEntityRenderUtil {
	private static final Vector3f[] placeholderVertices = new Vector3f[]{
		new Vector3f(-0.5F, -0.5F, 0.0F),
		new Vector3f(0.5F, -0.5F, 0.0F),
		new Vector3f(0.5F, 0.5F, 0.0F),
		new Vector3f(-0.5F, 0.5F, 0.0F)
	};

	public static void renderPlaceholder(BlockEntity entity, Identifier texture, float scale, Quaternionf rotation, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float zOffset) {
		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(rotation);
		matrices.translate(0, 0, zOffset);
		matrices.scale(scale, scale, scale);
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texture));
		renderPlaceholderFace(matrices.peek(), vertexConsumer, entity.getPos());
		renderPlaceholderBackFace(matrices.peek(), vertexConsumer, entity.getPos());
		matrices.pop();
	}

	public static void renderBillboardPlaceholder(BlockEntity entity, Identifier texture, float scale, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera) {
		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - camera.getYaw()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-camera.getPitch()));
		matrices.scale(scale, scale, scale);
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texture));
		renderPlaceholderFace(matrices.peek(), vertexConsumer, entity.getPos());
		matrices.pop();
	}

	public static void renderTrackingPlaceholder(BlockEntity entity, Identifier texture, float scale, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity camera, float tickDelta) {
		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		Vec2f tracking = getTracking(camera, entity.getPos(), tickDelta);
		float pitch = tracking.x;
		float yaw = tracking.y;
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) (Math.PI + yaw)));
		matrices.multiply(RotationAxis.POSITIVE_X.rotation(-pitch));
		matrices.scale(scale, scale, scale);
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texture));
		renderPlaceholderFace(matrices.peek(), vertexConsumer, entity.getPos());
		matrices.pop();
	}

	public static void renderPlaceholderWithBlockRotation(BlockEntity entity, Identifier texture, float scale, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float zOffset) {
		renderPlaceholder(entity, texture, scale, RotationAxis.POSITIVE_Y.rotationDegrees(-(entity.getCachedState().get(Properties.ROTATION) * 360) / 16.0F), matrices, vertexConsumers, zOffset);
	}

	public static void renderPlaceholderWithBlockRotation(BlockEntity entity, Identifier texture, float scale, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
		renderPlaceholderWithBlockRotation(entity, texture, scale, matrices, vertexConsumers, 0F);
	}

	public static void renderCenteredPlaceholder(BlockEntity entity, Identifier texture, float scale, Quaternionf rotation, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
		renderPlaceholder(entity, texture, scale, rotation, matrices, vertexConsumers, 0F);
	}

	public static void renderFacingPlaceholder(BlockEntity entity, Identifier texture, float scale, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
		renderPlaceholder(entity, texture, scale, entity.getCachedState().get(Properties.FACING).getRotationQuaternion().mul(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F)), matrices, vertexConsumers, -0.4F);
	}

	public static boolean shouldRenderPlaceholder(BlockPos pos) {
		return shouldRenderPlaceholder(pos, true);
	}

	public static boolean shouldRenderPlaceholder(BlockPos pos, boolean disappearWhenFaced) {
		return MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.isHolding(stack -> stack.isIn(Glowcase.ITEM_TAG)) && (!disappearWhenFaced || !(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr && bhr.getBlockPos().equals(pos)));
	}

	public static Vec2f getTracking(Entity camera, BlockPos pos, float delta) {
		double d = pos.getX() - camera.getLerpedPos(delta).x + 0.5;
		double e = pos.getY() - camera.getEyeY() + 0.5;
		double f = pos.getZ() - camera.getLerpedPos(delta).z + 0.5;
		double g = MathHelper.sqrt((float) (d * d + f * f));

		float pitch = (float) ((-MathHelper.atan2(e, g)));
		float yaw = (float) (-MathHelper.atan2(f, d) + Math.PI / 2);

		return new Vec2f(pitch, yaw);
	}

	private static void renderPlaceholderFace(MatrixStack.Entry entry, VertexConsumer vertexConsumer, BlockPos pos) {
		int color = MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr && bhr.getBlockPos().equals(pos) ? 0x808080 : 0xFFFFFF;
		placeholderVertex(entry, vertexConsumer, placeholderVertices[0], 0, 1, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[1], 1, 1, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[2], 1, 0, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[3], 0, 0, color);
	}

	private static void renderPlaceholderBackFace(MatrixStack.Entry entry, VertexConsumer vertexConsumer, BlockPos pos) {
		int color = MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr && bhr.getBlockPos().equals(pos) ? 0x404040 : 0x808080;
		placeholderVertex(entry, vertexConsumer, placeholderVertices[3], 0, 0, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[2], 1, 0, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[1], 1, 1, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[0], 0, 1, color);
	}

	private static void placeholderVertex(
		MatrixStack.Entry matrix, VertexConsumer vertexConsumer, Vector3f vertex, float u, float v, int color) {
		vertexConsumer.vertex(matrix, vertex.x(), vertex.y(), vertex.z())
			.color(color)
			.texture(u, v)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
			.normal(0, 1, 0);
	}
}

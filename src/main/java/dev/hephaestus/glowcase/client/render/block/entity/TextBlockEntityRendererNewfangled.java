package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.client.util.BlockEntityRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class TextBlockEntityRendererNewfangled implements BlockEntityRenderer<TextBlockEntity> {
	public static Identifier ITEM_TEXTURE = Glowcase.id("textures/item/text_block.png");
	private final BlockEntityRendererFactory.Context context;
	private boolean wasOutOfRange = false;
	
	public TextBlockEntityRendererNewfangled(BlockEntityRendererFactory.Context context) {
		this.context = context;
	}
	
	@Override
	public void render(
		TextBlockEntity entity,
		float tickProgress,
		MatrixStack matrices,
		VertexConsumerProvider vertexConsumers,
		int light,
		int overlay,
		Vec3d cameraPos
	) {
		Entity camera = MinecraftClient.getInstance().getCameraEntity();
		if (camera != null && entity.viewDistance >= 0) {
			double dx = camera.getX() - (entity.getPos().getX() + 0.5);
			double dy = camera.getY() - (entity.getPos().getY() + 0.5);
			double dz = camera.getZ() - (entity.getPos().getZ() + 0.5);

			if ((dx * dx + dy * dy + dz * dz) > (entity.viewDistance * entity.viewDistance)) {
				if (!wasOutOfRange) {
					entity.renderDirty = true;
					wasOutOfRange = true;
				}
			} else {
				if (wasOutOfRange) {
					entity.renderDirty = true;
				}

				wasOutOfRange = false;
			}
		}

		if (entity.renderDirty) {
			entity.renderDirty = false;
			// no idea what Manager was but it probably doesn't matter
			// YOLO
//			Manager.markForRebuild(entity.getPos());
		}

		// todo: actually render text lol
		if (entity.getWorld() == null || entity.getWorld().getBlockState(entity.getPos()).isAir()) return;
		if (entity.lines.stream().allMatch(t -> t.getString().isBlank()) || BlockEntityRenderUtil.shouldRenderPlaceholder(entity.getPos())) BlockEntityRenderUtil.renderPlaceholderWithBlockRotation(entity, ITEM_TEXTURE, 1.0F, matrices, vertexConsumers, entity.zOffset == TextBlockEntity.ZOffset.CENTER ? 0.01F : entity.zOffset == TextBlockEntity.ZOffset.FRONT ? 0.4F : -0.4F);
	}
}

package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.client.util.BlockEntityRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class TextBlockEntityRenderer extends BakedBlockEntityRenderer<TextBlockEntity> {
	public static Identifier ITEM_TEXTURE = Glowcase.id("textures/item/text_block.png");
	private boolean wasOutOfRange = false;

	public TextBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	public boolean shouldBake(TextBlockEntity entity) {
		return !entity.lines.isEmpty();
	}

	@Override
	public void renderUnbaked(
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
			BakedBlockEntityRenderer.Manager.markForRebuild(entity.getPos());
		}

		if (entity.getWorld() == null || entity.getWorld().getBlockState(entity.getPos()).isAir()) return;
		if (entity.lines.stream().allMatch(t -> t.getString().isBlank()) || BlockEntityRenderUtil.shouldRenderPlaceholder(entity.getPos())) BlockEntityRenderUtil.renderPlaceholderWithBlockRotation(entity, ITEM_TEXTURE, 1.0F, matrices, vertexConsumers, entity.zOffset == TextBlockEntity.ZOffset.CENTER ? 0.01F : entity.zOffset == TextBlockEntity.ZOffset.FRONT ? 0.4F : -0.4F);
	}

	@Override
	public void renderBaked(TextBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
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

				return;
			} else {
				if (wasOutOfRange) {
					entity.renderDirty = true;
				}

				wasOutOfRange = false;
			}
		}

		matrices.push();
		matrices.translate(0.5D, 0.5D, 0.5D);

		float rotation = -(entity.getCachedState().get(Properties.ROTATION) * 360) / 16.0F;
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));

		switch (entity.zOffset) {
			case FRONT -> matrices.translate(0D, 0D, 0.4D);
			case BACK -> matrices.translate(0D, 0D, -0.4D);
		}

		float scale = 0.010416667F * entity.scale;
		matrices.scale(scale, -scale, scale);
		TextRenderer textRenderer = this.context.getTextRenderer();

		double maxLength = 0;
		double minLength = Double.MAX_VALUE;
		for (int i = 0; i < entity.lines.size(); ++i) {
			maxLength = Math.max(maxLength, textRenderer.getWidth(entity.lines.get(i)));
			minLength = Math.min(minLength, textRenderer.getWidth(entity.lines.get(i)));
		}

		matrices.translate(0, -((entity.lines.size() - 0.25) * 12) / 2D, 0D);
		for (int i = 0; i < entity.lines.size(); ++i) {
			Text line = entity.lines.get(i);
			double width = textRenderer.getWidth(line);
			if (width == 0) continue;

			double dX = switch (entity.textAlignment) {
				case LEFT -> -maxLength / 2D;
				case CENTER -> (maxLength - width) / 2D - maxLength / 2D;
				case CENTER_LEFT -> -(50D / entity.scale) - (width / 2D);
				case CENTER_RIGHT -> (50D / entity.scale) - (width / 2D);
				case RIGHT -> maxLength - width - maxLength / 2D;
			};

			matrices.push();
			matrices.translate(dX, 0, 0);

			TextRenderer.Drawer drawer = textRenderer.new Drawer(
				vertexConsumers,
				0.0F,
				i * 12,
				entity.color,
				entity.shadow,
				matrices.peek().getPositionMatrix(),
				TextRenderer.TextLayerType.NORMAL,
				LightmapTextureManager.MAX_LIGHT_COORDINATE
			);

			// Yep, we're back to that hack again.
			if (entity.backgroundColor != 0) {
				final BakedGlyph.Rectangle rect = new BakedGlyph.Rectangle(
					0.0F - 4, (i + 1) * 12 - 2f,
					(float) width + 4, i * 12 - 2f,
					-0.01F, entity.backgroundColor);

				drawer.addRectangle(rect);
			}

			TextVisitFactory.visitFormatted(line, Style.EMPTY, drawer);
			drawer.drawLayer(0.0F);

			matrices.pop();
		}
		matrices.pop();
	}

}

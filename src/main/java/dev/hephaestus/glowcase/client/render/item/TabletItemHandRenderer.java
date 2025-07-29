package dev.hephaestus.glowcase.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.client.GlowcaseClient;
import dev.hephaestus.glowcase.client.ScreenImageCache;
import dev.hephaestus.glowcase.client.render.block.entity.ScreenBlockEntityRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.List;

public class TabletItemHandRenderer extends ItemHandRenderer {
	private static final Identifier TABLET_TEXTURE = Glowcase.id("textures/gui/tablet_hand.png");

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack) {
		matrices.push();
//		RenderSystem.enableBlend();

		// Render background

		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
		matrices.scale(0.38F, 0.38F, 0.38F);
		matrices.translate(-0.5F, -0.5F, 0.0F);
		matrices.scale(0.0078125F, 0.0078125F, 0.0078125F);

		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getText(TABLET_TEXTURE));
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();

		vertexConsumer.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(Colors.WHITE).texture(0.0F, 1.0F).light(light);
		vertexConsumer.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(Colors.WHITE).texture(1.0F, 1.0F).light(light);
		vertexConsumer.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(Colors.WHITE).texture(1.0F, 0.0F).light(light);
		vertexConsumer.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(Colors.WHITE).texture(0.0F, 0.0F).light(light);

		if (!stack.contains(Glowcase.SLIDESHOW_COMPONENT.get()) || !stack.contains(Glowcase.CURRENT_SLIDE_COMPONENT.get())) {
			matrices.pop();
			return;
		}

		List<Pair<String, String>> slideshow = stack.get(Glowcase.SLIDESHOW_COMPONENT.get());
		Integer index = stack.getOrDefault(Glowcase.CURRENT_SLIDE_COMPONENT.get(), 0);

		if (slideshow == null || index >= slideshow.size()) {
			matrices.pop();
			return;
		}

		// Render current slide text

		{
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			MutableText literal = Text.translatable("gui.glowcase.progress", index + 1, slideshow.size());

			float font_scale = 1f;
			float font_width = textRenderer.getWidth(literal);
			float font_max_width = 142f/64f*14f;
			if (font_width >= font_max_width) {
				font_scale = font_max_width / font_width;
			}

			float off_x = 142f/64f*29.2f - (font_width * font_scale)/2f;
			float off_y = 142f/64f*8.2f;

			matrices.translate(off_x, off_y, -.01f);
			matrices.scale(font_scale, font_scale, 1f);
			textRenderer.draw(literal, 0, 0, 0xFFFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
			matrices.translate(-off_x, -off_y, .01f);
			matrices.scale(1f/font_scale, 1f/font_scale, 1f);
		}

		// Render current picture

		String url = slideshow.get(index).getFirst();

		ScreenImageCache.ScreenTexture image = GlowcaseClient.screenImageCache.getImage(url, null);
		Identifier texture = image.getTexture().getSecond();
		if (texture == null) {
			matrices.pop();
			return;
		}

		vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getText(texture));
		matrix4f = matrices.peek().getPositionMatrix();

		float pixel = 142f/64f;

		float x1 = pixel * -21;
		float y1 = pixel * -15;
		float x2 = pixel * 21;
		float y2 = pixel * 13;

		Pair<Float, Float> scale = ScreenBlockEntityRenderer.getScale(x2-x1, y2-y1, image.getWidth(), image.getHeight());

		Float scaled_width = scale.getFirst();
		Float scaled_height = scale.getSecond();

		x1 = -scaled_width / 2f;
		x2 = scaled_width / 2f;
		y1 = -scaled_height / 2f;
		y2 = scaled_height / 2f;

		vertexConsumer.vertex(matrix4f, 64+x1, 64+y1, -0.01F).color(Colors.WHITE).texture(0f, 0f).light(light);
		vertexConsumer.vertex(matrix4f, 64+x1, 64+y2, -0.01F).color(Colors.WHITE).texture(0f, 1f).light(light);
		vertexConsumer.vertex(matrix4f, 64+x2, 64+y2, -0.01F).color(Colors.WHITE).texture(1f, 1f).light(light);
		vertexConsumer.vertex(matrix4f, 64+x2, 64+y1, -0.01F).color(Colors.WHITE).texture(1f, 0f).light(light);

		matrices.pop();
	}
}

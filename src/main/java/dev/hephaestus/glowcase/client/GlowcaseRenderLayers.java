package dev.hephaestus.glowcase.client;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

import static net.minecraft.client.render.RenderPhase.ENABLE_LIGHTMAP;
import static net.minecraft.client.render.RenderPhase.NO_TEXTURE;

// fixme: Don't do this! 1.21.6 removes RenderLayers
public abstract class GlowcaseRenderLayers {
	public static RenderPhase.Layering GLOWCASE_POLYGON_OFFSET_LAYERING = new RenderPhase.Layering("glowcase_polygon_offset_layering", () -> {
		// fixme: what's the equivalent
//		RenderSystem.polygonOffset(-1, -1.0F);
//		RenderSystem.enablePolygonOffset();
	}, () -> {
//		RenderSystem.polygonOffset(0.0F, 0.0F);
//		RenderSystem.disablePolygonOffset();
	});

	// Use a custom render layer to render the text plate - mimics DrawableHelper's RenderSystem call
	public static final RenderLayer TEXT_PLATE = RenderLayer.of(
		"glowcase_text_plate",
		256,
		true,
		true,
		RenderPipelines.RENDERTYPE_TEXT_BG_SEETHROUGH,
		RenderLayer.MultiPhaseParameters.builder()
			.texture(NO_TEXTURE)
			.build(false)
	);


	private static final BiFunction<Identifier, Boolean, RenderLayer> SCREEN = Util.memoize((texture, culling) -> RenderLayer.of(
		"glowcase_screen",
		786432,
		true,
		false,
		RenderPipelines.RENDERTYPE_TEXT,
		RenderLayer.MultiPhaseParameters.builder()
			.texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
			.lightmap(ENABLE_LIGHTMAP)
			.layering(GLOWCASE_POLYGON_OFFSET_LAYERING)
			.build(false)
	));

	public static RenderLayer getScreen(Identifier texture, boolean culling) {
		return SCREEN.apply(texture, culling);
	}
}

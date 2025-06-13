package dev.hephaestus.glowcase.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

// fixme: Don't do this! 1.21.6 removes RenderLayers
public abstract class GlowcaseRenderLayers extends RenderLayer {
	public static RenderPhase.Layering GLOWCASE_POLYGON_OFFSET_LAYERING = new Layering("glowcase_polygon_offset_layering", () -> {
		RenderSystem.polygonOffset(-1, -1.0F);
		RenderSystem.enablePolygonOffset();
	}, () -> {
		RenderSystem.polygonOffset(0.0F, 0.0F);
		RenderSystem.disablePolygonOffset();
	});

	// Use a custom render layer to render the text plate - mimics DrawableHelper's RenderSystem call
	public static final RenderLayer TEXT_PLATE = RenderLayer.of("glowcase_text_plate", VertexFormats.POSITION_COLOR,
		VertexFormat.DrawMode.QUADS, 256, true, true, RenderLayer.MultiPhaseParameters.builder()
			.texture(NO_TEXTURE)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.writeMaskState(COLOR_MASK)
			.program(COLOR_PROGRAM)
			.build(false));


	private static final BiFunction<Identifier, Boolean, RenderLayer> SCREEN = Util.memoize((texture, culling) -> {
		return RenderLayer.of("glowcase_screen", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 786432, false, true, MultiPhaseParameters.builder()
			.program(TEXT_PROGRAM)
			.texture(new Texture(texture, false, false))
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.cull(culling ? ENABLE_CULLING : DISABLE_CULLING)
			.lightmap(ENABLE_LIGHTMAP)
			.layering(GLOWCASE_POLYGON_OFFSET_LAYERING)
			.build(false));
	});

	public static RenderLayer getScreen(Identifier texture, boolean culling) {
		return SCREEN.apply(texture, culling);
	}

	public GlowcaseRenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
		super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
	}
}

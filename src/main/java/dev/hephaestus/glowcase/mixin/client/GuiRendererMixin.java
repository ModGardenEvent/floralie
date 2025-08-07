package dev.hephaestus.glowcase.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.hephaestus.glowcase.client.gui.widget.ingame.SuggestionListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Supplier;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
	@Unique private final GpuBuffer texColorBuffer = RenderSystem.getDevice().createBuffer(() -> "TexColorQuad", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, 16 * VertexFormats.POSITION_TEXTURE_COLOR.getVertexSize());

	@Shadow @Final private List<GuiRenderer.Draw> draws;
	@Shadow @Final private BufferAllocator allocator;

	@Shadow protected abstract void render(Supplier<String> nameSupplier, Framebuffer framebuffer, GpuBufferSlice fogBuffer, GpuBufferSlice dynamicTransformsBuffer, GpuBuffer buffer, VertexFormat.IndexType indexType, int from, int to);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/DynamicUniforms;write(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;F)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;", shift = At.Shift.AFTER), method = "renderPreparedDraws")
	private void findBlurDraw(GpuBufferSlice fogBuffer, CallbackInfo ci, @Share("suggestionBlurLayer") LocalRef<Integer> suggestionBlurLayer) {
		suggestionBlurLayer.set(Integer.MAX_VALUE);
		for (int i = 0; i < this.draws.size(); i++) {
			GuiRenderer.Draw draw = draws.get(i);
			if (draw.textureSetup().texure0() == SuggestionListWidget.FRAMEBUFFER.getColorAttachmentView() && draw.pipeline() == RenderPipelines.MOJANG_LOGO) {
				suggestionBlurLayer.set(i);
				this.draws.remove(draw);
				break;
			}
		}
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 2), method = "renderPreparedDraws")
	private int renderBeforeSuggestionBlurAfterBlur(List<GuiRenderer.Draw> instance, Operation<Integer> original, @Share("suggestionBlurLayer") LocalRef<Integer> suggestionBlurLayer, @Share("afterBlurLimit") LocalRef<Integer> afterBlurLimit) {
		Integer i = original.call(instance);
		afterBlurLimit.set(i); // Save it for mod compat (in case another mod also changes it)
		return Math.min(suggestionBlurLayer.get(), i);
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0), method = "renderPreparedDraws")
	private int renderBeforeSuggestionBlurBeforeBlur(List<GuiRenderer.Draw> instance, Operation<Integer> original, @Share("suggestionBlurLayer") LocalRef<Integer> suggestionBlurLayer, @Share("beforeBlurLimit") LocalRef<Integer> beforeBlurLimit) {
		Integer i = original.call(instance);
		beforeBlurLimit.set(i); // Save it for mod compat (in case another mod also changes it)
		return Math.min(suggestionBlurLayer.get(), i);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Ljava/util/function/Supplier;Lnet/minecraft/client/gl/Framebuffer;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;II)V", ordinal = 0, shift = At.Shift.AFTER), method = "renderPreparedDraws")
	private void renderSuggestionsBlurBeforeBlur(GpuBufferSlice fogBuffer, CallbackInfo ci, @Local GpuBuffer gpuBuffer, @Local VertexFormat.IndexType indexType, @Local(ordinal = 1) GpuBufferSlice gpuBufferSlice, @Share("suggestionBlurLayer") LocalRef<Integer> suggestionBlurLayer, @Share("beforeBlurLimit") LocalRef<Integer> beforeBlurLimit) {
		Integer layer = suggestionBlurLayer.get();
		if (this.draws.size() > layer) {
			renderSuggestionsBlur(() -> "GUI before blur", fogBuffer, gpuBuffer, indexType, gpuBufferSlice, layer, beforeBlurLimit.get());
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Ljava/util/function/Supplier;Lnet/minecraft/client/gl/Framebuffer;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;II)V", ordinal = 1, shift = At.Shift.AFTER), method = "renderPreparedDraws")
	private void renderSuggestionsBlurAfterBlur(GpuBufferSlice fogBuffer, CallbackInfo ci, @Local GpuBuffer gpuBuffer, @Local VertexFormat.IndexType indexType, @Local(ordinal = 1) GpuBufferSlice gpuBufferSlice, @Share("suggestionBlurLayer") LocalRef<Integer> suggestionBlurLayer, @Share("afterBlurLimit") LocalRef<Integer> afterBlurLimit) {
		Integer layer = suggestionBlurLayer.get();
		if (this.draws.size() > layer) {
			renderSuggestionsBlur(() -> "GUI after blur", fogBuffer, gpuBuffer, indexType, gpuBufferSlice, layer, afterBlurLimit.get());
		}
	}

	@Unique
	private void renderSuggestionsBlur(Supplier<String> nameSupplier, GpuBufferSlice fogBuffer, GpuBuffer indexBuffer, VertexFormat.IndexType indexType, GpuBufferSlice dynamicTransformsBuffer, int from, int to) {
		RenderSystem.getDevice().createCommandEncoder().clearColorTexture(SuggestionListWidget.FRAMEBUFFER.getColorAttachment(), 0);

		MinecraftClient client = MinecraftClient.getInstance();
		Framebuffer framebuffer = SuggestionListWidget.FRAMEBUFFER;
		Framebuffer clientFramebuffer = client.getFramebuffer();
		if (framebuffer.textureWidth != clientFramebuffer.textureWidth || framebuffer.textureHeight != clientFramebuffer.textureHeight) {
			framebuffer.resize(clientFramebuffer.textureWidth, clientFramebuffer.textureHeight);

			int width = client.getWindow().getScaledWidth();
			int height = client.getWindow().getScaledHeight();

			Matrix3x2f matrices = new Matrix3x2f();
			BufferBuilder bufferBuilder = new BufferBuilder(this.allocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(matrices, 0, 	0, 	from).texture(0, 0).color(0XFFFFFFFF);
			bufferBuilder.vertex(matrices, 0, 	height,	from).texture(0, 1).color(0XFFFFFFFF);
			bufferBuilder.vertex(matrices, width, 	height,	from).texture(1, 1).color(0XFFFFFFFF);
			bufferBuilder.vertex(matrices, width, 	0, 	from).texture(1, 0).color(0XFFFFFFFF);

			try (BuiltBuffer builtBuffer = bufferBuilder.end()) {
				RenderSystem.getDevice().createCommandEncoder().writeToBuffer(texColorBuffer.slice(), builtBuffer.getBuffer());
			}
		}

		copyTexture(clientFramebuffer, framebuffer, dynamicTransformsBuffer);
		if ((float) client.options.getMenuBackgroundBlurrinessValue() >= 1.0F) {
			renderBlur(framebuffer);
		}

		this.render(nameSupplier, clientFramebuffer, fogBuffer, dynamicTransformsBuffer, indexBuffer, indexType, from, to);
	}

	@Unique
	public void copyTexture(Framebuffer sourceBuffer, Framebuffer targetBuffer, GpuBufferSlice dynamicTransformsBuffer) {
		RenderSystem.assertOnRenderThread();
		RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
		GpuBuffer indexBuffer = shapeIndexBuffer.getIndexBuffer(6);

		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
			() -> "Copy render target",
			targetBuffer.getColorAttachmentView(), OptionalInt.empty()
		)) {
			renderPass.setPipeline(RenderPipelines.GUI_TEXTURED);
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransformsBuffer);
			renderPass.setIndexBuffer(indexBuffer, shapeIndexBuffer.getIndexType());
			renderPass.setVertexBuffer(0, texColorBuffer);
			renderPass.bindSampler("Sampler0", sourceBuffer.getColorAttachmentView());

			renderPass.drawIndexed(0, 0, 6, 1);
		}
	}

	@Inject(at = @At("RETURN"), method = "renderPreparedDraws")
	private void decrementPool(GpuBufferSlice fogBuffer, CallbackInfo ci) {
		SuggestionListWidget.POOL.decrementLifespan();
	}

	@Unique
	public void renderBlur(Framebuffer framebuffer) {
		PostEffectProcessor postEffectProcessor = MinecraftClient.getInstance().getShaderLoader().loadPostEffect(SuggestionListWidget.BLUR_ID, DefaultFramebufferSet.MAIN_ONLY);
		if (postEffectProcessor != null) {
			postEffectProcessor.render(framebuffer, SuggestionListWidget.POOL);
		}
	}
}

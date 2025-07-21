package dev.hephaestus.glowcase.client.render.item.tint;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record GlowcaseTintSource(int defaultColor) implements TintSource {
	public static final MapCodec<GlowcaseTintSource> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Codecs.RGB.fieldOf("default").forGetter(GlowcaseTintSource::defaultColor)).apply(instance, GlowcaseTintSource::new)
	);

	@Override
	public int getTint(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user) {
		NbtComponent component = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
		if (component == null) return defaultColor;

		NbtCompound nbt = component.copyNbt();
		int color = nbt.getInt("color", 0);
		if (color != 0 && color != defaultColor) return color;
		return 0xFFAA00AA;
	}

	@Override
	public MapCodec<GlowcaseTintSource> getCodec() {
		return CODEC;
	}
}

package dev.hephaestus.glowcase.item.component;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Unit;

public record LockComponent(Unit unit) {
	public static final Codec<LockComponent> CODEC = Unit.CODEC.xmap(
		LockComponent::new, component -> component.unit);
	
	public static final ComponentType<LockComponent> TYPE = ComponentType.<LockComponent>builder()
		.codec(CODEC)
		.packetCodec(PacketCodecs.codec(CODEC))
		.build();
	
	public static LockComponent of() {
		return new LockComponent(Unit.INSTANCE);
	}
}

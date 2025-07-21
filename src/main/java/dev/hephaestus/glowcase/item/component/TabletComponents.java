package dev.hephaestus.glowcase.item.component;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TabletComponents {

	// TODO: Rewrite to use RecordCodecBuilder rather than three components
	// (see NoteComponent.java for an example)

	public static final ComponentType<Pair<UUID, BlockPos>> LINKED_SCREEN_TYPE;
	public static final ComponentType<Integer> CURRENT_SLIDE_TYPE;
	public static final ComponentType<List<Pair<String,String>>> SLIDESHOW_COMPONENT_TYPE;

	static {
		{
			Codec<UUID> uuidCodec = Codec.INT_STREAM.comapFlatMap(stream -> Util.decodeFixedLengthArray(stream, 4).map(Uuids::toUuid), uuid -> Arrays.stream(Uuids.toIntArray(uuid)));
			Codec<Pair<UUID, BlockPos>> codec = Codec.mapPair(uuidCodec.fieldOf("uuid"), BlockPos.CODEC.fieldOf("pos")).codec();
			LINKED_SCREEN_TYPE = ComponentType.<Pair<UUID, BlockPos>>builder()
				.codec(codec)
				.packetCodec(PacketCodecs.registryCodec(codec))
				.build();
		}

		{
			CURRENT_SLIDE_TYPE = ComponentType.<Integer>builder()
				.codec(Codecs.NON_NEGATIVE_INT)
				.packetCodec(PacketCodecs.registryCodec(Codecs.NON_NEGATIVE_INT))
				.build();
		}

		{
			Codec<List<Pair<String, String>>> codec = Codec.mapPair(Codec.STRING.fieldOf("url"), Codec.STRING.fieldOf("alt")).codec().listOf();
			SLIDESHOW_COMPONENT_TYPE = ComponentType.<List<Pair<String,String>>>builder()
				.codec(codec)
				.packetCodec(PacketCodecs.registryCodec(codec))
				.build();
		}
	}
}

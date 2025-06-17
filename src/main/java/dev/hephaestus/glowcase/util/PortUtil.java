package dev.hephaestus.glowcase.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Just a grab-bag of things you use when porting code.
 * <br>
 * Put anything that makes porting easier here.
 */
public final class PortUtil {
	private PortUtil() {}
	
	public static void putUuid(NbtCompound nbt, String key, UUID uuid) {
		nbt.putNullable(key, Uuids.INT_STREAM_CODEC, uuid);
	}
	
	public static UUID getUuid(NbtCompound nbt, String key) {
		return nbt.get(key, Uuids.INT_STREAM_CODEC).orElse(null);
	}
}

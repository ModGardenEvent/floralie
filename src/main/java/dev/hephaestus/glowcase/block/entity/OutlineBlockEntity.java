package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public class OutlineBlockEntity extends GlowcaseBlockEntity {
	public Vec3i offset = Vec3i.ZERO;
	public Vec3i scale = new Vec3i(1, 1, 1);
	public int color = 0xFFFFFF;

	public OutlineBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.OUTLINE_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		tag.putIntArray("offset", new int[]{this.offset.getX(), this.offset.getY(), this.offset.getZ()});
		tag.putIntArray("scale", new int[]{this.scale.getX(), this.scale.getY(), this.scale.getZ()});
		tag.putInt("color", this.color);
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);

		tag.getIntArray("offset").ifPresent(ia -> this.offset = new Vec3i(ia[0], ia[1], ia[2]));
		tag.getIntArray("scale").ifPresent(ia -> this.scale = new Vec3i(ia[0], ia[1], ia[2]));

		tag.getInt("color").ifPresent(i -> this.color = i);
	}
}

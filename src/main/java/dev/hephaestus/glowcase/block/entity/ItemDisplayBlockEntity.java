package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class ItemDisplayBlockEntity extends DisplayBlockEntity implements StackInteractable {
	protected ItemStack stack = ItemStack.EMPTY;

	public ItemStack getStack() {
		return stack;
	}

	@Override
	public boolean matchesStack(ItemStack stack) {
		return ItemStack.areItemsEqual(this.stack, stack);
	}

	@Override
	public void setFromStack(ItemStack stack) {
		this.stack = stack.copy();
		this.markDirty();
	}

	@Override
	public void unsetFromStack() {
		this.stack = ItemStack.EMPTY;
		this.markDirty();
	}

	public ItemDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.ITEM_DISPLAY_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);
		if (!this.stack.isEmpty()) tag.put("item", this.stack.toNbt(registryLookup));
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);
		this.stack = tag.contains("item") ? ItemStack.fromNbt(registryLookup, tag.getCompoundOrEmpty("item")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
	}
}

package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ItemProviderBlockEntity extends GlowcaseBlockEntity implements InfiniteInventory, StackInteractable {
	protected ItemStack stack = ItemStack.EMPTY;
	protected GivesItem givesItem = GivesItem.ALWAYS;
	protected boolean invisible = false;
	public long cooldown = 0;
	protected Map<UUID, Long> givenTimes = new HashMap<>();

	public ItemProviderBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.ITEM_PROVIDER_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public boolean matchesStack(ItemStack stack) {
		return ItemStack.areItemsEqual(this.stack, stack);
	}

	@Override
	public void setFromStack(ItemStack stack) {
		this.stack = stack.copy();
		this.givenTimes.clear();
		this.markDirty();
	}

	@Override
	public void unsetFromStack() {
		this.stack = ItemStack.EMPTY;
		this.markDirty();
	}

	@Override
	public ItemStack getStack() {
		return stack;
	}

	public GivesItem getGivesItem() {
		return givesItem;
	}

	public boolean isInvisible() {
		return this.invisible;
	}

	public void setGivesItem(GivesItem givesItem) {
		this.givesItem = givesItem;
		markDirty();
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);
		if (!this.stack.isEmpty()) tag.put("item", this.stack.toNbt(registryLookup));
		tag.putString("gives_item", this.givesItem.name());
		tag.putLong("cooldown", this.cooldown);
		NbtCompound timesNbt = new NbtCompound();
		givenTimes.forEach((id, tick) -> timesNbt.putLong(id.toString(), tick));
		tag.put("given_times", timesNbt);
		tag.putBoolean("invisible", this.invisible);
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);
		this.stack = tag.contains("item") ? ItemStack.fromNbt(registryLookup, tag.getCompoundOrEmpty("item")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
		if (tag.contains("gives_item")) {
			this.givesItem = GivesItem.valueOf(tag.getString("gives_item", GivesItem.ALWAYS.name()));
		} else {
			this.givesItem = GivesItem.ALWAYS;
		}
		this.cooldown = tag.getLong("cooldown", 0);

		givenTimes.clear();
		NbtCompound given = tag.getCompoundOrEmpty("given_times");
		for (String key : given.getKeys()) {
			givenTimes.put(UUID.fromString(key), given.getLong(key, 0));
		}

		this.invisible = tag.getBoolean("invisible", false);
	}

	public void cycleGiveType() {
		this.givesItem = GivesItem.values()[(this.givesItem.ordinal() + 1) % GivesItem.values().length];
		givenTimes.clear();
		markDirty();
	}

	public long getCooldownTicks(PlayerEntity player) {
		return givenTimes.containsKey(player.getUuid()) ? givenTimes.get(player.getUuid()) + this.cooldown * 20 - world.getTime() : 0;
	}

	public boolean canGiveTo(PlayerEntity player) {
		if (!hasItem()) return false;
		else return switch (this.givesItem) {
			case ALWAYS -> true;
			case TIMED -> player.isCreative() || getCooldownTicks(player) <= 0;
			case ONE -> player.isCreative() || !player.getInventory().containsAny(Set.of(stack.getItem()));
		};
	}

	public void giveTo(PlayerEntity player) {
		ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
		boolean holdingSameAsDisplay = ItemStack.areItemsAndComponentsEqual(getStack(), itemStack);

		if (itemStack.isEmpty()) {
			ItemStack stackToGive = getStack().copy();
			if (player.isSneaking()) {
				stackToGive.setCount(stackToGive.getMaxCount());
			}

			player.setStackInHand(Hand.MAIN_HAND, stackToGive);
		} else if (holdingSameAsDisplay) {
			itemStack.increment(getStack().getCount());
			itemStack.capCount(itemStack.getMaxCount());
			player.setStackInHand(Hand.MAIN_HAND, itemStack);
		} else {
			return;
		}

		if (!player.isCreative()) {
			givenTimes.put(player.getUuid(), world.getTime());
			markDirty();
		}
	}

	public enum GivesItem implements StringIdentifiable {
		ALWAYS, TIMED, ONE;

		public static final Codec<GivesItem> CODEC = StringIdentifiable.createCodec(GivesItem::values);

		@Override
		public String asString() {
			return name().toLowerCase(Locale.ROOT);
		}
	}
}

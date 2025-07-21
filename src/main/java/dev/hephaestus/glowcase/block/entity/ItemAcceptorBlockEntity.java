package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class ItemAcceptorBlockEntity extends GlowcaseBlockEntity {
	private Identifier item = Identifier.ofVanilla("air");
	public int count = 1;
	public int pulse = 4;
	public OutputDirection outputDirection = OutputDirection.BACK;
	public boolean isItemTag = false;
	private List<Item> itemTagList = List.of();

	public ItemAcceptorBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.ITEM_ACCEPTOR_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		tag.putString("item", this.item.toString());
		tag.putInt("count", this.count);
		tag.putInt("pulse", this.pulse);
		tag.putBoolean("is_item_tag", this.isItemTag);
		tag.putString("output_direction", this.outputDirection.name());
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);

		setItem(Identifier.tryParse(tag.getString("item", "minecraft:air")));

		this.count = tag.getInt("count", 1);

		this.pulse = tag.getInt("pulse", 4);

		this.isItemTag = tag.getBoolean("is_item_tag", false);
		OutputDirection value = OutputDirection.getByName(tag.getString("output_direction"));

		if (value != null) {
			this.outputDirection = value;
		}
	}

	public Identifier getItem() {
		return item;
	}

	public void setItem(Identifier item) {
		if (item == null) {
			return;
		}

		this.item = item;

		TagKey<Item> itemTag = TagKey.of(RegistryKeys.ITEM, item);
		itemTagList = Registries.ITEM.stream().filter(it -> it.getDefaultStack().isIn(itemTag)).toList();
	}

	public ItemStack getDisplayItemStack() {
		if (isItemTag) {
			if (itemTagList.isEmpty()) {
				return ItemStack.EMPTY;
			}

			return itemTagList.get((int) (Util.getMeasuringTimeMs() / 1000f) % itemTagList.size()).getDefaultStack();
		} else {
			return Registries.ITEM.get(item).getDefaultStack();
		}
	}

	public boolean isItemAccepted(ItemStack stack) {
		boolean isEqual = isItemTag
			? stack.isIn(TagKey.of(RegistryKeys.ITEM, item))
			: stack.isOf(Registries.ITEM.get(item));

		return isEqual && stack.getCount() >= count;
	}

	public int getPulse() {
		return pulse;
	}

	public enum OutputDirection
	{
		TOP, BACK, BOTTOM;

		private static final Map<String, OutputDirection> directions;

		static {
			final Map<String, OutputDirection> map = new HashMap<>();

			for (final OutputDirection direction : OutputDirection.values()) {
				map.put(direction.name().toLowerCase(Locale.ROOT), direction);
			}

			directions = Map.copyOf(map);
		}

		public static OutputDirection getByName(Optional<String> name) {
			return name.map(s -> directions.get(s.toLowerCase(Locale.ROOT))).orElse(null);
		}
	}
}

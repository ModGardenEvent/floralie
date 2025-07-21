package dev.hephaestus.glowcase.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record CollectableStack(RegistryEntry<Item> item, ComponentChanges changes, int count, boolean collected) {
	public static final Codec<CollectableStack> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Item.ENTRY_CODEC.fieldOf("item").forGetter(CollectableStack::item),
			ComponentChanges.CODEC.fieldOf("components").forGetter(CollectableStack::changes),
			Codec.INT.fieldOf("count").forGetter(CollectableStack::count),
			Codec.BOOL.fieldOf("collected").forGetter(CollectableStack::collected)
		).apply(instance, CollectableStack::new)
	);

	public ItemStack getStack() {
		return new ItemStack(item, count, changes);
	}

	public MutableText getCollectableName(RegistryWrapper.WrapperLookup lookup, boolean selected) {
		ItemStack stack = getStack();
		Text name = stack.getName();
		JukeboxPlayableComponent songComponent = stack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
		if (songComponent != null) {
			JukeboxSong song = songComponent.song().resolveEntry(lookup).map(RegistryEntry::value).orElse(null);
			if (song != null) {
				name = song.description();
			}
		}
		return Text.literal("%s %dx ".formatted(selected ? ">" : "-", count)).append(name).formatted(collected ? Formatting.AQUA : Formatting.GRAY).styled(selected ? s -> s.withBold(true) : s -> s);
	}

	public CollectableStack asCollected() {
		return new CollectableStack(item, changes, count, true);
	}

	public CollectableStack asRetrieved() {
		return new CollectableStack(item, changes, count, false);
	}
}

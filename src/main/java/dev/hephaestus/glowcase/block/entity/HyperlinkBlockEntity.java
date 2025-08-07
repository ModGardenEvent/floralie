package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class HyperlinkBlockEntity extends GlowcaseBlockEntity {
	public static final int TITLE_MAX_LENGTH = 1024;
	public static final int URL_MAX_LENGTH = 1024;
	private String title = "";
	private String url = "";

	public HyperlinkBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.HYPERLINK_BLOCK_ENTITY.get(), pos, state);
	}

	public String getText() {
		return !title.isEmpty() ? title : url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String newTitle) {
		title = newTitle;
		markDirty();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String newUrl) {
		url = newUrl;
		markDirty();
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);
		tag.putString("title", this.title);
		tag.putString("url", this.url);
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);
		this.title = tag.getString("title", "")
		this.url = tag.getString("url", "")
	}
}

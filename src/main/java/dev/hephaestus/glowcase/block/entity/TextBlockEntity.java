package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.client.util.ColorUtil;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TextBlockEntity extends GlowcaseBlockEntity {
	public static final NodeParser PARSER = TagParser.DEFAULT;

	public static final int PLATE_BACKGROUND = 0x44000000;

	public List<Text> lines = new ArrayList<>();
	public TextAlignment textAlignment = TextAlignment.CENTER;
	public ZOffset zOffset = ZOffset.CENTER;
	public boolean shadow = true;
	public float scale = 1F;
	public int color = ColorUtil.WHITE;
	public int backgroundColor = 0;
	public boolean renderDirty = true;
	public float viewDistance = -1.0F;

	public TextBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.TEXT_BLOCK_ENTITY.get(), pos, state);
		lines.add(Text.empty());
	}

	@Override
	protected void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		tag.putFloat("scale", this.scale);
		tag.putInt("color", this.color);
		tag.putInt("background_color", this.backgroundColor);

		tag.putString("text_alignment", this.textAlignment.name());
		tag.putString("z_offset", this.zOffset.name());
		tag.putBoolean("shadow", this.shadow);
		tag.putFloat("viewDistance", this.viewDistance);

		NbtList lines = tag.getListOrEmpty("lines");
		for (var text : this.lines) {
			lines.add(NbtString.of(Text.Serialization.toJsonString(text, registryLookup)));
		}

		tag.put("lines", lines);
	}

	@Override
	protected void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);

		this.scale = tag.getFloat("scale", 1);
		this.color = tag.getInt("color", 0xFFFFFFFF);

		// Force-fix alpha of 0 to opaque.
		if ((this.color & ColorUtil.ALPHA_MASK) == 0) {
			this.color |= ColorUtil.ALPHA_MASK;
		}

		this.backgroundColor = tag.getInt("background_color", 0);
		this.shadow = tag.getBoolean("shadow", true);
		this.textAlignment = TextAlignment.valueOf(tag.getString("text_alignment", "0"));
		this.zOffset = ZOffset.valueOf(tag.getString("z_offset", "0"));
		this.viewDistance = tag.getFloat("viewDistance", -1.0F)

		this.lines = new ArrayList<>();
		NbtList lines = tag.getListOrEmpty("lines");

		for (NbtElement line : lines) {
			if (line.getType() != NbtElement.STRING_TYPE) break;
			this.lines.add(Text.Serialization.fromJson(line.asString().orElseThrow(), registryLookup));
		}

		this.renderDirty = true;
	}

	public String getRawLine(int i) {
		var line = this.lines.get(i);

		if (line.getStyle() == null) {
			return line.getString();
		}

		var insert = line.getStyle().getInsertion();

		if (insert == null) {
			return line.getString();
		}
		return insert;
	}

	public void addRawLine(int i, String string) {
		var parsed = PARSER.parseText(string, ParserContext.of());

		if (parsed.getString().equals(string)) {
			this.lines.add(i, Text.literal(string));
		} else {
			this.lines.add(i, Text.empty().append(parsed).setStyle(Style.EMPTY.withInsertion(string)));
		}
	}

	public void setRawLine(int i, String string) {
		var parsed = PARSER.parseText(string, ParserContext.of());

		if (parsed.getString().equals(string)) {
			this.lines.set(i, Text.literal(string));
		} else {
			this.lines.set(i, Text.empty().append(parsed).setStyle(Style.EMPTY.withInsertion(string)));
		}
	}

	public enum TextAlignment implements StringIdentifiable {
		LEFT, CENTER, CENTER_LEFT, CENTER_RIGHT, RIGHT;

		public static final Codec<TextAlignment> CODEC = StringIdentifiable.createCodec(TextAlignment::values);

		@Override
		public String asString() {
			return name().toLowerCase();
		}
	}

	public enum ZOffset implements StringIdentifiable {
		FRONT, CENTER, BACK;

		public static final Codec<ZOffset> CODEC = StringIdentifiable.createCodec(ZOffset::values);

		@Override
		public String asString() {
			return name().toLowerCase();
		}
	}
}

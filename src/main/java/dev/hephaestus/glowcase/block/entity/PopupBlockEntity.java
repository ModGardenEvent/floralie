package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
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

public class PopupBlockEntity extends GlowcaseBlockEntity {
	public static final NodeParser PARSER = TagParser.DEFAULT;
	public String title = "";
	public List<Text> lines = new ArrayList<>();
	public TextBlockEntity.TextAlignment textAlignment = TextBlockEntity.TextAlignment.CENTER;
	public int color = 0xFFFFFF;
	public boolean renderDirty = true;

	public PopupBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.POPUP_BLOCK_ENTITY.get(), pos, state);
		lines.add(Text.empty());
	}

	@Override
	protected void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		tag.putString("title", this.title);
		tag.putInt("color", this.color);

		tag.putString("text_alignment", this.textAlignment.name());

		NbtList lines = tag.getListOrEmpty("lines");
		for (var text : this.lines) {
			lines.add(NbtString.of(Text.Serialization.toJsonString(text, registryLookup)));
		}

		tag.put("lines", lines);
	}

	@Override
	protected void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);

		tag.getString("title").ifPresent(s -> this.title = s);
		this.lines = new ArrayList<>();
		tag.getInt("color").ifPresent(i -> this.color = i);

		tag.getString("text_alignment").ifPresent(s -> this.textAlignment = TextBlockEntity.TextAlignment.valueOf(s));

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
}

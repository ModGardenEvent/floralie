package dev.hephaestus.glowcase.client.gui.screen.ingame;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.hephaestus.glowcase.client.gui.widget.ingame.ColorPickerWidget;
import eu.pb4.placeholders.api.parsers.tag.TagRegistry;
import eu.pb4.placeholders.api.parsers.tag.TextTag;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.Arrays;
import java.util.Comparator;

public abstract class TextEditorScreen extends GlowcaseScreen implements ColorPickerIncludedScreen {
	private ButtonWidget colorText;
	private ButtonWidget[] widgets = new ButtonWidget[0];

	abstract SelectionManager getSelectionManager();

	protected void addFormattingButtons(int x, int y, int innerPadding, int buttonSize, int buttonPadding) {
		int buttonX = x + innerPadding * 2; //adding numbers to this variable because I personally find that more readable, that's all
		int buttonY = y + innerPadding; //reduce the times this is calculated
		ButtonWidget boldText = ButtonWidget.builder(Text.literal("B").formatted(Formatting.BOLD), action -> {
			insertTag(TagRegistry.SAFE.getTag("bold"), true);
		}).dimensions(buttonX, buttonY, buttonSize, buttonSize).build();

		buttonX += buttonSize + buttonPadding;
		ButtonWidget italicizeText = ButtonWidget.builder(Text.literal("I").formatted(Formatting.ITALIC), action -> {
			insertTag(TagRegistry.SAFE.getTag("italic"), true);
		}).dimensions(buttonX, buttonY, buttonSize, buttonSize).build();

		buttonX += buttonSize + buttonPadding;
		ButtonWidget strikeText = ButtonWidget.builder(Text.literal("S").formatted(Formatting.STRIKETHROUGH), action -> {
			insertTag(TagRegistry.SAFE.getTag("strikethrough"), true);
		}).dimensions(buttonX, buttonY, buttonSize, buttonSize).build();

		buttonX += buttonSize + buttonPadding;
		ButtonWidget underlineText = ButtonWidget.builder(Text.literal("U").formatted(Formatting.UNDERLINE), action -> {
			insertTag(TagRegistry.SAFE.getTag("underline"), true);
		}).dimensions(buttonX, buttonY, buttonSize, buttonSize).build();

		buttonX += buttonSize + buttonPadding;
		//not using the actual obfuscated formatting here because the movement can be annoying
		ButtonWidget obfuscateText = ButtonWidget.builder(Text.literal("@"), action -> {
			insertTag(TagRegistry.SAFE.getTag("obfuscated"), true);
		}).dimensions(buttonX, buttonY, buttonSize, buttonSize).build();

		buttonX += buttonSize + buttonPadding; // + 4? (only works on padding of 2)
		this.colorText = ButtonWidget.builder(Text.literal("\uD83D\uDD8C"), action -> {
			ColorPickerWidget colorPickerWidget = colorPickerWidget();
			colorPickerWidget.setPosition(216, 10);
			colorPickerWidget.setTargetElement(this.colorText);
			colorPickerWidget.setOnAccept(picker -> {
				picker.insertColor(picker.color);
				picker.toggle(false);
			});
			colorPickerWidget.setOnCancel(picker -> picker.toggle(false));
			colorPickerWidget.setPresetListener((color, formatting) -> {
				if(formatting != null) {
					insertFormattingTag(formatting);
				} else {
					insertHexTag(ColorPickerWidget.getHexCode(color));
				}
				this.toggleColorPicker(false);
			});
			colorPickerWidget.setChangeListener(null);
			toggleColorPicker(!colorPickerWidget.active);
		}).dimensions(buttonX, buttonY, buttonSize, buttonSize).build();

		widgets = new ButtonWidget[]{
			boldText, italicizeText, strikeText, underlineText, obfuscateText, colorText
		};

		this.addDrawableChild(boldText);
		this.addDrawableChild(italicizeText);
		this.addDrawableChild(strikeText);
		this.addDrawableChild(underlineText);
		this.addDrawableChild(obfuscateText);
		this.addDrawableChild(colorText);
	}

	public void toggleWidgets(boolean active) {
		for (ButtonWidget widget : widgets)
			widget.active = active;
	}

	public void insertTag(TextTag tag, boolean findShortest) {
		if(tag == null) return;
		//find the alias with the least amount of characters
		String name = tag.name();
		if(findShortest && tag.aliases().length > 1) {
			String shortest = Arrays.stream(tag.aliases()).min(Comparator.comparing(String::length)).get();
			name = Arrays.stream(tag.aliases()).min(Comparator.comparing(String::length)).get();
		}

		SelectionManager selectionManager = getSelectionManager();

		int selectedStart = selectionManager.getSelectionStart();
		int selectedEnd = selectionManager.getSelectionEnd();
		if(selectedStart != selectedEnd) {
			int selectedAmount = Math.abs(selectedEnd - selectedStart);
			//text is selected/highlighted - selection is determined based on the direction it happens, so an extra check is needed
			selectionManager.moveCursor(selectedStart < selectedEnd ? 0 : -selectedAmount, false, SelectionManager.SelectionType.CHARACTER);
			selectionManager.insert("<" + name + ">");
			selectionManager.moveCursor(selectedAmount, false, SelectionManager.SelectionType.CHARACTER);
			selectionManager.insert("</" + name + ">");
			selectionManager.moveCursor(-name.length() - 3, false, SelectionManager.SelectionType.CHARACTER);
			selectionManager.setSelection(selectedStart + name.length() + 2, selectedEnd + name.length() + 2);
		} else {
			selectionManager.insert("<" + name + "></" + name + ">");
			selectionManager.moveCursor(-name.length() - 3, false, SelectionManager.SelectionType.CHARACTER);
		}
	}

	@Override
	public void insertHexTag(String hex) {
		SelectionManager selectionManager = getSelectionManager();
		int selectedStart = selectionManager.getSelectionStart();
		int selectedEnd = selectionManager.getSelectionEnd();
		if(selectedStart != selectedEnd) {
			int selectedAmount = Math.abs(selectedEnd - selectedStart);
			//text is selected/highlighted - selection is determined based on the direction it happens, so an extra check is needed
			selectionManager.moveCursor(selectedStart < selectedEnd ? 0 : -selectedAmount, false, SelectionManager.SelectionType.CHARACTER);
			selectionManager.insert("<" + hex + ">");
			selectionManager.moveCursor(selectedAmount, false, SelectionManager.SelectionType.CHARACTER);
			selectionManager.insert("</" + hex + ">");
			selectionManager.moveCursor(-hex.length() - 3, false, SelectionManager.SelectionType.CHARACTER);
			selectionManager.setSelection(selectedStart + hex.length() + 2, selectedEnd + hex.length() + 2);
		} else {
			selectionManager.insert("<" + hex + "></" + hex + ">");
			selectionManager.moveCursor(-hex.length() - 3, false, SelectionManager.SelectionType.CHARACTER);
		}
	}

	@Override
	public void insertFormattingTag(Formatting formatting) {
		insertTag(TagRegistry.SAFE.getTag(formatting.getName()), false);
	}
}

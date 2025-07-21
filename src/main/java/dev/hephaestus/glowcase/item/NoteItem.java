package dev.hephaestus.glowcase.item;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.item.component.NoteComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class NoteItem extends Item {
	public NoteItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		ItemStack stackInHand = user.getStackInHand(hand);

		NoteComponent noteComponent = stackInHand.get(Glowcase.NOTE_COMPONENT.get());

		// Only edit when not signed
		if (noteComponent != null && noteComponent.title().isPresent())
			return ActionResult.PASS;

		if (world.isClient())
			Glowcase.proxy.openNoteEditScreen(stackInHand);

		return ActionResult.SUCCESS;
	}

	@Override
	public Text getName(ItemStack stack) {
		if (stack.contains(Glowcase.NOTE_COMPONENT.get())) {
			NoteComponent noteComponent = stack.get(Glowcase.NOTE_COMPONENT.get());
			assert noteComponent != null;
			if (noteComponent.title().isPresent())
				return Text.literal(noteComponent.title().get()).setStyle(Style.EMPTY.withItalic(true));
		}
		return super.getName(stack);
	}

	@Override
	public void appendTooltip(ItemStack itemStack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
		boolean signed = false;

		if (itemStack.contains(Glowcase.NOTE_COMPONENT.get())) {
			NoteComponent noteComponent = itemStack.get(Glowcase.NOTE_COMPONENT.get());
			assert noteComponent != null;

			if (noteComponent.title().isPresent()) {
				signed = true;
				Text author = (noteComponent.author().isPresent()) ? Text.literal(noteComponent.author().get()) : Text.translatable("gui.glowcase.note.anonymous").formatted(Formatting.WHITE);

				textConsumer.accept(Text.translatable("item.glowcase.note.tooltip.0", author).formatted(Formatting.YELLOW));
			}
		}

		if (!signed) {
			textConsumer.accept(Text.translatable("item.glowcase.note.tooltip.1").formatted(Formatting.GRAY));
		}
	}
}

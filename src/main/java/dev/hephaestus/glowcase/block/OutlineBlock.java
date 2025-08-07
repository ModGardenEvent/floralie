package dev.hephaestus.glowcase.block;

import com.mojang.serialization.MapCodec;
import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.OutlineBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class OutlineBlock extends WaterloggableGlowcaseBlock {
	public static final MapCodec<OutlineBlock> CODEC = createCodec(OutlineBlock::new);

	public OutlineBlock(AbstractBlock.Settings settings) {
		super(settings);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new OutlineBlockEntity(pos, state);
	}

	@Override
	protected boolean openEditScreen(BlockPos pos) {
		Glowcase.proxy.openOutlineBlockEditScreen(pos);
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
		textConsumer.accept(Text.translatable("block.glowcase.outline_block.tooltip.0").formatted(Formatting.GRAY));
		textConsumer.accept(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}
}

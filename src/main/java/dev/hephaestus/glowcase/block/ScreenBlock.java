package dev.hephaestus.glowcase.block;

import com.mojang.serialization.MapCodec;
import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.ScreenBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ScreenBlock extends RotatableBlock {
	public static final MapCodec<ScreenBlock> CODEC = createCodec(ScreenBlock::new);

	public ScreenBlock(AbstractBlock.Settings settings) {
		super(settings);
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ScreenBlockEntity(pos, state);
	}

	@Override
	protected boolean openEditScreen(BlockPos pos) {
		Glowcase.proxy.openScreenBlockEditScreen(pos);
		return true;
	}

	@Override
	boolean canTarget(PlayerEntity player, BlockPos pos) {
		return super.canTarget(player, pos) || player.getMainHandStack().isOf(Glowcase.TABLET_ITEM.get());
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
		textConsumer.accept(Text.translatable("block.glowcase.screen_block.tooltip.0").formatted(Formatting.GRAY));
		textConsumer.accept(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}
}

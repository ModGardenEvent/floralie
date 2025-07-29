package dev.hephaestus.glowcase.block;

import com.mojang.serialization.MapCodec;
import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.SpriteBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;

import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class SpriteBlock extends WaterloggableGlowcaseBlock {
	public static final MapCodec<SpriteBlock> CODEC = createCodec(SpriteBlock::new);
	public static final EnumProperty<Direction> FACING = Properties.FACING;


	public SpriteBlock() {
		this(defaultSettings());
	}

	public SpriteBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(FACING, Direction.UP));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(FACING);
	}

	@Override
	protected boolean openEditScreen(BlockPos pos) {
		Glowcase.proxy.openSpriteBlockEditScreen(pos);
		return true;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getSide());
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		loadClientSideNBT(world, pos, placer, stack);
		if (placer != null && world.getBlockEntity(pos) instanceof SpriteBlockEntity be) {
			if (state.get(FACING).equals(Direction.UP)) {
				be.setRotation(Math.round(((540.0F + placer.getHeadYaw()) % 360.0F) / 45.0F) * 45);
			}
			if (state.get(FACING).equals(Direction.DOWN)) {
				be.setRotation(Math.round(((540.0F - placer.getHeadYaw()) % 360.0F) / 45.0F) * 45);
			}
		}
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new SpriteBlockEntity(pos, state);
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
		textConsumer.accept(Text.translatable("block.glowcase.sprite_block.tooltip.0").formatted(Formatting.GRAY));
		textConsumer.accept(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
		textConsumer.accept(Text.translatable("block.glowcase.sprite_block.tooltip.1").formatted(Formatting.DARK_GRAY));
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}
}

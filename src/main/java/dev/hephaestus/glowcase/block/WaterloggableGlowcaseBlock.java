package dev.hephaestus.glowcase.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ampflower
 **/
public abstract class WaterloggableGlowcaseBlock extends GlowcaseBlock implements Waterloggable {
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public WaterloggableGlowcaseBlock() {
		this(defaultSettings());
	}

	public WaterloggableGlowcaseBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
	}

	@Override
	protected boolean isTransparent(BlockState state) {
		return state.getFluidState().isEmpty();
	}

	@Override
	protected BlockState getStateForNeighborUpdate(
		final BlockState state,
		final WorldView world,
		final ScheduledTickView tickView,
		final BlockPos pos,
		final Direction direction,
		final BlockPos neighborPos,
		final BlockState neighborState,
		final Random random
	) {
		if (state.get(WATERLOGGED)) {
			tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected FluidState getFluidState(final BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public @Nullable BlockState getPlacementState(final ItemPlacementContext ctx) {
		return getDefaultState().with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
	}

	@Override
	protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(WATERLOGGED);
	}

	@Override
	public ItemStack tryDrainFluid(
		@Nullable final LivingEntity drainer,
		final WorldAccess world,
		final BlockPos pos,
		final BlockState state
	) {
		if (drainer instanceof PlayerEntity player && !GlowcaseBlock.canEditGlowcase(player, pos)) {
			return ItemStack.EMPTY;
		}
		return Waterloggable.super.tryDrainFluid(drainer, world, pos, state);
	}

	@Override
	public boolean canFillWithFluid(
		@Nullable final LivingEntity drainer,
		final BlockView world,
		final BlockPos pos,
		final BlockState state,
		final Fluid fluid
	) {
		return drainer instanceof PlayerEntity player && GlowcaseBlock.canEditGlowcase(player, pos) && Waterloggable.super.canFillWithFluid(drainer, world, pos, state, fluid);
	}
}

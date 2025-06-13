package dev.hephaestus.glowcase.block;

import com.mojang.serialization.MapCodec;
import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.SoundPlayerBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoundPlayerBlock extends WaterloggableGlowcaseBlock {
	public static final MapCodec<SoundPlayerBlock> CODEC = createCodec(SoundPlayerBlock::new);

	public SoundPlayerBlock() {
		super();
	}

	public SoundPlayerBlock(AbstractBlock.Settings settings) {
		super(settings);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		if (!world.isClient()) return null;
		return checkType(type, Glowcase.SOUND_BLOCK_ENTITY.get(), SoundPlayerBlockEntity::clientTick);
	}

	@Override
	protected boolean openEditScreen(BlockPos pos) {
		Glowcase.proxy.openSoundBlockEditScreen(pos);
		return true;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new SoundPlayerBlockEntity(pos, state);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}
}

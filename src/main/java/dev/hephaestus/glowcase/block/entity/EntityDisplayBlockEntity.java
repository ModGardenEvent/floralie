package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class EntityDisplayBlockEntity extends DisplayBlockEntity implements StackInteractable {
	public static final TagKey<EntityType<?>> TICK = TagKey.of(RegistryKeys.ENTITY_TYPE, Glowcase.id("tick_in_display"));

	protected Entity displayEntity = null;
	protected EntityType<?> entityType = null;

	public EntityDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.ENTITY_DISPLAY_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public boolean matchesStack(ItemStack stack) {
		return (stack.isEmpty() && displayEntity == null) || (stack.getItem() instanceof SpawnEggItem eggItem && eggItem.isOfSameEntityType(stack, entityType));
	}

	@Override
	public void setFromStack(ItemStack stack) {
		if (stack.getItem() instanceof SpawnEggItem eggItem) {
			setDisplayEntity(eggItem.getEntityType(world.getRegistryManager(), stack).create(world, SpawnReason.SPAWN_ITEM_USE));
			setScale(new Vector3f(Math.clamp(Math.round(Math.min(1F / displayEntity.getHeight(), 1F / displayEntity.getWidth()) * 8F) / 8F, 0.125F, 10F)));
		}
	}

	@Override
	public void unsetFromStack() {
		setDisplayEntity(null);
	}

	public Entity getDisplayEntity() {
		return this.displayEntity;
	}

	public void setDisplayEntity(Entity displayEntity) {
		this.displayEntity = displayEntity;
		this.entityType = displayEntity == null ? null : displayEntity.getType();
		this.markDirty();
	}

	public static void tick(World world, BlockPos blockPos, BlockState state, EntityDisplayBlockEntity blockEntity) {
		if (blockEntity.displayEntity == null && blockEntity.entityType != null) {
			blockEntity.setDisplayEntity(blockEntity.entityType.create(world, SpawnReason.LOAD));
		}
		if (blockEntity.getDisplayEntity() != null && blockEntity.getDisplayEntity().getType().isIn(TICK)) {
			++blockEntity.displayEntity.age;
		}
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);
		if (entityType != null) tag.putString("type", Registries.ENTITY_TYPE.getId(entityType).toString());
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);
		this.entityType = tag.contains("type") ? Registries.ENTITY_TYPE.get(Identifier.tryParse(tag.getString("type", ""))) : null;
		this.displayEntity = null;
	}
}

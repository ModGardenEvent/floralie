package dev.hephaestus.glowcase.block.entity;

import de.crafty.eiv.common.overlay.ItemViewOverlay;
import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.client.GlowcaseClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class RecipeBlockEntity extends GlowcaseBlockEntity {
	public String recipe = "diamond_sword";
	public TextBlockEntity.ZOffset zOffset = TextBlockEntity.ZOffset.CENTER;

	//TODO: maybe move XYZ rotation to nbt? or use vec2f
	public float rotationX = 0f;
	public float rotationY = 0f;

	public RecipeBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.RECIPE_BLOCK_ENTITY.get(), pos, state);
	}

	@Environment(EnvType.CLIENT)
	public void openRecipe() {
		Identifier rid = Identifier.tryParse(recipe);
//		if (GlowcaseClient.EMI_LOADED) {
//			EmiClientUtils.displayRecipe(rid);
//		}
		if (GlowcaseClient.EIV_LOADED) {
			ItemStack itemStack = Registries.ITEM.get(rid).getDefaultStack();
			ItemViewOverlay.INSTANCE.openRecipeView(itemStack, ItemViewOverlay.ItemViewOpenType.RESULT);
		}
	}

	public void setRecipe(String newRecipe) {
		recipe = newRecipe;
		markDirty();
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		tag.putString("recipe", this.recipe);
		tag.putString("z_offset", this.zOffset.name());
		tag.putFloat("rotationX", this.rotationX);
		tag.putFloat("rotationY", this.rotationY);
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);

		this.recipe = tag.getString("recipe", "diamond_sword");
		this.zOffset = TextBlockEntity.ZOffset.valueOf(tag.getString("z_offset", "center"));
		this.rotationX = tag.getFloat("rotationX", 0);
		this.rotationY = tag.getFloat("rotationY", 0);
	}
}

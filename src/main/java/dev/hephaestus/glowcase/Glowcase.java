package dev.hephaestus.glowcase;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import dev.hephaestus.glowcase.block.ConfigLinkBlock;
import dev.hephaestus.glowcase.block.EntityDisplayBlock;
import dev.hephaestus.glowcase.block.HyperlinkBlock;
import dev.hephaestus.glowcase.block.ItemAcceptorBlock;
import dev.hephaestus.glowcase.block.ItemDisplayBlock;
import dev.hephaestus.glowcase.block.ItemProviderBlock;
import dev.hephaestus.glowcase.block.OutlineBlock;
import dev.hephaestus.glowcase.block.ParticleDisplayBlock;
import dev.hephaestus.glowcase.block.PopupBlock;
import dev.hephaestus.glowcase.block.RecipeBlock;
import dev.hephaestus.glowcase.block.ScreenBlock;
import dev.hephaestus.glowcase.block.SoundPlayerBlock;
import dev.hephaestus.glowcase.block.SpriteBlock;
import dev.hephaestus.glowcase.block.TextBlock;
import dev.hephaestus.glowcase.block.entity.ConfigLinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.EntityDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemAcceptorBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemProviderBlockEntity;
import dev.hephaestus.glowcase.block.entity.OutlineBlockEntity;
import dev.hephaestus.glowcase.block.entity.ParticleDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.PopupBlockEntity;
import dev.hephaestus.glowcase.block.entity.RecipeBlockEntity;
import dev.hephaestus.glowcase.block.entity.ScreenBlockEntity;
import dev.hephaestus.glowcase.block.entity.SoundPlayerBlockEntity;
import dev.hephaestus.glowcase.block.entity.SpriteBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.compat.PolydexCompatibility;
import dev.hephaestus.glowcase.item.CollectionCaseItem;
import dev.hephaestus.glowcase.item.LockItem;
import dev.hephaestus.glowcase.item.NoteItem;
import dev.hephaestus.glowcase.item.TabletItem;
import dev.hephaestus.glowcase.item.component.CollectionComponent;
import dev.hephaestus.glowcase.item.component.LockComponent;
import dev.hephaestus.glowcase.item.component.NoteComponent;
import dev.hephaestus.glowcase.item.component.TabletComponents;
import dev.hephaestus.glowcase.util.CollectableStack;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Glowcase implements ModInitializer {
	public static final String MODID = "glowcase";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final GlowcaseConfig CONFIG = GlowcaseConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", MODID, GlowcaseConfig.class);
	public static GlowcaseCommonProxy proxy = new GlowcaseCommonProxy(); //Overridden in GlowcaseClient

	public static final TagKey<Item> ITEM_TAG = TagKey.of(RegistryKeys.ITEM, id("items"));

	public static final Supplier<HyperlinkBlock> HYPERLINK_BLOCK = registerBlock("hyperlink_block", HyperlinkBlock::new);
	public static final Supplier<BlockItem> HYPERLINK_BLOCK_ITEM = registerItem("hyperlink_block", () -> new BlockItem(HYPERLINK_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<HyperlinkBlockEntity>> HYPERLINK_BLOCK_ENTITY = registerBlockEntity("hyperlink_block", () -> FabricBlockEntityTypeBuilder.create(HyperlinkBlockEntity::new, HYPERLINK_BLOCK.get()).build(null));

	public static final Supplier<ConfigLinkBlock> CONFIG_LINK_BLOCK = registerBlock("config_link_block", ConfigLinkBlock::new);
	public static final Supplier<BlockItem> CONFIG_LINK_BLOCK_ITEM = registerItem("config_link_block", () -> new BlockItem(CONFIG_LINK_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<ConfigLinkBlockEntity>> CONFIG_LINK_BLOCK_ENTITY = registerBlockEntity("config_link_block", () -> FabricBlockEntityTypeBuilder.create(ConfigLinkBlockEntity::new, CONFIG_LINK_BLOCK.get()).build(null));

	public static final Supplier<ItemDisplayBlock> ITEM_DISPLAY_BLOCK = registerBlock("item_display_block", ItemDisplayBlock::new);
	public static final Supplier<BlockItem> ITEM_DISPLAY_BLOCK_ITEM = registerItem("item_display_block", () -> new BlockItem(ITEM_DISPLAY_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<ItemDisplayBlockEntity>> ITEM_DISPLAY_BLOCK_ENTITY = registerBlockEntity("item_display_block", () -> FabricBlockEntityTypeBuilder.create(ItemDisplayBlockEntity::new, ITEM_DISPLAY_BLOCK.get()).build(null));

	public static final Supplier<ItemProviderBlock> ITEM_PROVIDER_BLOCK = registerBlock("item_provider_block", ItemProviderBlock::new);
	public static final Supplier<BlockItem> ITEM_PROVIDER_BLOCK_ITEM = registerItem("item_provider_block", () -> new BlockItem(ITEM_PROVIDER_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<ItemProviderBlockEntity>> ITEM_PROVIDER_BLOCK_ENTITY = registerBlockEntity("item_provider_block", () -> FabricBlockEntityTypeBuilder.create(ItemProviderBlockEntity::new, ITEM_PROVIDER_BLOCK.get()).build(null));

	public static final Supplier<ParticleDisplayBlock> PARTICLE_DISPLAY = registerBlock("particle_display", ParticleDisplayBlock::new);
	public static final Supplier<BlockItem> PARTICLE_DISPLAY_ITEM = registerItem("particle_display", () -> new BlockItem(PARTICLE_DISPLAY.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<ParticleDisplayBlockEntity>> PARTICLE_DISPLAY_BLOCK_ENTITY = registerBlockEntity("particle_display", () -> FabricBlockEntityTypeBuilder.create(ParticleDisplayBlockEntity::new, PARTICLE_DISPLAY.get()).build(null));

	public static final Supplier<SoundPlayerBlock> SOUND_BLOCK = registerBlock("sound_block", SoundPlayerBlock::new);
	public static final Supplier<BlockItem> SOUND_BLOCK_ITEM = registerItem("sound_block", () -> new BlockItem(SOUND_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<SoundPlayerBlockEntity>> SOUND_BLOCK_ENTITY = registerBlockEntity("sound_block", () -> FabricBlockEntityTypeBuilder.create(SoundPlayerBlockEntity::new, SOUND_BLOCK.get()).build(null));

	public static final Supplier<TextBlock> TEXT_BLOCK = registerBlock("text_block", TextBlock::new);
	public static final Supplier<BlockItem> TEXT_BLOCK_ITEM = registerItem("text_block", () -> new BlockItem(TEXT_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<TextBlockEntity>> TEXT_BLOCK_ENTITY = registerBlockEntity("text_block", () -> FabricBlockEntityTypeBuilder.create(TextBlockEntity::new, TEXT_BLOCK.get()).build(null));

	public static final Supplier<PopupBlock> POPUP_BLOCK = registerBlock("popup_block", PopupBlock::new);
	public static final Supplier<BlockItem> POPUP_BLOCK_ITEM = registerItem("popup_block", () -> new BlockItem(POPUP_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<PopupBlockEntity>> POPUP_BLOCK_ENTITY = registerBlockEntity("popup_block", () -> FabricBlockEntityTypeBuilder.create(PopupBlockEntity::new, POPUP_BLOCK.get()).build(null));

	public static final Supplier<ScreenBlock> SCREEN_BLOCK = registerBlock("screen_block", ScreenBlock::new);
	public static final Supplier<BlockItem> SCREEN_BLOCK_ITEM = registerItem("screen_block", () -> new BlockItem(SCREEN_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<ScreenBlockEntity>> SCREEN_BLOCK_ENTITY = registerBlockEntity("screen_block", () -> FabricBlockEntityTypeBuilder.create(ScreenBlockEntity::new, SCREEN_BLOCK.get()).build(null));

	public static final Supplier<SpriteBlock> SPRITE_BLOCK = registerBlock("sprite_block", SpriteBlock::new);
	public static final Supplier<BlockItem> SPRITE_BLOCK_ITEM = registerItem("sprite_block", () -> new BlockItem(SPRITE_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<SpriteBlockEntity>> SPRITE_BLOCK_ENTITY = registerBlockEntity("sprite_block", () -> FabricBlockEntityTypeBuilder.create(SpriteBlockEntity::new, SPRITE_BLOCK.get()).build(null));

	public static final Supplier<RecipeBlock> RECIPE_BLOCK = registerBlock("recipe_block", RecipeBlock::new);
	public static final Supplier<BlockItem> RECIPE_BLOCK_ITEM = registerItem("recipe_block", () -> new BlockItem(RECIPE_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<RecipeBlockEntity>> RECIPE_BLOCK_ENTITY = registerBlockEntity("recipe_block", () -> FabricBlockEntityTypeBuilder.create(RecipeBlockEntity::new, RECIPE_BLOCK.get()).build(null));

	public static final Supplier<OutlineBlock> OUTLINE_BLOCK = registerBlock("outline_block", OutlineBlock::new);
	public static final Supplier<BlockItem> OUTLINE_BLOCK_ITEM = registerItem("outline_block", () -> new BlockItem(OUTLINE_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<OutlineBlockEntity>> OUTLINE_BLOCK_ENTITY = registerBlockEntity("outline_block", () -> FabricBlockEntityTypeBuilder.create(OutlineBlockEntity::new, OUTLINE_BLOCK.get()).build(null));

	public static final Supplier<ItemAcceptorBlock> ITEM_ACCEPTOR_BLOCK = registerBlock("item_acceptor_block", ItemAcceptorBlock::new);
	public static final Supplier<BlockItem> ITEM_ACCEPTOR_BLOCK_ITEM = registerItem("item_acceptor_block", () -> new BlockItem(ITEM_ACCEPTOR_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<ItemAcceptorBlockEntity>> ITEM_ACCEPTOR_BLOCK_ENTITY = registerBlockEntity("item_acceptor_block", () -> FabricBlockEntityTypeBuilder.create(ItemAcceptorBlockEntity::new, ITEM_ACCEPTOR_BLOCK.get()).build(null));

	public static final Supplier<Item> LOCK_ITEM = registerItem("lock", () -> new LockItem(new Item.Settings().maxCount(1)));
	public static final Supplier<ComponentType<LockComponent>> LOCK_COMPONENT = registerComponent("lock", () -> LockComponent.TYPE);

	public static final Supplier<ComponentType<CollectionComponent>> COLLECTION_COMPONENT = registerComponent("collection", () -> CollectionComponent.TYPE);
	public static final Supplier<Item> COLLECTION_CASE_ITEM = registerItem("collection_case", () -> new CollectionCaseItem(new Item.Settings().maxCount(1).component(COLLECTION_COMPONENT.get(), new CollectionComponent()).component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xFFFFFF))));

	public static final Supplier<Item> TABLET_ITEM = registerItem("tablet", () -> new TabletItem(new Item.Settings().maxCount(1)));
	public static final Supplier<ComponentType<Pair<UUID, BlockPos>>> LINKED_SCREEN_COMPONENT = registerComponent("linked_screen", () -> TabletComponents.LINKED_SCREEN_TYPE);
	public static final Supplier<ComponentType<Integer>> CURRENT_SLIDE_COMPONENT = registerComponent("current_slide", () -> TabletComponents.CURRENT_SLIDE_TYPE);
	public static final Supplier<ComponentType<List<Pair<String, String>>>> SLIDESHOW_COMPONENT = registerComponent("slideshow", () -> TabletComponents.SLIDESHOW_COMPONENT_TYPE);

	public static final Supplier<Item> NOTE_ITEM = registerItem("note", () -> new NoteItem(new Item.Settings().maxCount(1)));
	public static final Supplier<ComponentType<NoteComponent>> NOTE_COMPONENT = registerComponent("note", () -> NoteComponent.TYPE);

	public static final Supplier<EntityDisplayBlock> ENTITY_DISPLAY_BLOCK = registerBlock("entity_display_block", EntityDisplayBlock::new);
	public static final Supplier<BlockItem> ENTITY_DISPLAY_BLOCK_ITEM = registerItem("entity_display_block", () -> new BlockItem(ENTITY_DISPLAY_BLOCK.get(), new Item.Settings().maxCount(1)));
	public static final Supplier<BlockEntityType<EntityDisplayBlockEntity>> ENTITY_DISPLAY_BLOCK_ENTITY = registerBlockEntity("entity_display_block", () -> FabricBlockEntityTypeBuilder.create(EntityDisplayBlockEntity::new, ENTITY_DISPLAY_BLOCK.get()).build(null));

	public static final Supplier<ItemGroup> ITEM_GROUP = registerItemGroup("items", () -> FabricItemGroup.builder()
		.displayName(Text.translatable("itemGroup.glowcase.items"))
		.icon(() -> new ItemStack(SPRITE_BLOCK_ITEM.get()))
		.entries((displayContext, entries) -> {
			entries.add(TEXT_BLOCK_ITEM.get());
			entries.add(ENTITY_DISPLAY_BLOCK_ITEM.get());
			entries.add(ITEM_DISPLAY_BLOCK_ITEM.get());
			entries.add(RECIPE_BLOCK_ITEM.get());
			entries.add(SPRITE_BLOCK_ITEM.get());
			entries.add(PARTICLE_DISPLAY_ITEM.get());
			entries.add(SOUND_BLOCK_ITEM.get());
			entries.add(SCREEN_BLOCK_ITEM.get());
			entries.add(OUTLINE_BLOCK_ITEM.get());
			entries.add(HYPERLINK_BLOCK_ITEM.get());
			entries.add(CONFIG_LINK_BLOCK_ITEM.get());
			entries.add(POPUP_BLOCK_ITEM.get());
			entries.add(ITEM_PROVIDER_BLOCK_ITEM.get());
			entries.add(ITEM_ACCEPTOR_BLOCK_ITEM.get());
			entries.add(LOCK_ITEM.get());
			entries.add(COLLECTION_CASE_ITEM.get());
			entries.add(TABLET_ITEM.get());
			entries.add(NOTE_ITEM.get());
		})
		.build()
	);

	public static Identifier id(String... path) {
		return Identifier.of(MODID, String.join(".", path));
	}

	public static <T extends Block> Supplier<T> registerBlock(String path, Supplier<T> supplier) {
		return Suppliers.ofInstance(Registry.register(Registries.BLOCK, id(path), supplier.get()));
	}

	public static <T extends Item> Supplier<T> registerItem(String path, Supplier<T> supplier) {
		return Suppliers.ofInstance(Registry.register(Registries.ITEM, id(path), supplier.get()));
	}

	public static <T extends ComponentType<U>, U> Supplier<T> registerComponent(String path, Supplier<T> supplier) {
		return Suppliers.ofInstance(Registry.register(Registries.DATA_COMPONENT_TYPE, id(path), supplier.get()));
	}

	public static <T extends ItemGroup> Supplier<T> registerItemGroup(String path, Supplier<T> supplier) {
		return Suppliers.ofInstance(Registry.register(Registries.ITEM_GROUP, id(path), supplier.get()));
	}

	public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String path, Supplier<BlockEntityType<T>> supplier) {
		return Suppliers.ofInstance(Registry.register(Registries.BLOCK_ENTITY_TYPE, id(path), supplier.get()));
	}

	@Override
	public void onInitialize() {
		GlowcaseNetworking.init();

		if (FabricLoader.getInstance().isModLoaded("polydex2")) {
			PolydexCompatibility.onInitialize();
		}

		// Never make this command available outside of dev
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			if (FabricLoader.getInstance().isModLoaded("emi")) {
//				EmiUtils.registerDevCommands();
			}
		}
		
		// Tooltips
		ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {
			if (stack.isOf(ITEM_ACCEPTOR_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.item_acceptor_block.tooltip.0").formatted(
					Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.item_acceptor_block.tooltip.1").formatted(Formatting.BLUE));
				tooltip.add(Text.translatable("block.glowcase.item_acceptor_block.tooltip.2").formatted(Formatting.BLUE));
				tooltip.add(Text.translatable("block.glowcase.item_acceptor_block.tooltip.3").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(SPRITE_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.sprite_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
				tooltip.add(Text.translatable("block.glowcase.sprite_block.tooltip.1").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(SCREEN_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.screen_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(PARTICLE_DISPLAY_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.particle_display_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(SOUND_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.sound_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(ENTITY_DISPLAY_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.entity_display_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.entity_display_block.tooltip.1").formatted(Formatting.DARK_GRAY));
				tooltip.add(Text.translatable("block.glowcase.entity_display_block.tooltip.2").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(ITEM_DISPLAY_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.item_display_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.item_display_block.tooltip.1").formatted(Formatting.DARK_GRAY));
				tooltip.add(Text.translatable("block.glowcase.item_display_block.tooltip.2").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(ITEM_PROVIDER_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.item_provider_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.item_provider_block.tooltip.1").formatted(Formatting.DARK_GRAY));
				tooltip.add(Text.translatable("block.glowcase.item_provider_block.tooltip.2").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(RECIPE_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.recipe_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(HYPERLINK_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.hyperlink_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(CONFIG_LINK_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.config_link_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(OUTLINE_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.outline_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(TEXT_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.text_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
				tooltip.add(Text.translatable("block.glowcase.text_block.tooltip.1").formatted(Formatting.DARK_GRAY));
				NbtComponent component = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
				if (component == null) return;
				NbtCompound nbt = component.getNbt();
				if (nbt == null) return;
				Optional<NbtList> optionalNbtList = nbt.getList("lines");
				if (optionalNbtList.isEmpty()) return;
				NbtList nbtList =  optionalNbtList.get();
				for (NbtElement element : nbtList) {
					if (element instanceof NbtString line) {
						Optional<String> optionalString = line.asString();
						optionalString.ifPresent(string -> tooltip.add(Text.literal((string.length() > 20 ? "%s...\"" : "%s").formatted(
							string.substring(
								0,
								Math.min(string.length(), 20)
							))).formatted(Formatting.DARK_PURPLE)));
					}
				}
			} else if (stack.isOf(POPUP_BLOCK_ITEM.get())) {
				tooltip.add(Text.translatable("block.glowcase.popup_block.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
				tooltip.add(Text.translatable("block.glowcase.popup_block.tooltip.1").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(COLLECTION_CASE_ITEM.get())) {
				CollectionComponent collection = stack.get(Glowcase.COLLECTION_COMPONENT.get());
				tooltip.add(Text.translatable("item.glowcase.collection_case.tooltip.0").formatted(Formatting.GRAY));
				if (type.isCreative()) tooltip.add(Text.translatable("item.glowcase.collection_case.tooltip.creative.0").formatted(Formatting.DARK_GRAY));
				if (collection != null && !collection.collectables().isEmpty()) {
					tooltip.add(Text.translatable("item.glowcase.collection_case.tooltip.1", collection.collected(), collection.collectables().size()).formatted(Formatting.DARK_PURPLE));
					for (int i = 0; i < collection.collectables().size(); i++) {
						CollectableStack collectable = collection.collectables().get(i);
						tooltip.add(collectable.getCollectableName(context.getRegistryLookup(), collection.selected() == i));
					}
				}
			} else if (stack.isOf(TABLET_ITEM.get())) {
				tooltip.add(Text.translatable("item.glowcase.tablet.tooltip.0").formatted(Formatting.GRAY));
				tooltip.add(Text.translatable("item.glowcase.tablet.tooltip.1").formatted(Formatting.DARK_GRAY));
				tooltip.add(Text.translatable("item.glowcase.tablet.tooltip.2").formatted(Formatting.DARK_GRAY));
			} else if (stack.isOf(NOTE_ITEM.get())) {
				boolean signed = false;

				if (stack.contains(Glowcase.NOTE_COMPONENT.get())) {
					NoteComponent noteComponent = stack.get(Glowcase.NOTE_COMPONENT.get());
					assert noteComponent != null;

					if (noteComponent.title().isPresent()) {
						signed = true;
						Text author = (noteComponent.author().isPresent())
							? Text.literal(noteComponent.author().get())
							: Text.translatable("gui.glowcase.note.anonymous").formatted(Formatting.WHITE);

						tooltip.add(Text.translatable("item.glowcase.note.tooltip.0", author).formatted(Formatting.YELLOW));
					}
				}

				if (!signed)
					tooltip.add(Text.translatable("item.glowcase.note.tooltip.1").formatted(Formatting.GRAY));
			} else if (stack.isOf(LOCK_ITEM.get())) {
				tooltip.add(Text.translatable("item.glowcase.lock.tooltip.0").formatted(Formatting.GRAY));
			}
		});
	}
}

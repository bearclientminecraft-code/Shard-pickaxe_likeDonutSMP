package dev.shardpickaxe;

import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class ShardItems {
	public static final String MARKER = "shard_pickaxe";
	public static final String CREATED = "shard_created";
	public static final long LIFETIME_MS = 24L * 60L * 60L * 1000L;

	private static final int PURPLE = 0xAA00FF;
	private static final int GRAY = 0xAAAAAA;
	private static final int RED = 0xFF5555;

	private ShardItems() {
	}

	private static Component line(String text, int color) {
		return Component.literal(text).withStyle(style -> style.withColor(color).withItalic(false));
	}

	public static ItemStack create(HolderLookup.Provider registries) {
		ItemStack stack = new ItemStack(Items.NETHERITE_PICKAXE);

		stack.set(DataComponents.CUSTOM_NAME, line("Shard Pickaxe", PURPLE));

		stack.set(DataComponents.LORE, new ItemLore(List.of(
				line("Breaks 9 Blocks at Once", GRAY),
				line("Silk Touch I", GRAY),
				line("Efficiency V", GRAY),
				line("Unbreaking III", GRAY),
				line("Mending", GRAY),
				line("Self Destruct: 24h", RED),
				line("3K Shards", PURPLE))));

		HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
		EnchantmentHelper.updateEnchantments(stack, mutable -> {
			mutable.set(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
			mutable.set(enchantments.getOrThrow(Enchantments.EFFICIENCY), 5);
			mutable.set(enchantments.getOrThrow(Enchantments.UNBREAKING), 3);
			mutable.set(enchantments.getOrThrow(Enchantments.MENDING), 1);
		});

		LinkedHashSet<DataComponentType<?>> hidden = new LinkedHashSet<>();
		hidden.add(DataComponents.ENCHANTMENTS);
		hidden.add(DataComponents.ATTRIBUTE_MODIFIERS);
		stack.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, hidden));

		CompoundTag tag = new CompoundTag();
		tag.putBoolean(MARKER, true);
		tag.putLong(CREATED, System.currentTimeMillis());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

		return stack;
	}

	public static boolean isShard(ItemStack stack) {
		if (stack.isEmpty() || !stack.is(Items.NETHERITE_PICKAXE)) {
			return false;
		}
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null && data.copyTag().contains(MARKER);
	}

	public static boolean isExpired(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return false;
		}
		long created = data.copyTag().getLongOr(CREATED, 0L);
		return created > 0L && System.currentTimeMillis() - created >= LIFETIME_MS;
	}
      }

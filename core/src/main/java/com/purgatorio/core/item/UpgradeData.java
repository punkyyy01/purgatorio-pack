package com.purgatorio.core.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Nivel de mejora de un item. Vive en el propio objeto (custom_data): es permanente y viaja con el. */
public final class UpgradeData {
	private static final String ROOT = "purgatorio";
	private static final String KEY = "mejora";

	private UpgradeData() {
	}

	public static int get(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return 0;
		}
		return Math.max(0, data.copyTag().getCompoundOrEmpty(ROOT).getIntOr(KEY, 0));
	}

	public static void set(ItemStack stack, int level) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
			CompoundTag root = new CompoundTag();
			root.putInt(KEY, Math.max(0, level));
			tag.put(ROOT, root);
		});
	}
}

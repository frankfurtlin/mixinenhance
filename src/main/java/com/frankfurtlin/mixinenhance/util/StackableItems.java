package com.frankfurtlin.mixinenhance.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

/**
 * 「强制堆叠到 64」的物品判定。
 *
 * <p>凡是能用原版标签/组件判断的一律交给标签/组件，只有零散物品才逐个列出：
 * 原版新增的船、木筏、床、音乐唱片（例如 26.3 新增的 poplar 木船）都会自动生效。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public final class StackableItems {
    /** 没有对应标签/组件、数量又少的零散物品。 */
    private static final Set<Item> EXTRA_STACKABLE = Set.of(
        Items.SADDLE,
        Items.MINECART, Items.CHEST_MINECART, Items.FURNACE_MINECART,
        Items.TNT_MINECART, Items.HOPPER_MINECART,
        Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP,
        Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.POWDER_SNOW_BUCKET,
        Items.MILK_BUCKET, Items.PUFFERFISH_BUCKET, Items.SALMON_BUCKET, Items.COD_BUCKET,
        Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION,
        Items.SPYGLASS, Items.CAKE, Items.TOTEM_OF_UNDYING);

    private StackableItems() {
    }

    /** 该物品堆是否应被强制改为可堆叠到 64。 */
    public static boolean isForceStackable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // 标签驱动：#minecraft:boats 已包含全部船、箱船与木筏；#minecraft:beds 包含全部颜色床
        if (stack.is(ItemTags.BOATS) || stack.is(ItemTags.BEDS)) {
            return true;
        }
        // 组件驱动：所有音乐唱片都带 JUKEBOX_PLAYABLE，新唱片自动生效
        if (stack.get(DataComponents.JUKEBOX_PLAYABLE) != null) {
            return true;
        }
        return EXTRA_STACKABLE.contains(stack.getItem());
    }
}

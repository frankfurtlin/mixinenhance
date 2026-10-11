package com.frankfurtlin.mixinenhance.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 从原版物品标签（tag）中取物品，避免把武器/工具类型写死。
 *
 * <p>好处：原版后续版本或其它模组新增的物品（如铜斧、矛）会被自动包含，无需改代码。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public final class ItemTagUtil {
    private ItemTagUtil() {
    }

    /** 收集若干 tag 的并集物品列表（去重，保持稳定顺序）。 */
    @SafeVarargs
    public static List<Item> itemsInTags(TagKey<Item>... tags) {
        Set<Item> items = new LinkedHashSet<>();
        for (TagKey<Item> tag : tags) {
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                items.add(holder.value());
            }
        }
        return new ArrayList<>(items);
    }

    /**
     * 从若干 tag 的并集中随机取一个物品堆。
     *
     * @return 随机物品堆；若这些 tag 内容为空则返回 {@code null}
     */
    @SafeVarargs
    public static ItemStack randomStack(RandomSource random, TagKey<Item>... tags) {
        List<Item> pool = itemsInTags(tags);
        if (pool.isEmpty()) {
            return null;
        }
        return new ItemStack(pool.get(random.nextInt(pool.size())));
    }
}

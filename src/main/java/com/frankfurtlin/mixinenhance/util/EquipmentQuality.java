package com.frankfurtlin.mixinenhance.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 「怪物装备系数」的取值逻辑：把 0-1 的系数翻译成装备材质档位。
 *
 * <p>材质好坏的依据完全来自原版：</p>
 * <ul>
 *   <li>盔甲：沿用原版 {@code Mob#getEquipmentForSlot} 自己定义的档位阶梯
 *       （皮革 → 铜 → 金 → 锁链 → 铁 → 钻石 → 下界合金），档位数运行时探测，原版加材质会自动跟随。</li>
 *   <li>武器/工具：原版没有统一的武器材质台阶，这里用「同一种武器（剑/斧/镐/锹/锄/矛，按原版 item tag 区分）
 *       按物品耐久排序」来代表材质强弱（金 &lt; 木 &lt; 石 &lt; 铜 &lt; 铁 &lt; 钻石 &lt; 下界合金）。
 *       弓、三叉戟等没有材质变体的武器不在这些 tag 里，因此不会被替换（只受附魔系数影响）。</li>
 * </ul>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public final class EquipmentQuality {
    /** 有材质变体的武器种类（原版物品标签）。 */
    private static final List<TagKey<Item>> WEAPON_KINDS = List.of(
        ItemTags.SWORDS, ItemTags.AXES, ItemTags.PICKAXES, ItemTags.SHOVELS, ItemTags.HOES, ItemTags.SPEARS);

    /** 盔甲档位数缓存（按部位），-1 表示尚未探测。 */
    private static final int[] ARMOR_TIER_CACHE = new int[EquipmentSlot.values().length];

    /** 没有材质变体、但算「武器」的物品（弓/弩/三叉戟）：只受武器系数的概率与附魔影响。 */
    private static final Set<Item> EXTRA_WEAPONS = Set.of(Items.BOW, Items.CROSSBOW, Items.TRIDENT);

    /** 出生阶段标记（finalizeSpawn 期间）：用于区分「出生自带武器」与「后天拾取武器」。 */
    private static final ThreadLocal<Boolean> SPAWNING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    static {
        for (int i = 0; i < ARMOR_TIER_CACHE.length; i++) {
            ARMOR_TIER_CACHE[i] = -1;
        }
    }

    private EquipmentQuality() {
    }

    /**
     * 按系数随机一个档位：系数 0 只会取最低档，系数 1 会在全档位里均匀取，
     * 也就是「系数越大，材质越好的概率越大」。
     *
     * @param tierCount 档位总数（&gt;= 1）
     * @param factor    0-1 的系数
     * @param random01  0-1 的随机数
     */
    public static int randomTier(int tierCount, double factor, double random01) {
        if (tierCount <= 1) {
            return 0;
        }
        int tier = (int) Math.floor(random01 * tierCount * Math.max(0, Math.min(1, factor)));
        return Math.max(0, Math.min(tierCount - 1, tier));
    }

    /** 进入怪物出生流程（finalizeSpawn 开始）。 */
    public static void beginSpawn() {
        SPAWNING.set(Boolean.TRUE);
    }

    /** 离开怪物出生流程（finalizeSpawn 结束）。 */
    public static void endSpawn() {
        SPAWNING.set(Boolean.FALSE);
    }

    /** 当前线程是否处于怪物出生流程中。 */
    public static boolean isSpawning() {
        return SPAWNING.get();
    }

    /** 该物品堆算不算「武器」（决定是否受武器系数的概率/材质影响）。 */
    public static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (TagKey<Item> kind : WEAPON_KINDS) {
            if (stack.is(kind)) {
                return true;
            }
        }
        return EXTRA_WEAPONS.contains(stack.getItem());
    }

    /**
     * 某部位盔甲的档位数：探测原版 {@code Mob#getEquipmentForSlot}，
     * 返回 null（或与上一档重复）即到头。原版新增材质时档位数会自动变大。
     *
     * @return 档位数；该部位没有盔甲（如主手）时返回 0
     */
    public static int armorTierCount(EquipmentSlot slot) {
        int index = slot.ordinal();
        int cached = ARMOR_TIER_CACHE[index];
        if (cached >= 0) {
            return cached;
        }
        Item previous = null;
        int count = 0;
        while (count < 32) {
            Item current = Mob.getEquipmentForSlot(slot, count);
            if (current == null || current == previous) {
                break;
            }
            previous = current;
            count++;
        }
        ARMOR_TIER_CACHE[index] = count;
        return count;
    }

    /**
     * 按系数换一把「同一种武器、不同材质」的武器。
     *
     * @param weapon 原版给出的武器
     * @param factor 武器系数
     * @return 替换后的武器；无需替换（无材质变体 / 没变化）时返回 null
     */
    public static ItemStack betterWeapon(ItemStack weapon, double factor, double random01) {
        if (weapon.isEmpty()) {
            return null;
        }
        for (TagKey<Item> kind : WEAPON_KINDS) {
            if (!weapon.is(kind)) {
                continue;
            }
            List<Item> materials = materialsOf(kind);
            if (materials.size() <= 1) {
                return null;
            }
            Item target = materials.get(randomTier(materials.size(), factor, random01));
            return target == weapon.getItem() ? null : new ItemStack(target);
        }
        // 弓、三叉戟等：没有材质变体，保持原样
        return null;
    }

    /** 某个武器标签里按「材质强弱」（此处用原版耐久）排序后的物品列表。 */
    private static List<Item> materialsOf(TagKey<Item> kind) {
        List<Item> items = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(kind)) {
            items.add(holder.value());
        }
        items.sort(Comparator.comparingInt((Item item) -> new ItemStack(item).getMaxDamage())
            .thenComparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
        return items;
    }
}

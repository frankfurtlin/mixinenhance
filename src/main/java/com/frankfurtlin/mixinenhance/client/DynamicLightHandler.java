package com.frankfurtlin.mixinenhance.client;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态照明（DynamicLights）：生物（玩家及其他生物）手持发光物品/方块时，光照实时跟随其移动照亮周围。
 * 参考 LambDynamicLights 的机制。
 *
 * 关键：26.3 的方块光照烘焙进 chunk mesh，需通过 levelExtractor 标记区块为脏触发重新烘焙。
 * 这里每 tick 标记光源「亮度半径」内的区块 section 为脏（实时跟随），
 * 并通过 lastRanges 跟踪上一 tick 影响范围，把移动后、以及消失（切换非发光物/离开）后的旧范围也标记为脏，
 * 从而正确清除旧光照。
 *
 * 并发说明：update 在客户端 tick 线程执行，getDynamicLightLevel 在渲染线程执行。
 * lightSources 用 volatile 快照，tick 线程整体替换、渲染线程读稳定快照。
 */
public class DynamicLightHandler {
    /** 发光物品 -> 亮度（与原版方块光照等级一致） */
    private static final Map<Item, Integer> LIGHT_ITEMS = createLightItems();

    private static Map<Item, Integer> createLightItems() {
        Map<Item, Integer> map = new HashMap<>();
        map.put(Items.TORCH, 14);
        map.put(Items.LANTERN, 15);
        map.put(Items.GLOWSTONE, 15);
        map.put(Items.SEA_LANTERN, 15);
        map.put(Items.JACK_O_LANTERN, 15);
        map.put(Items.SHROOMLIGHT, 15);
        map.put(Items.GLOW_BERRIES, 14);
        map.put(Items.CAMPFIRE, 15);
        map.put(Items.END_ROD, 14);
        map.put(Items.CONDUIT, 15);
        map.put(Items.OCHRE_FROGLIGHT, 15);
        map.put(Items.PEARLESCENT_FROGLIGHT, 15);
        map.put(Items.VERDANT_FROGLIGHT, 15);
        map.put(Items.SOUL_TORCH, 10);
        map.put(Items.SOUL_LANTERN, 10);
        map.put(Items.SOUL_CAMPFIRE, 10);
        map.put(Items.REDSTONE_TORCH, 7);
        map.put(Items.GLOW_LICHEN, 7);
        return map;
    }

    /** 单个动态光源（位置 + 亮度） */
    private record LightSource(BlockPos pos, int luminance) {
        /** 该光源在 target 处产生的动态光照等级（线性衰减，越远越暗） */
        double getLightLevel(BlockPos target) {
            double dx = target.getX() + 0.5 - (pos.getX() + 0.5);
            double dy = target.getY() + 0.5 - (pos.getY() + 0.5);
            double dz = target.getZ() + 0.5 - (pos.getZ() + 0.5);
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            return luminance - distance;
        }
    }

    /** 光源影响的区块 section 范围（min/max 为 section 坐标） */
    private record LightRange(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {}

    /** 当前帧的动态光源快照（volatile，tick 线程写、渲染线程读） */
    private static volatile List<LightSource> lightSources = List.of();

    /** 每个发光实体上一 tick 的影响范围，用于检测移动/消失并清除旧光照 */
    private static final Map<Integer, LightRange> lastRanges = new HashMap<>();

    /** 每客户端 tick 调用：收集光源、标记影响范围（含移动/消失的旧范围）、原子替换快照。 */
    public static void update(Minecraft client) {
        List<LightSource> newSources = new ArrayList<>();
        Map<Integer, LightRange> currentRanges = new HashMap<>();

        if (MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.enableDynamicLight) {
            ClientLevel level = client.level;
            if (level != null) {
                for (Entity entity : level.entitiesForRendering()) {
                    if (entity instanceof LivingEntity living) {
                        int luminance = getHeldLuminance(living);
                        if (luminance > 0) {
                            BlockPos pos = entity.blockPosition();
                            newSources.add(new LightSource(pos, luminance));
                            LightRange range = computeRange(pos, luminance);
                            currentRanges.put(entity.getId(), range);
                            // 实时跟随：标记当前范围
                            markRangeDirty(client, range);
                        }
                    }
                }
            }
        }

        // 清除旧光照：上一 tick 存在、但这一 tick 消失（切换非发光物/离开）或范围变化（移动）的旧范围
        for (Map.Entry<Integer, LightRange> entry : lastRanges.entrySet()) {
            LightRange old = entry.getValue();
            LightRange current = currentRanges.get(entry.getKey());
            if (current == null || !current.equals(old)) {
                markRangeDirty(client, old);
            }
        }

        lastRanges.clear();
        lastRanges.putAll(currentRanges);
        lightSources = newSources;
    }

    /** 计算光源「亮度半径」影响的区块 section 范围。 */
    private static LightRange computeRange(BlockPos pos, int luminance) {
        int radius = luminance + 1;
        return new LightRange(
            (pos.getX() - radius) >> 4, (pos.getY() - radius) >> 4, (pos.getZ() - radius) >> 4,
            (pos.getX() + radius) >> 4, (pos.getY() + radius) >> 4, (pos.getZ() + radius) >> 4);
    }

    private static void markRangeDirty(Minecraft client, LightRange range) {
        client.levelExtractor.setSectionRangeDirty(range.minX(), range.minY(), range.minZ(), range.maxX(), range.maxY(), range.maxZ());
    }

    /** 计算 pos 处的动态光照等级（取所有光源贡献的最大值，上限 15）。 */
    public static double getDynamicLightLevel(BlockPos pos) {
        double result = 0;
        for (LightSource source : lightSources) {
            double light = source.getLightLevel(pos);
            if (light > result) {
                result = light;
            }
        }
        return Math.min(result, 15);
    }

    private static int getHeldLuminance(LivingEntity entity) {
        int main = getLuminance(entity.getMainHandItem());
        int offhand = getLuminance(entity.getOffhandItem());
        return Math.max(main, offhand);
    }

    private static int getLuminance(ItemStack stack) {
        return stack.isEmpty() ? 0 : LIGHT_ITEMS.getOrDefault(stack.getItem(), 0);
    }
}

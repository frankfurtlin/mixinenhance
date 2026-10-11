package com.frankfurtlin.mixinenhance.mixin;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.frankfurtlin.mixinenhance.config.section.FishingConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * 钓鱼战利品过滤：根据配置逐项排除全部钓鱼战利品（鱼 / 垃圾 / 宝藏，共 21 种）。
 * 拦截 LootTable.getRandomItems，仅在钓鱼战利品（上下文 THIS_ENTITY 为浮标）时过滤，
 * 同时覆盖原版钓鱼与「急速钓鱼」功能（二者都走该 loot 表）。
 */
@Mixin(LootTable.class)
public abstract class LootTableMixin {

    @ModifyReturnValue(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    private ObjectArrayList<ItemStack> mixinEnhance$excludeFishingLoot(ObjectArrayList<ItemStack> original, LootParams lootParams) {
        // 仅对钓鱼战利品生效：其 loot 上下文的 THIS_ENTITY 一定是 FishingHook
        ContextMap contextMap = lootParams.contextMap();
        if (!contextMap.has(LootContextParams.THIS_ENTITY)
                || !(contextMap.get(LootContextParams.THIS_ENTITY) instanceof FishingHook)) {
            return original;
        }
        // 经验模式：不套用战利品过滤。物品已由经验模式统一拦截，
        // 经验按「完整战利品」计算，从而不受排除 xxx 配置影响。
        if (MixinEnhanceClient.getConfig().fishingConfig.enableExpOnlyMode) {
            return original;
        }
        FishingConfig.LootFilterConfig cfg = MixinEnhanceClient.getConfig().fishingConfig.lootFilterConfig;
        if (!cfg.excludeCod && !cfg.excludeSalmon && !cfg.excludeTropicalFish && !cfg.excludePufferfish
                && !cfg.excludeBamboo && !cfg.excludeBowl && !cfg.excludeLeather && !cfg.excludeLeatherBoots
                && !cfg.excludeFishingRod && !cfg.excludeString && !cfg.excludeStick && !cfg.excludePotion
                && !cfg.excludeInkSac && !cfg.excludeRottenFlesh && !cfg.excludeBone && !cfg.excludeTripwireHook
                && !cfg.excludeEnchantedBook && !cfg.excludeNameTag && !cfg.excludeNautilusShell
                && !cfg.excludeSaddle && !cfg.excludeBow) {
            return original;
        }
        ObjectArrayList<ItemStack> filtered = new ObjectArrayList<>(original.size());
        for (ItemStack stack : original) {
            Item item = stack.getItem();
            if (cfg.excludeCod && item == Items.COD) {
                continue;
            }
            if (cfg.excludeSalmon && item == Items.SALMON) {
                continue;
            }
            if (cfg.excludeTropicalFish && item == Items.TROPICAL_FISH) {
                continue;
            }
            if (cfg.excludePufferfish && item == Items.PUFFERFISH) {
                continue;
            }
            if (cfg.excludeBamboo && item == Items.BAMBOO) {
                continue;
            }
            if (cfg.excludeBowl && item == Items.BOWL) {
                continue;
            }
            if (cfg.excludeLeather && item == Items.LEATHER) {
                continue;
            }
            if (cfg.excludeLeatherBoots && item == Items.LEATHER_BOOTS) {
                continue;
            }
            if (cfg.excludeFishingRod && item == Items.FISHING_ROD) {
                continue;
            }
            if (cfg.excludeString && item == Items.STRING) {
                continue;
            }
            if (cfg.excludeStick && item == Items.STICK) {
                continue;
            }
            if (cfg.excludePotion && item == Items.POTION) {
                continue;
            }
            if (cfg.excludeInkSac && item == Items.INK_SAC) {
                continue;
            }
            if (cfg.excludeRottenFlesh && item == Items.ROTTEN_FLESH) {
                continue;
            }
            if (cfg.excludeBone && item == Items.BONE) {
                continue;
            }
            if (cfg.excludeTripwireHook && item == Items.TRIPWIRE_HOOK) {
                continue;
            }
            if (cfg.excludeEnchantedBook && item == Items.ENCHANTED_BOOK) {
                continue;
            }
            if (cfg.excludeNameTag && item == Items.NAME_TAG) {
                continue;
            }
            if (cfg.excludeNautilusShell && item == Items.NAUTILUS_SHELL) {
                continue;
            }
            if (cfg.excludeSaddle && item == Items.SADDLE) {
                continue;
            }
            if (cfg.excludeBow && item == Items.BOW) {
                continue;
            }
            filtered.add(stack);
        }
        return filtered;
    }
}

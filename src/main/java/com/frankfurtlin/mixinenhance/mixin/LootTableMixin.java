package com.frankfurtlin.mixinenhance.mixin;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.frankfurtlin.mixinenhance.config.modMenuMuduleConfig.DefaultModuleConfig;
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
 * 钓鱼战利品过滤：根据配置排除皮革靴子 / 钓鱼竿 / 弓（含附魔）。
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
        DefaultModuleConfig.FishingConfig cfg = MixinEnhanceClient.getConfig().defaultModuleConfig.fishingConfig;
        if (!cfg.excludeLeatherBoots && !cfg.excludeFishingRod && !cfg.excludeBow) {
            return original;
        }
        ObjectArrayList<ItemStack> filtered = new ObjectArrayList<>(original.size());
        for (ItemStack stack : original) {
            Item item = stack.getItem();
            if (cfg.excludeLeatherBoots && item == Items.LEATHER_BOOTS) {
                continue;
            }
            if (cfg.excludeFishingRod && item == Items.FISHING_ROD) {
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

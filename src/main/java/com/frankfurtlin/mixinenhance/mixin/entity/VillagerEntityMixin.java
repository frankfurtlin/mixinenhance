package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 14:12
 */
@Mixin(AbstractVillager.class)
public abstract class VillagerEntityMixin {
    // 每级村民解锁的交易选项数（tradeCount）
    // 26.3 中村民交易改为数据驱动的 TradeSet，每级解锁的交易数由
    // TradeSet.calculateNumberOfTrades 决定（取代原先 updateTrades 里的常量 2）。
    // 该方法在 AbstractVillager.addOffersFromTradeSet 中被调用，故重定向该调用；
    // 仅对村民（Villager）生效，流浪商人（WanderingTrader）保持原逻辑。
    @Redirect(method = "addOffersFromTradeSet", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/trading/TradeSet;calculateNumberOfTrades(Lnet/minecraft/world/level/storage/loot/LootContext;)I"))
    private int addMoreRecipeCount(TradeSet tradeSet, LootContext lootContext) {
        if ((Object) this instanceof Villager) {
            return MixinEnhanceClient.getConfig().defaultModuleConfig.tradeCount;
        }
        return tradeSet.calculateNumberOfTrades(lootContext);
    }
}

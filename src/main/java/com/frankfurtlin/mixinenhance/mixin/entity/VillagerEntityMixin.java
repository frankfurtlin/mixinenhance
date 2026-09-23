package com.frankfurtlin.mixinenhance.mixin.entity;

import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 14:12
 */
@Mixin(Villager.class)
public abstract class VillagerEntityMixin {
    // TODO 每级村民解锁的交易选项数（tradeCount）
    // 26.3 中 updateTrades 改为数据驱动的 addOffersFromTradeSet，不再有常量 2 可供修改，
    // 该特性需针对新的交易集机制重新设计。原实现：
    //   @ModifyConstant(method = "updateTrades", constant = @Constant(intValue = 2))
    //   private int addMoreRecipeCount(int count){ return MixinEnhanceClient.getConfig().defaultModuleConfig.tradeCount; }
}

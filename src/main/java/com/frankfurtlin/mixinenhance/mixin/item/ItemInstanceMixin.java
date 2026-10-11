package com.frankfurtlin.mixinenhance.mixin.item;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.frankfurtlin.mixinenhance.util.StackableItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把部分原版不可堆叠的物品改成可堆叠到 64。
 *
 * <p>26.3 中 {@code getMaxStackSize} 是 {@link ItemInstance} 接口的默认方法，
 * 所以这里必须用「接口 mixin」混入接口本体（Mixin 规定：目标是接口时，mixin 自身也必须是接口）。
 * 旧写法是在 {@code ItemStack} 上加 {@code @Unique} 方法去覆盖接口默认方法，
 * 一旦原版自己在 ItemStack 里实现该方法就会冲突报错，接口 mixin 没有这个问题。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
@Mixin(ItemInstance.class)
public interface ItemInstanceMixin {

    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true, require = 0)
    default void mixinEnhance$canStackTo64(CallbackInfoReturnable<Integer> cir) {
        int original = cir.getReturnValue();
        if (original >= Item.DEFAULT_MAX_STACK_SIZE
            || !MixinEnhanceClient.getConfig().itemConfig.canStackTo64) {
            return;
        }
        if (((Object) this) instanceof ItemStack stack && StackableItems.isForceStackable(stack)) {
            cir.setReturnValue(Item.DEFAULT_MAX_STACK_SIZE);
        }
    }
}

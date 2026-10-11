package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.frankfurtlin.mixinenhance.util.EquipmentQuality;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.DropChances;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 怪物生成时的盔甲与附魔：全部由「盔甲系数」这一个 0-1 配置决定
 * ——自带盔甲的概率、材质档位、附魔概率、附魔强度（等级），且不再受区域难度/月相/游戏天数影响。
 *
 * <p>武器相关的部分见 {@code LivingEntityMixin}（武器材质）与 {@code ZombieEntityMixin}（武器概率）。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 15:32
 */
@Mixin(Mob.class)
public abstract class MobEntityMixin {
    /** 附魔强度上限：系数为 1 时按满级 30 级计算 */
    private static final int MAX_ENCHANT_LEVEL = 30;

    /** 死亡掉落概率 = 系数 ÷ 该值（系数 1 时掉落 10%），避免装备掉落过滥 */
    private static final double DROP_CHANCE_DIVISOR = 10.0;

    @Shadow
    private DropChances dropChances;

    // 死亡掉落概率：手部槽位沿用武器系数，其余（盔甲）沿用盔甲系数，再统一除以 10
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        double armorFactor = MixinEnhanceClient.getConfig().entityConfig.equipmentConfig.armorFactor;
        double weaponFactor = MixinEnhanceClient.getConfig().entityConfig.equipmentConfig.weaponFactor;
        Map<EquipmentSlot, Float> chances = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            boolean hand = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
            double factor = hand ? weaponFactor : armorFactor;
            chances.put(slot, (float) (factor / DROP_CHANCE_DIVISOR));
        }
        this.dropChances = new DropChances(chances);
    }

    // 自带盔甲的概率 = 盔甲系数
    @ModifyConstant(method = "populateDefaultEquipmentSlots", constant = @Constant(floatValue = 0.15F), require = 0)
    private float mixinEnhance$armorChance(float chance) {
        return (float) MixinEnhanceClient.getConfig().entityConfig.equipmentConfig.armorFactor;
    }

    // 概率只按配置来：原版的 0.15F * 区域难度系数 会把概率带偏，这里把系数固定为 1
    @Redirect(method = "populateDefaultEquipmentSlots", require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DifficultyInstance;getSpecialMultiplier()F"))
    private float mixinEnhance$exactArmorChance(DifficultyInstance difficulty) {
        return 1.0F;
    }

    // 盔甲材质档位由盔甲系数决定（档位顺序沿用原版 getEquipmentForSlot 自己的定义）
    @Redirect(method = "populateDefaultEquipmentSlots", require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getEquipmentForSlot(Lnet/minecraft/world/entity/EquipmentSlot;I)Lnet/minecraft/world/item/Item;"))
    private static Item mixinEnhance$armorByFactor(EquipmentSlot equipmentSlot, int vanillaLevel) {
        int tiers = EquipmentQuality.armorTierCount(equipmentSlot);
        if (tiers <= 0) {
            return Mob.getEquipmentForSlot(equipmentSlot, vanillaLevel);
        }
        double factor = MixinEnhanceClient.getConfig().entityConfig.equipmentConfig.armorFactor;
        int tier = EquipmentQuality.randomTier(tiers, factor, ThreadLocalRandom.current().nextDouble());
        return Mob.getEquipmentForSlot(equipmentSlot, tier);
    }

    // 盔甲附魔概率 = 盔甲系数
    @ModifyConstant(method = "enchantSpawnedArmor", constant = @Constant(floatValue = 0.5F), require = 0)
    private float mixinEnhance$armorEnchantChance(float chance) {
        return (float) MixinEnhanceClient.getConfig().entityConfig.equipmentConfig.armorFactor;
    }

    // 武器附魔概率 = 武器系数（原版是 0.25F；此前只改了盔甲的 0.5F 与武器概率，武器附魔一直卡在 25%）
    @ModifyConstant(method = "enchantSpawnedWeapon", constant = @Constant(floatValue = 0.25F), require = 0)
    private float mixinEnhance$weaponEnchantChance(float chance) {
        return (float) MixinEnhanceClient.getConfig().entityConfig.equipmentConfig.weaponFactor;
    }

    // 附魔概率只按配置来（同上，去掉区域难度系数）
    @Redirect(method = "enchantSpawnedEquipment", require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DifficultyInstance;getSpecialMultiplier()F"))
    private float mixinEnhance$exactEnchantChance(DifficultyInstance difficulty) {
        return 1.0F;
    }

    // 附魔强度（等级）由系数决定，替掉原版按难度算出的等级
    @Redirect(method = "enchantSpawnedEquipment", require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;enchantItemFromProvider(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/util/RandomSource;)V"))
    private void mixinEnhance$enchantByFactor(ItemStack stack, RegistryAccess registries,
        ResourceKey<EnchantmentProvider> provider, DifficultyInstance difficulty, RandomSource random) {
        Mob self = (Mob) (Object) this;
        // 主手装备用武器系数，其余（盔甲）用盔甲系数
        double factor = self.getMainHandItem() == stack
            ? MixinEnhanceClient.getConfig().entityConfig.equipmentConfig.weaponFactor
            : MixinEnhanceClient.getConfig().entityConfig.equipmentConfig.armorFactor;
        int level = (int) Math.max(1, Math.round(MAX_ENCHANT_LEVEL * factor));
        EnchantmentHelper.enchantItem(random, stack, level, registries, Optional.<HolderSet<Enchantment>>empty());
    }

    // 出生开始/结束标记：供 LivingEntityMixin 的 setItemSlot 公共钩子区分「出生自带」与「后天拾取」
    @Inject(method = "finalizeSpawn", at = @At("HEAD"), require = 0)
    private void mixinEnhance$beginSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
        EntitySpawnReason reason, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        EquipmentQuality.beginSpawn();
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"), require = 0)
    private void mixinEnhance$endSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
        EntitySpawnReason reason, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        EquipmentQuality.endSpawn();
    }

}

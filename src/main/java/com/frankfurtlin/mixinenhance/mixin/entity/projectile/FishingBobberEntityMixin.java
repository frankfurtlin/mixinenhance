package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/14 12:03
 */
@Mixin(FishingHook.class)
public abstract class FishingBobberEntityMixin extends Projectile {
    @Shadow
    public abstract @Nullable Player getPlayerOwner();

    @Shadow public abstract int retrieve(ItemStack usedItem);

    @Shadow @Final private int luck;

    public FishingBobberEntityMixin(EntityType<? extends Projectile> entityType, Level world) {
        super(entityType, world);
    }

    // 自动钓鱼：浮标下沉（鱼上钩）时自动收杆并重新抛竿
    // 1.21.11 中 getWorld() 改名为 getEntityWorld()；use() 内部 discard 时会清空 player.fishHook，
    // 随后 ItemStack.use 会重新抛出新的浮标
    @Inject(method = "tick", at = @At(value = "INVOKE", ordinal = 2, shift = At.Shift.AFTER,
        target = "Lnet/minecraft/world/entity/projectile/FishingHook;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void autoFishing(CallbackInfo ci) {
        if(!MixinEnhanceClient.getConfig().defaultModuleConfig.enableAutoFishing){
            return;
        }
        Player user = this.getPlayerOwner();
        if (!this.level().isClientSide() && user != null && user.fishing != null) {
            ItemStack itemStack = user.getItemInHand(InteractionHand.MAIN_HAND);
            int i = this.retrieve(itemStack);
            itemStack.hurtWithoutBreaking(i, user);

            this.level().playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL,
                1.0f, 0.4f / (this.level().getRandom().nextFloat() * 0.4f + 0.8f));
            user.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            itemStack.use(this.level(), user, InteractionHand.MAIN_HAND);
        }
    }

    // 急速钓鱼：浮标在水中时每 tick 直接结算钓鱼战利品
    // 1.21.11 中改用 getEntityWorld()/getEntityPos()，Criteria 触发参数直接传 this
    @Inject(method = "catchingFish", at = @At("HEAD"))
    private void buffFishing(BlockPos pos, CallbackInfo ci){
        Player playerEntity = this.getPlayerOwner();
        if(playerEntity == null || !MixinEnhanceClient.getConfig().defaultModuleConfig.enableTickFishing){
            return;
        }
        ItemStack usedItem = playerEntity.getItemInHand(InteractionHand.MAIN_HAND);
        Level world = this.level();
        if (world instanceof ServerLevel serverWorld && world.getServer() != null) {
            LootParams lootWorldContext = new LootParams.Builder(serverWorld)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withParameter(LootContextParams.TOOL, usedItem)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withLuck((float)this.luck + playerEntity.getLuck())
                .create(LootContextParamSets.FISHING);
            LootTable lootTable = world.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
            List<ItemStack> list = lootTable.getRandomItems(lootWorldContext);
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)playerEntity, usedItem, (FishingHook)(Object)this, list);
            for (ItemStack itemStack : list) {
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), itemStack);
                double d = playerEntity.getX() - this.getX();
                double e = playerEntity.getY() - this.getY();
                double f = playerEntity.getZ() - this.getZ();
                itemEntity.setDeltaMovement(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
                world.addFreshEntity(itemEntity);
                world.addFreshEntity(new ExperienceOrb(world, playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ(), this.random.nextInt(6) + 1));
                if (itemStack.is(ItemTags.FISHES)) {
                    playerEntity.awardStat(Stats.FISH_CAUGHT, 1);
                }
            }
        }
    }
}

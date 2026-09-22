package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/14 12:03
 */
@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends ProjectileEntity {
    @Shadow
    public abstract @Nullable PlayerEntity getPlayerOwner();

    @Shadow public abstract int use(ItemStack usedItem);

    @Shadow @Final private int luckBonus;

    public FishingBobberEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    // 自动钓鱼：浮标下沉（鱼上钩）时自动收杆并重新抛竿
    // 1.21.11 中 getWorld() 改名为 getEntityWorld()；use() 内部 discard 时会清空 player.fishHook，
    // 随后 ItemStack.use 会重新抛出新的浮标
    @Inject(method = "tick", at = @At(value = "INVOKE", ordinal = 2, shift = At.Shift.AFTER,
        target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void autoFishing(CallbackInfo ci) {
        if(!MixinEnhanceClient.getConfig().defaultModuleConfig.enableAutoFishing){
            return;
        }
        PlayerEntity user = this.getPlayerOwner();
        if (!this.getEntityWorld().isClient() && user != null && user.fishHook != null) {
            ItemStack itemStack = user.getStackInHand(Hand.MAIN_HAND);
            int i = this.use(itemStack);
            itemStack.damage(i, user);

            this.getEntityWorld().playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL,
                1.0f, 0.4f / (this.getEntityWorld().getRandom().nextFloat() * 0.4f + 0.8f));
            user.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
            itemStack.use(this.getEntityWorld(), user, Hand.MAIN_HAND);
        }
    }

    // 急速钓鱼：浮标在水中时每 tick 直接结算钓鱼战利品
    // 1.21.11 中改用 getEntityWorld()/getEntityPos()，Criteria 触发参数直接传 this
    @Inject(method = "tickFishingLogic", at = @At("HEAD"))
    private void buffFishing(BlockPos pos, CallbackInfo ci){
        PlayerEntity playerEntity = this.getPlayerOwner();
        if(playerEntity == null || !MixinEnhanceClient.getConfig().defaultModuleConfig.enableTickFishing){
            return;
        }
        ItemStack usedItem = playerEntity.getStackInHand(Hand.MAIN_HAND);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld serverWorld && world.getServer() != null) {
            LootWorldContext lootWorldContext = new LootWorldContext.Builder(serverWorld)
                .add(LootContextParameters.ORIGIN, this.getEntityPos())
                .add(LootContextParameters.TOOL, usedItem)
                .add(LootContextParameters.THIS_ENTITY, this)
                .luck((float)this.luckBonus + playerEntity.getLuck())
                .build(LootContextTypes.FISHING);
            LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(LootTables.FISHING_GAMEPLAY);
            List<ItemStack> list = lootTable.generateLoot(lootWorldContext);
            Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, usedItem, (FishingBobberEntity)(Object)this, list);
            for (ItemStack itemStack : list) {
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), itemStack);
                double d = playerEntity.getX() - this.getX();
                double e = playerEntity.getY() - this.getY();
                double f = playerEntity.getZ() - this.getZ();
                itemEntity.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
                world.spawnEntity(itemEntity);
                world.spawnEntity(new ExperienceOrbEntity(world, playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ(), this.random.nextInt(6) + 1));
                if (itemStack.isIn(ItemTags.FISHES)) {
                    playerEntity.increaseStat(Stats.FISH_CAUGHT, 1);
                }
            }
        }
    }
}

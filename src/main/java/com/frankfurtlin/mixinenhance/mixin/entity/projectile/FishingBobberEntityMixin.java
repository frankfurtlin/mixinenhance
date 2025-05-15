package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
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

    // 自动钓鱼
    @Inject(method = "tick", at = @At(value = "INVOKE", ordinal = 2, shift = At.Shift.AFTER,
        target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void autoFishing(CallbackInfo ci) {
        if(!MixinEnhanceClient.getConfig().defaultModuleConfig.enableAutoFishing){
            return;
        }
        PlayerEntity user = this.getPlayerOwner();
        if (!this.getWorld().isClient && user != null && user.fishHook != null) {
            ItemStack itemStack = user.getStackInHand(Hand.MAIN_HAND);
            int i = this.use(itemStack);
            itemStack.damage(i, user, LivingEntity.getSlotForHand(Hand.MAIN_HAND));

            this.getWorld().playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL,
                1.0f, 0.4f / (this.getWorld().getRandom().nextFloat() * 0.4f + 0.8f));
            user.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
            itemStack.use(this.getWorld(), user, Hand.MAIN_HAND);
        }
    }

    // 急速钓鱼
    @Inject(method = "tickFishingLogic", at = @At("HEAD"))
    private void buffFishing(BlockPos pos, CallbackInfo ci){
        PlayerEntity playerEntity = this.getPlayerOwner();
        if(playerEntity != null){
            ItemStack usedItem = playerEntity.getStackInHand(Hand.MAIN_HAND);
            if(MixinEnhanceClient.getConfig().defaultModuleConfig.enableTickFishing){
                LootWorldContext lootWorldContext = new LootWorldContext.Builder((ServerWorld)this.getWorld())
                    .add(LootContextParameters.ORIGIN, this.getPos())
                    .add(LootContextParameters.TOOL, usedItem)
                    .add(LootContextParameters.THIS_ENTITY, this)
                    .luck((float)this.luckBonus + playerEntity.getLuck())
                    .build(LootContextTypes.FISHING);
                if(this.getWorld().getServer() != null){
                    LootTable lootTable = this.getWorld().getServer().getReloadableRegistries().getLootTable(LootTables.FISHING_GAMEPLAY);
                    List<ItemStack> list = lootTable.generateLoot(lootWorldContext);
                    Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, usedItem, this.getPlayerOwner().fishHook, list);
                    for (ItemStack itemStack : list) {
                        ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), itemStack);
                        double d = playerEntity.getX() - this.getX();
                        double e = playerEntity.getY() - this.getY();
                        double f = playerEntity.getZ() - this.getZ();
                        itemEntity.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
                        this.getWorld().spawnEntity(itemEntity);
                        playerEntity.getWorld()
                            .spawnEntity(
                                new ExperienceOrbEntity(playerEntity.getWorld(), playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ() + 0.5, this.random.nextInt(6) + 1)
                            );
                        if (itemStack.isIn(ItemTags.FISHES)) {
                            playerEntity.increaseStat(Stats.FISH_CAUGHT, 1);
                        }
                    }
                }

            }
        }

    }
}

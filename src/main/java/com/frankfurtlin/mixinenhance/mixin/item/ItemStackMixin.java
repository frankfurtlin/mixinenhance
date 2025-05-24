package com.frankfurtlin.mixinenhance.mixin.item;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.component.ComponentHolder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder  {

    @Shadow public abstract Item getItem();

    // 对某些物品改变其堆叠数量
    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void changeItemsStack(CallbackInfoReturnable<Integer> cir) {
        if ((
            this.getItem() == Items.SADDLE ||
            this.getItem() == Items.MINECART ||
            this.getItem() == Items.CHEST_MINECART ||
            this.getItem() == Items.FURNACE_MINECART ||
            this.getItem() == Items.TNT_MINECART ||
            this.getItem() == Items.HOPPER_MINECART ||
            this.getItem() == Items.OAK_BOAT ||
            this.getItem() == Items.OAK_CHEST_BOAT ||
            this.getItem() == Items.SPRUCE_BOAT ||
            this.getItem() == Items.SPRUCE_CHEST_BOAT ||
            this.getItem() == Items.BIRCH_BOAT ||
            this.getItem() == Items.BIRCH_CHEST_BOAT ||
            this.getItem() == Items.JUNGLE_BOAT ||
            this.getItem() == Items.JUNGLE_CHEST_BOAT ||
            this.getItem() == Items.ACACIA_BOAT ||
            this.getItem() == Items.ACACIA_CHEST_BOAT ||
            this.getItem() == Items.CHERRY_BOAT ||
            this.getItem() == Items.CHERRY_CHEST_BOAT ||
            this.getItem() == Items.DARK_OAK_BOAT ||
            this.getItem() == Items.DARK_OAK_CHEST_BOAT ||
            this.getItem() == Items.PALE_OAK_BOAT ||
            this.getItem() == Items.PALE_OAK_CHEST_BOAT ||
            this.getItem() == Items.MANGROVE_BOAT ||
            this.getItem() == Items.MANGROVE_CHEST_BOAT ||
            this.getItem() == Items.BAMBOO_RAFT ||
            this.getItem() == Items.BAMBOO_CHEST_RAFT ||
            this.getItem() == Items.MUSHROOM_STEW ||
            this.getItem() == Items.WATER_BUCKET ||
            this.getItem() == Items.LAVA_BUCKET ||
            this.getItem() == Items.POWDER_SNOW_BUCKET ||
            this.getItem() == Items.MILK_BUCKET ||
            this.getItem() == Items.PUFFERFISH_BUCKET ||
            this.getItem() == Items.SALMON_BUCKET ||
            this.getItem() == Items.COD_BUCKET ||
            this.getItem() == Items.SPYGLASS ||
            this.getItem() == Items.CAKE ||
            this.getItem() == Items.WHITE_BED ||
            this.getItem() == Items.ORANGE_BED ||
            this.getItem() == Items.MAGENTA_BED ||
            this.getItem() == Items.LIGHT_BLUE_BED ||
            this.getItem() == Items.YELLOW_BED ||
            this.getItem() == Items.LIME_BED ||
            this.getItem() == Items.PINK_BED ||
            this.getItem() == Items.GRAY_BED ||
            this.getItem() == Items.LIGHT_GRAY_BED ||
            this.getItem() == Items.CYAN_BED ||
            this.getItem() == Items.PURPLE_BED ||
            this.getItem() == Items.BLUE_BED ||
            this.getItem() == Items.BROWN_BED ||
            this.getItem() == Items.GREEN_BED ||
            this.getItem() == Items.RED_BED ||
            this.getItem() == Items.BLACK_BED ||
            this.getItem() == Items.POTION ||
            this.getItem() == Items.SPLASH_POTION ||
            this.getItem() == Items.LINGERING_POTION ||
            this.getItem() == Items.RABBIT_STEW ||
            this.getItem() == Items.BEETROOT_SOUP ||
            this.getItem() == Items.TOTEM_OF_UNDYING ||
            this.getItem() == Items.MUSIC_DISC_13 ||
            this.getItem() == Items.MUSIC_DISC_CAT ||
            this.getItem() == Items.MUSIC_DISC_BLOCKS ||
            this.getItem() == Items.MUSIC_DISC_CHIRP ||
            this.getItem() == Items.MUSIC_DISC_CREATOR ||
            this.getItem() == Items.MUSIC_DISC_CREATOR_MUSIC_BOX ||
            this.getItem() == Items.MUSIC_DISC_FAR ||
            this.getItem() == Items.MUSIC_DISC_MALL ||
            this.getItem() == Items.MUSIC_DISC_MELLOHI ||
            this.getItem() == Items.MUSIC_DISC_STAL ||
            this.getItem() == Items.MUSIC_DISC_STRAD ||
            this.getItem() == Items.MUSIC_DISC_WARD ||
            this.getItem() == Items.MUSIC_DISC_11 ||
            this.getItem() == Items.MUSIC_DISC_WAIT ||
            this.getItem() == Items.MUSIC_DISC_OTHERSIDE ||
            this.getItem() == Items.MUSIC_DISC_RELIC ||
            this.getItem() == Items.MUSIC_DISC_5 ||
            this.getItem() == Items.MUSIC_DISC_PRECIPICE
        ) && MixinEnhanceClient.getConfig().itemModuleConfig.canStackTo64) {
            cir.setReturnValue(Item.DEFAULT_MAX_COUNT);
        }
    }
}
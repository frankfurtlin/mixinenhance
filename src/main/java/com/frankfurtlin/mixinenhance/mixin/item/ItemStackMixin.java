package com.frankfurtlin.mixinenhance.mixin.item;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {
    @Shadow public abstract Item getItem();

    // 对某些物品改变其堆叠数量
    // 26.3 中 getMaxStackSize 由 ItemInstance 接口的默认方法提供（标准 Mixin 无法混入接口），
    // 改为向 ItemStack 合并一个覆写方法：命中列表时返回 64，否则按原逻辑读取 MAX_STACK_SIZE 组件
    @Unique
    public int getMaxStackSize() {
        if (MixinEnhanceClient.getConfig().itemModuleConfig.canStackTo64) {
            Item item = this.getItem();
            if ((
                item == Items.SADDLE ||
                item == Items.MINECART ||
                item == Items.CHEST_MINECART ||
                item == Items.FURNACE_MINECART ||
                item == Items.TNT_MINECART ||
                item == Items.HOPPER_MINECART ||
                item == Items.OAK_BOAT ||
                item == Items.OAK_CHEST_BOAT ||
                item == Items.SPRUCE_BOAT ||
                item == Items.SPRUCE_CHEST_BOAT ||
                item == Items.BIRCH_BOAT ||
                item == Items.BIRCH_CHEST_BOAT ||
                item == Items.JUNGLE_BOAT ||
                item == Items.JUNGLE_CHEST_BOAT ||
                item == Items.ACACIA_BOAT ||
                item == Items.ACACIA_CHEST_BOAT ||
                item == Items.CHERRY_BOAT ||
                item == Items.CHERRY_CHEST_BOAT ||
                item == Items.DARK_OAK_BOAT ||
                item == Items.DARK_OAK_CHEST_BOAT ||
                item == Items.PALE_OAK_BOAT ||
                item == Items.PALE_OAK_CHEST_BOAT ||
                item == Items.MANGROVE_BOAT ||
                item == Items.MANGROVE_CHEST_BOAT ||
                item == Items.BAMBOO_RAFT ||
                item == Items.BAMBOO_CHEST_RAFT ||
                item == Items.MUSHROOM_STEW ||
                item == Items.BUCKET ||
                item == Items.WATER_BUCKET ||
                item == Items.LAVA_BUCKET ||
                item == Items.POWDER_SNOW_BUCKET ||
                item == Items.MILK_BUCKET ||
                item == Items.PUFFERFISH_BUCKET ||
                item == Items.SALMON_BUCKET ||
                item == Items.COD_BUCKET ||
                item == Items.SPYGLASS ||
                item == Items.CAKE ||
                item == Blocks.BED.white().asItem() ||
                item == Blocks.BED.orange().asItem() ||
                item == Blocks.BED.magenta().asItem() ||
                item == Blocks.BED.lightBlue().asItem() ||
                item == Blocks.BED.yellow().asItem() ||
                item == Blocks.BED.lime().asItem() ||
                item == Blocks.BED.pink().asItem() ||
                item == Blocks.BED.gray().asItem() ||
                item == Blocks.BED.lightGray().asItem() ||
                item == Blocks.BED.cyan().asItem() ||
                item == Blocks.BED.purple().asItem() ||
                item == Blocks.BED.blue().asItem() ||
                item == Blocks.BED.brown().asItem() ||
                item == Blocks.BED.green().asItem() ||
                item == Blocks.BED.red().asItem() ||
                item == Blocks.BED.black().asItem() ||
                item == Items.POTION ||
                item == Items.SPLASH_POTION ||
                item == Items.LINGERING_POTION ||
                item == Items.RABBIT_STEW ||
                item == Items.BEETROOT_SOUP ||
                item == Items.TOTEM_OF_UNDYING ||
                item == Items.MUSIC_DISC_13 ||
                item == Items.MUSIC_DISC_CAT ||
                item == Items.MUSIC_DISC_BLOCKS ||
                item == Items.MUSIC_DISC_CHIRP ||
                item == Items.MUSIC_DISC_CREATOR ||
                item == Items.MUSIC_DISC_CREATOR_MUSIC_BOX ||
                item == Items.MUSIC_DISC_FAR ||
                item == Items.MUSIC_DISC_MALL ||
                item == Items.MUSIC_DISC_MELLOHI ||
                item == Items.MUSIC_DISC_STAL ||
                item == Items.MUSIC_DISC_STRAD ||
                item == Items.MUSIC_DISC_WARD ||
                item == Items.MUSIC_DISC_11 ||
                item == Items.MUSIC_DISC_WAIT ||
                item == Items.MUSIC_DISC_OTHERSIDE ||
                item == Items.MUSIC_DISC_RELIC ||
                item == Items.MUSIC_DISC_5 ||
                item == Items.MUSIC_DISC_PRECIPICE
            )) {
                return Item.DEFAULT_MAX_STACK_SIZE;
            }
        }
        // 原版逻辑：读取 MAX_STACK_SIZE 数据组件，缺省为 1
        return this.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }
}

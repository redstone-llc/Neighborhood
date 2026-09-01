package llc.redstone.neighborhood.mixin;

import llc.redstone.neighborhood.gui.TextureSelector;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
//? if <26.1 {
import net.minecraft.world.inventory.ClickType;
//?} else {
/*import net.minecraft.world.inventory.ContainerInput;
*///?}

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen implements MenuAccess<ChestMenu> {
    protected AbstractContainerScreenMixin(Component component) {
        super(component);
    }

    @Unique
    private static ItemStack textureSelectorStack;

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci) {
        if (!this.title.getString().equals("Edit Item")) return;
        textureSelectorStack = new ItemStack(Items.PAPER);
        textureSelectorStack.set(DataComponents.CUSTOM_NAME, Component.literal("§aSelect a texture"));
        textureSelectorStack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("neighborhood", "ruby_large"));
    }

    @Inject(method = "containerTick", at = @At("HEAD"), cancellable = true)
    private void onContainerTick(CallbackInfo ci) {
        if (!this.title.getString().equals("Edit Item")) return;
        if (this.getMenu() instanceof ChestMenu menu) {
            Container container = menu.getContainer();
            if (!this.title.getString().equals("Edit Item")) return;
            if (container.getItem(53).isEmpty()) {
                container.setItem(53, textureSelectorStack);
            }
        }
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    //? if <26.1 {
    private void onSlotClicked(Slot slot, int i, int j, ClickType clickType, CallbackInfo ci) {
    //? } else {
    /*private void onSlotClicked(Slot slot, int i, int j, ContainerInput clickType, CallbackInfo ci) {*/
    //?}
        if (slot == null) return;
        if (this.getMenu() instanceof ChestMenu menu) {
            Container container = menu.getContainer();
            if (!this.title.getString().equals("Edit Item")) return;
            if (slot.index == 53) {
                ItemStack stack = container.getItem(53);
                if (stack.isEmpty()) return;
                TextureSelector.Companion.open(true);
                ci.cancel();
            }
        }
    }

}

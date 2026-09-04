package llc.redstone.neighborhood.mixin;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @Redirect(method = "appendItemLayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object neighborhood$applyPackModel(ItemStack instance, DataComponentType dataComponentType) {
        if (instance.get(DataComponents.CUSTOM_MODEL_DATA) == null) {
            return instance.get(DataComponents.ITEM_MODEL);
        }
        if (instance.get(DataComponents.CUSTOM_MODEL_DATA).getString(0) == null) {
            return instance.get(DataComponents.ITEM_MODEL);
        }
        return Identifier.fromNamespaceAndPath("neighborhood", instance.get(DataComponents.CUSTOM_MODEL_DATA).getString(0));
    }
}
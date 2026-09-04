package llc.redstone.neighborhood.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import llc.redstone.neighborhood.textures.Texture.Companion.setTexture
//? if <26.1 {
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
//?} else {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
*///?}
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot

object UnSetTextureCommand {
    fun create(): LiteralArgumentBuilder<FabricClientCommandSource> =
        literal("unsettexture")
            .executes(this::unset)

    fun unset(it: CommandContext<FabricClientCommandSource>): Int {
        if (!it.source.player.isCreative) {
            it.source.sendFeedback(Component.literal("§cYou must be in creative mode to use this command."))
            return 0
        }
        if (!it.source.player.hasItemInSlot(EquipmentSlot.MAINHAND)) {
            it.source.sendFeedback(Component.literal("§cYou must hold an item in your main hand to unset its texture."))
            return 0
        }
        it.source.player.setTexture(null)
        return 1
    }
}
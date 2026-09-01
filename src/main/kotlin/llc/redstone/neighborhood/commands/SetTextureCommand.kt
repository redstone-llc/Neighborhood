package llc.redstone.neighborhood.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import llc.redstone.neighborhood.gui.TextureSelector
import llc.redstone.neighborhood.textures.Texture
import llc.redstone.neighborhood.textures.Texture.Companion.setTexture
//? if <26.1 {
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument

//?} else {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument
*///?}
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot

object SetTextureCommand {
    fun create(): LiteralArgumentBuilder<FabricClientCommandSource> =
        literal("settexture")
            .executes(this::open)
            .then(argument("texture", StringArgumentType.word())
                .suggests { context, builder ->
                    val textures = Texture.textures
                    textures.forEach {
                        if (it.id.startsWith(builder.remaining)) builder.suggest(it.id)
                    }
                    builder.buildFuture()
                }
                .executes(this::setTexture)
            )

    fun open(it: CommandContext<FabricClientCommandSource>): Int {
        if (!it.source.player.isCreative) {
            it.source.sendFeedback(Component.literal("§cYou must be in creative mode to use this command."))
            return 0
        }
        if (!it.source.player.hasItemInSlot(EquipmentSlot.MAINHAND)) {
            it.source.sendFeedback(Component.literal("§cYou must hold an item in your main hand to set its texture."))
            return 0
        }
        TextureSelector.open()
        return 1
    }

    fun setTexture(it: CommandContext<FabricClientCommandSource>): Int {
        val texture = StringArgumentType.getString(it, "texture")
        it.source.player.setTexture(texture)
        return 1
    }

    fun alias(): LiteralArgumentBuilder<FabricClientCommandSource> =
        literal("st")
            .executes(this::open)
            .then(argument("texture", StringArgumentType.word())
                .suggests { context, builder ->
                    val textures = Texture.textures
                    textures.forEach {
                        if (it.id.startsWith(builder.remaining)) builder.suggest(it.id)
                    }
                    builder.buildFuture()
                }
                .executes(this::setTexture)
            )

}
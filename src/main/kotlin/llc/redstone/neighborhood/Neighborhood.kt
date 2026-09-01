package llc.redstone.neighborhood

import llc.redstone.neighborhood.commands.SetTextureCommand
import llc.redstone.neighborhood.textures.Texture
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Neighborhood: ClientModInitializer {
    const val MOD_ID = "neighborhood"
    val LOGGER: Logger = LoggerFactory.getLogger("Neighborhood")
    const val VERSION = /*$ mod_version*/ "0.1.0";
    const val MINECRAFT = /*$ minecraft*/ "1.21.11";

    fun Player.sendSystemMessage(comp: Component) {
        //? if <26.1 {
        this.displayClientMessage(comp, false)
        //?} else {
        /*this.sendSystemMessage(comp)
       *///?}
    }

    override fun onInitializeClient() {
        LOGGER.info("Loaded Neighborhood v$VERSION for Minecraft $MINECRAFT.")

        Texture.initTextures()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
            dispatcher.register(SetTextureCommand.create())
            dispatcher.register(SetTextureCommand.alias())
        }
    }
}
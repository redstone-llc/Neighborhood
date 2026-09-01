package llc.redstone.neighborhood.textures

import llc.redstone.neighborhood.Neighborhood
import llc.redstone.neighborhood.gui.TextureSelector
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import kotlin.text.trim

data class Texture(
    val name: String,
    val id: String,
    val category: TextureSelector.Category
) {
    fun toItemStack(): ItemStack {
        val item = ItemStack(Items.PAPER)
        item.set(DataComponents.ITEM_NAME, Component.literal("§a$name"))
        item.set(
            DataComponents.LORE,
            ItemLore(listOf(Component.literal("§7ID: $id"), Component.empty(), Component.literal("§eClick to set")))
        )
        item.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("neighborhood", id))
        return item
    }

    companion object {
        val textures = mutableListOf<Texture>()

        fun initTextures() {
            val file = Texture::class.java.getResourceAsStream("/neighborhood/textures.txt") ?: return
            file.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(":")
                    if (parts.size == 2) {
                        val name = parts[0].trim()
                        val id = parts[0].trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                        val category = try {
                            TextureSelector.Category.valueOf(parts[1].trim().uppercase())
                        } catch (e: IllegalArgumentException) {
                            return@forEach
                        }
                        textures.add(Texture(name, id, category))
                    }
                }
            }

            Neighborhood.LOGGER.info("Loaded ${textures.size} textures from textures.txt")
        }

        fun Player.setTexture(id: String) {
            val itemstack = this.mainHandItem.copy()
            itemstack.set(
                DataComponents.CUSTOM_MODEL_DATA,
                CustomModelData(emptyList(), emptyList(), listOf(id), emptyList())
            )

            Minecraft.getInstance().execute {
                val slot = convertSlot(this.inventory.selectedSlot) ?: return@execute
                Minecraft.getInstance().connection?.send(
                    ServerboundSetCreativeModeSlotPacket(
                        slot,
                        itemstack
                    )
                )
            }
        }

        private fun convertSlot(slot: Int): Int? {
            return when (slot) {
                in 0..8 -> slot + 36
                in 9..35 -> slot
                else -> null
            }
        }
    }
}
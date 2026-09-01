package llc.redstone.neighborhood.gui

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import llc.redstone.neighborhood.textures.Texture
import llc.redstone.neighborhood.utils.PaginationList
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.TooltipDisplay
import kotlin.collections.emptySet
import kotlin.collections.get

class TextureBrowser(
    menu: ChestMenu,
    playerInventory: Inventory,
    private val contents: SimpleContainer,
) : ContainerScreen(menu, playerInventory, Component.literal("Select Texture")) {
    var category: Category? = null
    val textures = Texture.textures.groupBy { it.category }
    var page = 1
    var search: EditBox? = null

    override fun init() {
        super.init()
        setItems()

        val box = EditBox(font, leftPos, topPos - 18 - 4, imageWidth, 18, Component.literal("Search Textures"))
        box.setMaxLength(24)
        box.setResponder {
            setItems()
        }
        search = addRenderableWidget(box)
        setInitialFocus(box)
    }

    fun getFilteredTextures(): List<Texture> {
        val searchText = search?.value?.lowercase() ?: ""
        if (category == null) {
            return Texture.textures.filter { it.name.lowercase().contains(searchText) }
        }
        return textures[category]?.filter { it.name.lowercase().contains(searchText) } ?: emptyList()
    }

    fun setItems() {
        val filteredTextures = getFilteredTextures()
        val paginatedTextures = PaginationList(filteredTextures, 28)
        val currentPageItems = paginatedTextures.getPage(page)

        contents.clearContent()
        currentPageItems?.forEachIndexed { index, texture ->
            contents.setItem(slots[index], texture.toItemStack())
        }

        //fill outside slots with empty items
        for (i in 0 until contents.containerSize) {
            if (i !in slots) {
                contents.setItem(i, ItemStack(Items.GLASS_PANE).apply {
                    this.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(true, ReferenceLinkedOpenHashSet()))
                    this.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("neighborhood", "115"))
                })
            }
        }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val box = search
        if (box != null && box.isFocused && event.key() != 256) {
            box.keyPressed(event)
            return true
        }
        return super.keyPressed(event)
    }

    override fun slotClicked(slot: Slot, i: Int, j: Int, clickType: ClickType) {

    }


    enum class Category {
        MISCELLANEOUS,
        MATERIALS,
        WEAPONS,
        TOOLS,
        ARMOR,
        SEASONAL
    }

    companion object {
        val slots = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        )

        fun open() {
            Minecraft.getInstance().execute {
                val player = Minecraft.getInstance().player ?: return@execute
                val container = SimpleContainer(9 * 6)
                val menu = ChestMenu.sixRows(0, player.inventory, container)
                val screen = TextureBrowser(menu, player.inventory, container)
                Minecraft.getInstance().setScreenAndShow(screen)
            }
        }
    }
}

package llc.redstone.neighborhood.gui

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import llc.redstone.neighborhood.textures.Texture
import llc.redstone.neighborhood.textures.Texture.Companion.setTexture
import llc.redstone.neighborhood.utils.PaginationList
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.TooltipDisplay

class TextureSelector(
    menu: ChestMenu,
    playerInventory: Inventory,
    private val contents: SimpleContainer,
    private val backToEditMenu: Boolean
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

    fun categoryItem(category: Category, lore: List<String>, id: String): ItemStack {
        val item = ItemStack(Items.PAPER)
        item.set(DataComponents.ITEM_NAME, Component.literal("§a${category.name.lowercase().replaceFirstChar { it.uppercase() }}"))
        item.set(
            DataComponents.LORE,
            ItemLore(lore.map { Component.literal(it) })
        )
        item.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("neighborhood", id))
        if (category == this.category) {
            item.set(DataComponents.ITEM_NAME, Component.literal("§a${category.name.lowercase().replaceFirstChar { it.uppercase() }} §7(Selected)"))
            item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        }
        return item
    }

    fun setItems() {
        val filteredTextures = getFilteredTextures()
        val paginatedTextures = PaginationList(filteredTextures, 35)
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
                    this.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("neighborhood", "inventory_fill"))
                })
            }
        }

        contents.setItem(0, categoryItem(Category.MISCELLANEOUS, listOf("§7This category has items", "§7that don't fit any", "§7of the other categories.", "", "§eClick to open!"), "checkpoint"))
        contents.setItem(9, categoryItem(Category.MATERIALS, listOf("§7This category has items", "§7that you would find", "§7as materials.", "", "§eClick to open!"), "silver_ingot"))
        contents.setItem(18, categoryItem(Category.WEAPONS, listOf("§7This category has items", "§7that you would find", "§7as weapons.", "", "§eClick to open!"), "platinum_sword"))
        contents.setItem(27, categoryItem(Category.TOOLS, listOf("§7This category has items", "§7that you would find", "§7as tools.", "", "§eClick to open!"), "cobalt_axe"))
        contents.setItem(36, categoryItem(Category.ARMOR, listOf("§7This category has items", "§7that you would find", "§7as armor.", "", "§eClick to open!"), "mithril_chestplate"))
        contents.setItem(45, categoryItem(Category.SEASONAL, listOf("§7This category has items", "§7that you would find", "§7as seasonal items.", "", "§eClick to open!"), "gift"))

        if (page < paginatedTextures.getPageCount()) {
            contents.setItem(53, ItemStack(Items.ARROW).apply {
                this.set(DataComponents.ITEM_NAME, Component.literal("§aNext Page"))
                this.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("neighborhood", "arrow_right"))
            })
        }

        if (page > 1) {
            contents.setItem(47, ItemStack(Items.ARROW).apply {
                this.set(DataComponents.ITEM_NAME, Component.literal("§aPrevious Page"))
                this.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("neighborhood", "arrow_left"))
            })
        }

        contents.setItem(50, ItemStack(Items.BARRIER).apply {
            this.set(DataComponents.ITEM_NAME, Component.literal(if (backToEditMenu) "§aBack to Edit Menu" else "§cClose"))
            this.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("neighborhood", "arrow_return"))
        })
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val box = search
        if (box != null && box.isFocused && event.key() != 256) {
            box.keyPressed(event)
            return true
        }
        return super.keyPressed(event)
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        val slot: Slot? = getHoveredSlot(mouseButtonEvent.x(), mouseButtonEvent.y())
        if (slot != null) {
            this.slotClicked(slot)
            return true
        }
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    private fun getHoveredSlot(d: Double, e: Double): Slot? {
        for (slot in this.menu.slots) {
            if (slot.isActive && this.isHovering(slot.x, slot.y, 16, 16, d, e)) {
                return slot
            }
        }

        return null
    }

    fun slotClicked(slot: Slot?) {
        val item = slot?.item ?: return
        if (item.isEmpty) return

        val paginatedTextures = PaginationList(getFilteredTextures(), 35)

        when (slot.index) {
            50 -> {
                //? if <26.2 {
                if (backToEditMenu) {
                    Minecraft.getInstance().setScreen(null)
                    Minecraft.getInstance().connection?.sendCommand("edit")
                } else {
                    Minecraft.getInstance().setScreen(null)
                }
                //?} else {
                /*if (backToEditMenu) {
                    Minecraft.getInstance().gui.setScreen(null)
                    Minecraft.getInstance().connection?.sendCommand("edit")
                } else {
                    Minecraft.getInstance().gui.setScreen(null)
                }
                *///?}
            }
            53 -> {
                if (page < paginatedTextures.getPageCount()) {
                    page++
                    setItems()
                }
            }
            47 -> {
                if (page > 1) {
                    page--
                    setItems()
                }
            }
            0 -> clickCategory(Category.MISCELLANEOUS)
            9 -> clickCategory(Category.MATERIALS)
            18 -> clickCategory(Category.WEAPONS)
            27 -> clickCategory(Category.TOOLS)
            36 -> clickCategory(Category.ARMOR)
            45 -> clickCategory(Category.SEASONAL)
            in slots -> {
                val id = item.get(DataComponents.ITEM_MODEL)?.path ?: return
                Minecraft.getInstance().player?.setTexture(id)
            }
        }
    }

    fun clickCategory(category: Category) {
        if (category == this.category) {
            this.category = null
        } else {
            this.category = category
        }
        page = 1
        setItems()
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
            2, 3, 4, 5, 6, 7, 8,
            11, 12, 13, 14, 15, 16, 17,
            20, 21, 22, 23, 24, 25, 26,
            29, 30, 31, 32, 33, 34, 35,
            38, 39, 40, 41, 42, 43, 44
        )

        fun open(backToEditMenu: Boolean = false) {
            Minecraft.getInstance().execute {
                val player = Minecraft.getInstance().player ?: return@execute
                val container = SimpleContainer(9 * 6)
                val menu = ChestMenu.sixRows(0, player.inventory, container)
                val screen = TextureSelector(menu, player.inventory, container, backToEditMenu)
                Minecraft.getInstance().setScreenAndShow(screen)
            }
        }
    }
}

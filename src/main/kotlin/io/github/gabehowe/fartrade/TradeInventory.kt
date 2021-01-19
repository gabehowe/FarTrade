package io.github.gabehowe.fartrade

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class TradeInventory {

    lateinit var inv: Inventory
    fun init() {
        inv = Bukkit.createInventory(null,54)
         var bottomrow = 18
        for (i in 18..26) {
            val blackglass = createItem(Material.BLACK_STAINED_GLASS_PANE, "")
            inv.setItem(i,blackglass)
            bottomrow += 1
        }
    }

    private fun createItem(material: Material, name: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta?.setDisplayName(name)
        item.itemMeta = meta
        return item
    }


}
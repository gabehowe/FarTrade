package io.github.gabehowe.fartrade

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class TradeInventory(farTrade: FarTrade) {
    var inv = Bukkit.createInventory(this, 54, "Custom Items")
    fun init() {
         val bottomrow = 18
        while (bottomrow <= 26) {
            inv.setItem(bottomrow, createItem(Material.BLACK_STAINED_GLASS_PANE, ""))
            bottomrow.inc()
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
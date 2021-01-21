package io.github.gabehowe.fartrade

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class TradeInventoryReceiver(private val farTrade: FarTrade) : InventoryHolder{
    lateinit var inv: Inventory
    override fun getInventory(): Inventory {
        return inv
    }

}

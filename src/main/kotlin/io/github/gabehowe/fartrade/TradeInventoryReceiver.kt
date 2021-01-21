package io.github.gabehowe.fartrade

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class TradeInventoryReceiver(private val farTrade: FarTrade) : InventoryHolder{

    override fun getInventory(): Inventory {
        return farTrade.receiverInv
    }

}

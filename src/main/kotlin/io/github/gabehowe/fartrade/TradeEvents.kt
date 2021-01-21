package io.github.gabehowe.fartrade

import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

class TradeEvents(private val farTrade: FarTrade) : Listener {
    @EventHandler
    fun inventoryClickEvent(event: InventoryClickEvent) {
        if (!farTrade.tradeMap.any{it.key.first == event.whoClicked.uniqueId || it.key.first == event.whoClicked.uniqueId}) {
            return
        }
        val filter = farTrade.tradeMap.filter {it.key.first == event.whoClicked.uniqueId || it.key.first == event.whoClicked.uniqueId}
        val onlyFiltered = filter[0]
        val senderInv = farTrade.tradeMap[filter[0]]?.first!!
        val receiverInv = farTrade.tradeMap[filter[0]]?.second!!
        if (event.clickedInventory?.holder is TradeInventoryReceiver) { // receiver
            event.isCancelled = true
            if (event.currentItem == null) {
                return
            }
            if (event.slot !in farTrade.senderOffer) {
                return
            }
            event.whoClicked.inventory.addItem(event.currentItem)
            event.currentItem!!.amount = 0
            farTrade.updateInventory(senderInv, receiverInv)
            farTrade.updateInventory(receiverInv, senderInv)
            return
        }
        if (receiverInv.viewers.contains(event.whoClicked)) {
            if (event.clickedInventory?.holder !is HumanEntity) {
                return
            }
            if (event.currentItem == null) {
                return
            }
            for (i in 1..12) {
                if (receiverInv.contents[farTrade.senderOffer[i]] == null) {
                    event.isCancelled = true
                    receiverInv.setItem(farTrade.senderOffer[i], event.currentItem)
                    event.currentItem!!.amount = 0
                    farTrade.updateInventory(senderInv, receiverInv)
                    farTrade.updateInventory(receiverInv, senderInv)
                    return
                }
            }
        }
        if (event.clickedInventory?.holder is TradeInventory) { // sender
            event.isCancelled = true
            if (event.currentItem == null) {
                return
            }
            if (event.slot !in farTrade.senderOffer) {
                return
            }
            event.whoClicked.inventory.addItem(event.currentItem)
            event.currentItem!!.amount = 0
            farTrade.updateInventory(senderInv, receiverInv)
            farTrade.updateInventory(receiverInv, senderInv)
            return
        }
        if (senderInv.viewers.contains(event.whoClicked)) {

            if (event.clickedInventory?.holder !is HumanEntity) {
                return
            }
            if (event.currentItem == null) {
                return
            }
            for (i in 1..12) {
                if (senderInv.contents[farTrade.senderOffer[i]] == null) {
                    event.isCancelled = true
                    senderInv.setItem(farTrade.senderOffer[i], event.currentItem)
                    event.currentItem!!.amount = 0
                    farTrade.updateInventory(senderInv, receiverInv)
                    farTrade.updateInventory(receiverInv, senderInv)
                    return
                }
            }
        }
    }
    @EventHandler
    fun acceptOrDeclineEvent(event: InventoryClickEvent ) {
        if (event.clickedInventory?.holder !is TradeInventory && event.clickedInventory?.holder !is TradeInventoryReceiver) {
            return
        }
        if (event.slot == 46) {
            event.isCancelled = true
            event.whoClicked.closeInventory()
        }
    }
    @EventHandler
    fun InventoryCloseEvent(event: InventoryCloseEvent) {
        if(event.inventory.holder !is TradeInventory && event.inventory.holder !is TradeInventoryReceiver) {
            return
        }
        event.player.openInventory(event.inventory)
    }
    @EventHandler
    fun inventoryOpenEvent(event: InventoryOpenEvent) {
        val list = event.player.inventory.contents
        if (list.isEmpty()) {
            return
        }
        for (item in list) {
            if (item == null) {
                continue
            }
            if (item.isSimilar(farTrade.blackglass)) {
                event.player.inventory.remove(item)
            }
        }
    }

}
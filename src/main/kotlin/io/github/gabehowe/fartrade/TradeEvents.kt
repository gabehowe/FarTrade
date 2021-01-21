package io.github.gabehowe.fartrade

import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent

class TradeEvents(private val farTrade: FarTrade) : Listener {
    @EventHandler
    fun inventoryClickEvent(event: InventoryClickEvent) {
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
            farTrade.updateInventory(farTrade.senderInv, farTrade.receiverInv)
            farTrade.updateInventory(farTrade.receiverInv, farTrade.senderInv)
            return
        }
        if (farTrade.receiverInv.viewers.contains(event.whoClicked)) {
            if (event.clickedInventory?.holder !is HumanEntity) {
                return
            }
            if (event.currentItem == null) {
                return
            }
            for (i in 1..12) {
                if (farTrade.receiverInv.contents[farTrade.senderOffer[i]] == null) {
                    event.isCancelled = true
                    farTrade.receiverInv.setItem(farTrade.senderOffer[i], event.currentItem)
                    event.currentItem!!.amount = 0
                    farTrade.updateInventory(farTrade.senderInv, farTrade.receiverInv)
                    farTrade.updateInventory(farTrade.receiverInv, farTrade.senderInv)
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
            farTrade.updateInventory(farTrade.senderInv, farTrade.receiverInv)
            farTrade.updateInventory(farTrade.receiverInv, farTrade.senderInv)
            return
        }
        if (farTrade.senderInv.viewers.contains(event.whoClicked)) {

            if (event.clickedInventory?.holder !is HumanEntity) {
                return
            }
            if (event.currentItem == null) {
                return
            }
            for (i in 1..12) {
                if (farTrade.senderInv.contents[farTrade.senderOffer[i]] == null) {
                    event.isCancelled = true
                    farTrade.senderInv.setItem(farTrade.senderOffer[i], event.currentItem)
                    event.currentItem!!.amount = 0
                    farTrade.updateInventory(farTrade.senderInv, farTrade.receiverInv)
                    farTrade.updateInventory(farTrade.receiverInv, farTrade.senderInv)
                    return
                }
            }
        }
    }
    @EventHandler
    fun acceptOrDeclineEvent(event: InventoryClickEvent ) {
        if (event.clickedInventory?.holder !is TradeInventory || event.clickedInventory?.holder !is TradeInventoryReceiver) {
            return
        }
        if (event.slot == 46) {
            event.isCancelled = true
            event.whoClicked.closeInventory()
        }
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
package io.github.gabehowe.fartrade

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import java.util.*

class TradeEvents(private val farTrade: FarTrade) : Listener {
    @EventHandler
    fun inventoryClickEvent(event: InventoryClickEvent) {
        if (!farTrade.tradeMap.any { it.key.first == event.whoClicked.uniqueId || it.key.second == event.whoClicked.uniqueId }) {
            return
        }
        val filter =
            farTrade.tradeMap.filter { it.key.first == event.whoClicked.uniqueId || it.key.second == event.whoClicked.uniqueId }
                .toList()
        if (filter.isEmpty()) {
            return
        }
        val onlyFiltered = filter[0]
        val senderInv = onlyFiltered.second.first
        val receiverInv = onlyFiltered.second.second
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
            for (i in 0..11) {
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
            for (i in 0..11) {
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
    fun acceptOrDeclineEvent(event: InventoryClickEvent) {
        if (!farTrade.tradeMap.any { it.key.first == event.whoClicked.uniqueId || it.key.second == event.whoClicked.uniqueId }) {
            return
        }
        if (event.slot != 46 && event.slot != 48) {
            return
        }
        val filter =
            farTrade.tradeMap.filter { it.key.first == event.whoClicked.uniqueId || it.key.second == event.whoClicked.uniqueId }
                .toList()
        val otherPlayer: UUID
        val otherInventory: Inventory
        val player = event.whoClicked as Player
        if (event.whoClicked.uniqueId == filter[0].first.first) {
            otherPlayer = filter[0].first.second
            otherInventory = filter[0].second.second
        } else {
            otherPlayer = filter[0].first.first
            otherInventory = filter[0].second.first
        }
        if (event.clickedInventory?.getItem(48)?.type == Material.YELLOW_STAINED_GLASS_PANE && otherInventory.getItem(48)?.type == Material.YELLOW_STAINED_GLASS_PANE) {
            farTrade.tradeMap.remove(filter[0].first)
            farTrade.acceptTrade(event.whoClicked.uniqueId, otherPlayer, event.clickedInventory!!, otherInventory)
            event.isCancelled = true
            return
        }
        if (event.slot == 46) {
            farTrade.returnItems(event.whoClicked.uniqueId, otherPlayer, event.clickedInventory!!, otherInventory)
            Bukkit.getPlayer(otherPlayer)!!.sendMessage("§cThe other player cancelled the trade")
            event.whoClicked.sendMessage("§cYou cancelled the trade")
            farTrade.tradeMap.remove(filter[0].first)
            event.isCancelled = true
            Bukkit.getPlayer(otherPlayer)?.closeInventory()
            event.whoClicked.closeInventory()
            Bukkit.getPlayer(otherPlayer)!!.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 0.75f)
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 0.75f)
        }
        if (event.slot == 48) {
            if (event.currentItem?.type != Material.GREEN_STAINED_GLASS_PANE) {
                return
            }
            event.currentItem?.type = Material.YELLOW_STAINED_GLASS_PANE
            otherInventory.getItem(50)?.type = Material.YELLOW_STAINED_GLASS_PANE
            otherInventory.getItem(52)?.type = Material.YELLOW_STAINED_GLASS_PANE
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 1.25f)
            event.isCancelled = true
            if (event.clickedInventory?.getItem(48)?.type == Material.YELLOW_STAINED_GLASS_PANE && otherInventory.getItem(48)?.type == Material.YELLOW_STAINED_GLASS_PANE) {
                farTrade.tradeMap.remove(filter[0].first)
                if (farTrade.countItems(event.clickedInventory!!) > farTrade.getEmptySlots(event.whoClicked as Player) ||
                    farTrade.countItems(otherInventory) > farTrade.getEmptySlots(Bukkit.getPlayer(otherPlayer)!!)) {
                    farTrade.returnItems(event.whoClicked.uniqueId, otherPlayer, event.clickedInventory!!, otherInventory)
                    Bukkit.getPlayer(otherPlayer)!!.sendMessage("§cThe other player cancelled the trade")
                    event.whoClicked.sendMessage("§cYou and/or the other player didn't have enough space in their inventory")
                    Bukkit.getPlayer(otherPlayer)?.closeInventory()
                    event.whoClicked.closeInventory()
                    return
                }
                Bukkit.getPlayer(otherPlayer)!!.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 1.5f)
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 1.5f)
                farTrade.acceptTrade(filter[0].first.first, filter[0].first.second, filter[0].second.first, filter[0].second.second)
                event.isCancelled = true
                return
            }
        }

    }

    @EventHandler
    fun inventoryCloseEvent(event: InventoryCloseEvent) {
        if (!farTrade.tradeMap.any { it.key.first == event.player.uniqueId || it.key.second == event.player.uniqueId }) {
            return
    }
        val filter =
            farTrade.tradeMap.filter { it.key.first == event.player.uniqueId || it.key.second == event.player.uniqueId }
                .toList()
        val otherPlayer: UUID
        val otherInventory: Inventory
        if (event.player.uniqueId == filter[0].first.first) {
            otherPlayer = filter[0].first.second
            otherInventory = filter[0].second.second
        } else {
            otherPlayer = filter[0].first.first
            otherInventory = filter[0].second.first
        }
        farTrade.returnItems(event.player.uniqueId, otherPlayer, event.inventory, otherInventory)
        farTrade.tradeMap.remove(filter[0].first)
        Bukkit.getPlayer(otherPlayer)?.closeInventory()
        Bukkit.getPlayer(otherPlayer)!!.sendMessage("§cThe other player cancelled the trade")
        event.player.sendMessage("§cYou cancelled the trade")
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

    @EventHandler
    fun itemPickUpEvent(event: EntityPickupItemEvent) {
        if (event.entityType != EntityType.PLAYER) {
            return
        }
        if (!farTrade.tradeMap.any { it.key.first == event.entity.uniqueId || it.key.first == event.entity.uniqueId }) {
            return
        }
        event.isCancelled = true

    }

}
package io.github.gabehowe.fartrade

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class FarTrade : JavaPlugin() {
    private lateinit var economy: Economy
    var blackglass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
    var blackglassmeta = blackglass.itemMeta
    var greenglass = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
    var greenglassmeta = blackglass.itemMeta
    var redglass = ItemStack(Material.RED_STAINED_GLASS_PANE)
    var redglassmeta = blackglass.itemMeta
    var tradeMap = mutableMapOf<Pair<UUID, UUID>, Pair<Inventory, Inventory>>()
    var voteMap = mutableMapOf<Pair<UUID, UUID>, Boolean>()

    val senderOffer = mutableListOf<Int>()
    override fun onEnable() {
        var e = 10
        senderOffer.add(10)
        var timeCount = 0
        while (e in 9..39) {
            timeCount += 1
            senderOffer.add(e)
            if (timeCount == 3) {
                e += 7
                timeCount = 0
                continue
            }
            e += 1
        }
        blackglassmeta?.setDisplayName(" ")
        blackglass.itemMeta = blackglassmeta
        redglassmeta?.setDisplayName("§cDecline")
        redglass.itemMeta = redglassmeta
        greenglassmeta?.setDisplayName("§2Accept")
        greenglass.itemMeta = greenglassmeta
        getCommand("trade")?.setExecutor(TradeCommand(this))
        logger.severe("hi how are you doing")
        server.pluginManager.registerEvents(TradeEvents(this), this)

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun setupEconomy(): Boolean {
        val rsp: RegisteredServiceProvider<Economy> = server.servicesManager.getRegistration(Economy::class.java)
            ?: return false
        economy = rsp.provider
        @Suppress("SENSELESS_COMPARISON")
        return economy != null
    }


    fun updateInventory(fromInventory: Inventory, toInventory: Inventory) {
        var e = 10
        var timeCount = 0
        while (e in 10..39) {
            val item = fromInventory.getItem(e)
            val newIndex = e + 4
            toInventory.setItem(newIndex, item)
            timeCount += 1
            if (timeCount == 3) {
                e += 7
                timeCount = 0
                continue
            }
            e += 1
        }
    }

    fun newTrade(senderUUID: UUID, receiverUUID: UUID) {
        val uuidPair = Pair(senderUUID, receiverUUID)
        val invPair = Pair(
            Bukkit.createInventory(TradeInventory(this), 54, "Sender Inventory"),
            Bukkit.createInventory(TradeInventoryReceiver(this), 54, "Receiver Inventory")
        )
        tradeMap[uuidPair] = invPair
        initialize(senderUUID, receiverUUID, tradeMap[uuidPair]!!.first)
        initialize(receiverUUID, senderUUID, tradeMap[uuidPair]!!.second)
        Bukkit.getPlayer(senderUUID)?.openInventory(tradeMap[uuidPair]!!.first)
        Bukkit.getPlayer(receiverUUID)?.openInventory(tradeMap[uuidPair]!!.second)
    }

    fun acceptTrade(uuid: UUID, uuid2: UUID, inventory: Inventory, inventory2: Inventory) {
        for (i in 0..12) {
            val item = inventory.getItem(senderOffer[i]) ?: continue
            Bukkit.getPlayer(uuid2)?.inventory?.addItem(item)
            item.amount = 0
        }
        for (i in 0..12) {
            val item = inventory2.getItem(senderOffer[i]) ?: continue
            Bukkit.getPlayer(uuid)?.inventory?.addItem(item)
            item.amount = 0
        }
        Bukkit.getPlayer(uuid)?.closeInventory()
        Bukkit.getPlayer(uuid2)?.closeInventory()
        Bukkit.getPlayer(uuid)?.sendMessage("§aTrade Accepted")
        Bukkit.getPlayer(uuid2)?.sendMessage("§aTrade Accepted")
    }

    fun returnItems(uuid: UUID, uuid2: UUID, inventory: Inventory, inventory2: Inventory) {
        for (i in 0..12) {
            val item = inventory.getItem(senderOffer[i]) ?: continue
            Bukkit.getPlayer(uuid)?.inventory?.addItem(item)
            item.amount = 0
        }
        for (i in 0..12) {
            val item = inventory2.getItem(senderOffer[i]) ?: continue
            Bukkit.getPlayer(uuid2)?.inventory?.addItem(item)
            item.amount = 0
        }
    }

    fun initialize(seeingPlayer: UUID, player2: UUID, inventory: Inventory) {
        for (i in 45..53) {
            inventory.setItem(i, blackglass)
        }
        for (i in 0..8) {
            inventory.setItem(i, blackglass)
        }
        var lSide = 9
        while (lSide <= 36) {
            inventory.setItem(lSide, blackglass)
            lSide += 9
        }
        var mSide = 13
        while (mSide <= 40) {
            inventory.setItem(mSide, blackglass)
            mSide += 9
        }
        var rSide = 17
        while (rSide <= 44) {
            inventory.setItem(rSide, blackglass)
            rSide += 9
        }
        val playerhead = ItemStack(Material.PLAYER_HEAD)
        val playerheadmeta = playerhead.itemMeta as SkullMeta?
        playerheadmeta!!.owningPlayer = Bukkit.getPlayer(seeingPlayer)
        playerheadmeta.setDisplayName("§6${Bukkit.getPlayer(seeingPlayer)?.displayName}")
        playerhead.itemMeta = playerheadmeta
        inventory.setItem(47, playerhead)
        inventory.setItem(46, redglass)
        inventory.setItem(48, greenglass)
        val playerhead2 = ItemStack(Material.PLAYER_HEAD)
        val playerheadmeta2 = playerhead2.itemMeta as SkullMeta?
        playerheadmeta2!!.owningPlayer = Bukkit.getPlayer(player2)
        playerheadmeta2.setDisplayName("§6${Bukkit.getPlayer(player2)?.displayName}")
        playerhead2.itemMeta = playerheadmeta2
        inventory.setItem(51, playerhead2)

    }

}
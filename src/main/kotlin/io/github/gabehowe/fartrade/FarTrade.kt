package io.github.gabehowe.fartrade

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin

class FarTrade : JavaPlugin() {
    private lateinit var economy: Economy
    var blackglass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
    var blackglassmeta = blackglass.itemMeta
    val senderInv: Inventory = Bukkit.createInventory(TradeInventory(this), 54, "Sender Inventory")
    val receiverInv: Inventory = Bukkit.createInventory(TradeInventoryReceiver(this), 54, "Receiver Inventory")
    val senderOffer = mutableListOf<Int>()
    override fun onEnable() {
        var e = 10
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

    fun initialize(displayName: String, inventory: Inventory) {
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
        playerheadmeta!!.owningPlayer = Bukkit.getPlayer(displayName)
        playerheadmeta.setDisplayName("${displayName}'s side")
        playerhead.itemMeta = playerheadmeta
        senderInv.setItem(47, playerhead)

    }

}
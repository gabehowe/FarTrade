package io.github.gabehowe.fartrade

import Treasury
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class FarTrade : JavaPlugin() {
    private lateinit var economy: Economy
    lateinit var treasury: Treasury
    var blackglass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
    var blackglassmeta = blackglass.itemMeta
    var greenglass = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
    var greenglassmeta = blackglass.itemMeta
    var redglass = ItemStack(Material.RED_STAINED_GLASS_PANE)
    var redglassmeta = blackglass.itemMeta
    var tradeMap = mutableMapOf<Pair<UUID, UUID>, Pair<Inventory, Inventory>>()
    var voteMap = mutableMapOf<Pair<UUID, UUID>, Boolean>()
    var voteSessionList = mutableListOf<UUID>()
    val voteTime: Number
        get() {
            return (config.get("vote-time") as Number?)!!
        }
    val distanceBuffer: Number
        get() {
            return (config.get("distance-buffer") as Number?)!!
        }
    val distancePrice: Number
        get() {
            return (config.get("distance-price") as Number?)!!
        }
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
        server.pluginManager.registerEvents(TradeEvents(this), this)
        saveDefaultConfig()
        setupEconomy()
        treasury = getPlugin(Treasury::class.java)
        if (!setupEconomy()) {
            logger.severe(
                String.format(
                    "Disabled due to no Vault dependency found!",
                    description.name
                )
            )
            server.pluginManager.disablePlugin(this)
            return
        }


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
    /*private fun setupTreasury(): Boolean {
        val rsp: RegisteredServiceProvider<Treasury> = server.servicesManager.getRegistration(Treasury::class.java)
            ?: return false
        treasury = rsp.provider
        @Suppress("SENSELESS_COMPARISON")
        return treasury != null
    }*/


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
        val p1 = Bukkit.getPlayer(uuid)!!
        val p2 = Bukkit.getPlayer(uuid2)!!
        var distanceCost =
            ((Math.round(((distancePrice.toDouble() * p1.location.distance(p2.location) - distancePrice.toDouble() * distanceBuffer.toDouble())) * 100.0) / 100.0))
        if (distanceCost < 0) {
            distanceCost = 0.00
        }
        if (economy.getBalance(p1) < distanceCost) {
            Bukkit.getPlayer(uuid)?.closeInventory()
            Bukkit.getPlayer(uuid2)?.closeInventory()
            Bukkit.getPlayer(uuid)?.sendMessage("§cYou didn't have enough money to trade")
            Bukkit.getPlayer(uuid2)
                ?.sendMessage("§c${Bukkit.getPlayer(uuid)?.displayName}§c didn't have enough money to trade")
            return
        }
        for (i in 0..12) {
            val item = inventory.getItem(senderOffer[i]) ?: continue
            Bukkit.getPlayer(uuid2)?.inventory?.addItem(item)
        }
        for (i in 0..12) {
            val item = inventory2.getItem(senderOffer[i]) ?: continue
            Bukkit.getPlayer(uuid)?.inventory?.addItem(item)
        }
        economy.withdrawPlayer(p1, distanceCost)
        treasury.reserveBalance += distanceCost
        Bukkit.getPlayer(uuid)?.closeInventory()
        Bukkit.getPlayer(uuid2)?.closeInventory()
        Bukkit.getPlayer(uuid)?.sendMessage("§aTrade Accepted, $${distanceCost} withdrawn from your account")
        Bukkit.getPlayer(uuid2)?.sendMessage("§aTrade Accepted")
    }

    fun returnItems(uuid: UUID, uuid2: UUID, inventory: Inventory, inventory2: Inventory) {
        val p1 = Bukkit.getPlayer(uuid)!!
        val p2 = Bukkit.getPlayer(uuid2)!!
        for (i in 0..12) {
            val item = inventory.getItem(senderOffer[i]) ?: continue
            p1.inventory.addItem(item)
        }
        for (i in 0..12) {
            val item = inventory2.getItem(senderOffer[i]) ?: continue
            p2.inventory.addItem(item)
        }
    }

    private fun initialize(seeingPlayer: UUID, player2: UUID, inventory: Inventory) {
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
        val goldNugget = ItemStack(Material.GOLD_NUGGET)
        val nuggetMeta = goldNugget.itemMeta
        nuggetMeta?.setDisplayName("§6Trade Price")
        val mutableList = mutableListOf<String>()
        mutableList.add(
            "§6$${
                Math.round(
                    (distancePrice.toDouble() * Bukkit.getPlayer(seeingPlayer)?.location?.distance(
                        Bukkit.getPlayer(player2)?.location!!
                    )!!) * 100.0
                ) / 100.0
            }"
        )
        mutableList.add("§6only the sending player")
        mutableList.add("§6has to pay this")
        nuggetMeta?.lore = mutableList
        goldNugget.itemMeta = nuggetMeta
        inventory.setItem(31, goldNugget)
        val playerhead = ItemStack(Material.PLAYER_HEAD)
        val playerheadmeta = playerhead.itemMeta as SkullMeta?
        playerheadmeta!!.owningPlayer = Bukkit.getPlayer(seeingPlayer)
        playerheadmeta.setDisplayName("§6${Bukkit.getPlayer(seeingPlayer)?.displayName}§c")
        playerhead.itemMeta = playerheadmeta
        inventory.setItem(47, playerhead)
        inventory.setItem(46, redglass)
        inventory.setItem(48, greenglass)
        val playerhead2 = ItemStack(Material.PLAYER_HEAD)
        val playerheadmeta2 = playerhead2.itemMeta as SkullMeta?
        playerheadmeta2!!.owningPlayer = Bukkit.getPlayer(player2)
        playerheadmeta2.setDisplayName("§6${Bukkit.getPlayer(player2)?.displayName}§c")
        playerhead2.itemMeta = playerheadmeta2
        inventory.setItem(51, playerhead2)

    }

    fun getEmptySlots(p: Player): Int {
        val inventory = p.inventory
        val cont = inventory.contents
        var i = 0
        for (item in cont) if (item != null && item.type != Material.AIR) {
            i++
        }
        return 36 - i
    }

    fun countItems(inventory: Inventory): Int {
        var itemsNumber = 0
        for (i in 0..12) {
            val item = inventory.getItem(senderOffer[i]) ?: continue
            itemsNumber++
        }
        return itemsNumber
    }

}
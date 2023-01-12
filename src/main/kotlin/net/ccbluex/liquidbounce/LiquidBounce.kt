package net.ccbluex.liquidbounce

import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.chat.Chat
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.misc.FriendManager
import net.ccbluex.liquidbounce.features.misc.ProxyManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.tabs.Tabs
import net.ccbluex.liquidbounce.render.engine.RenderEngine
import net.ccbluex.liquidbounce.render.ultralight.UltralightEngine
import net.ccbluex.liquidbounce.render.ultralight.theme.ThemeManager
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.block.ChunkScanner
import net.ccbluex.liquidbounce.utils.block.WorldChangeNotifier
import net.ccbluex.liquidbounce.utils.combat.globalEnemyConfigurable
import net.ccbluex.liquidbounce.utils.mappings.McMappings
import org.apache.logging.log4j.LogManager
import kotlin.system.exitProcess


object LiquidBounce : Listenable {

    const val CLIENT_NAME = "CatBounce"
    const val CLIENT_VERSION = "1.0.0"
    const val CLIENT_AUTHOR = "CMCat"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"

    /**
     * Client logger to print out console messages
     */
    val logger = LogManager.getLogger(CLIENT_NAME)!!

    /**
     * Should be executed to start the client.
     */
    val startHandler = handler<ClientStartEvent> {
        runCatching {
            logger.info("Launching $CLIENT_NAME v$CLIENT_VERSION by $CLIENT_AUTHOR")
            logger.debug("Loading from cloud: '$CLIENT_CLOUD'")

            // Load mappings
            McMappings.load()

            // Initialize client features
            EventManager

            // Config
            ConfigSystem
            globalEnemyConfigurable

            RotationManager

            ChunkScanner
            WorldChangeNotifier

            // Features
            ModuleManager
            CommandManager
            ThemeManager
            ScriptManager
            RotationManager
            FriendManager
            ProxyManager
            Tabs
            Chat

            // Initialize the render engine
            RenderEngine.init()

            // Load up web platform
            UltralightEngine.init()

            // Register commands and modules
            CommandManager.registerInbuilt()
            ModuleManager.registerInbuilt()

            // Load user scripts
            ScriptManager.loadScripts()

            // Load config system from disk
            ConfigSystem.load()

            // Connect to chat server
            Chat.connectAsync()
        }.onSuccess {
            logger.info("Successfully loaded client!")
        }.onFailure {
            logger.error("Unable to load client.", it)
            exitProcess(1)
        }
    }

    /**
     * Should be executed to stop the client.
     */
    val shutdownHandler = handler<ClientShutdownEvent> {
        logger.info("Shutting down client...")
        ConfigSystem.store()
        UltralightEngine.shutdown()

        ChunkScanner.ChunkScannerThread.stopThread()
    }

}

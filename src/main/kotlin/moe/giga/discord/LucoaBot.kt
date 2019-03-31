package moe.giga.discord

import io.github.classgraph.ClassGraph
import moe.giga.discord.commands.Command
import moe.giga.discord.listeners.BotListener
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.hooks.AnnotatedEventManager
import org.pmw.tinylog.Logger
import javax.security.auth.login.LoginException


object LucoaBot {
    //public static final int NORMAL_SHUTDOWN = 0;

    private const val UNABLE_TO_CONNECT = 110
    private const val BAD_TOKEN = 111

    lateinit var handler: Handler

    val statistics = BotStatistics()

    fun setupBot() {
        val settings = SettingsManager.instance.settings
        try {
            val jdaBuilder = JDABuilder(AccountType.BOT).setToken(settings.botToken)

            jdaBuilder.setEventManager(AnnotatedEventManager())

            handler = Handler(findCommands().apply { forEach { it.init(jdaBuilder) } })
            jdaBuilder.addEventListener(handler)

            findListeners { clazz ->
                jdaBuilder.addEventListener(clazz.getDeclaredConstructor().newInstance())
            }

            jdaBuilder.build()
        } catch (e: LoginException) {
            e.printStackTrace()
            Logger.error("The bot token provided was most likely incorrect.")
            System.exit(BAD_TOKEN)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            System.exit(UNABLE_TO_CONNECT)
        }

    }

    private fun findCommands(): List<Command> {
        val commands = mutableListOf<Command>()
        ClassGraph()
                //.verbose()             // Enable verbose logging
                .enableClassInfo()
                .whitelistPackages("moe.giga.discord.commands")    // Scan com.xyz and subpackages
                .scan().use { scanResult ->
                    val routes = scanResult.getClassesImplementing(Command::class.java.name)
                    routes.loadClasses(Command::class.java).forEach {
                        commands.add(it.getDeclaredConstructor().newInstance())
                    }
                }
        return commands
    }

    private fun findListeners(action: (Class<*>) -> Unit) {
        ClassGraph()
                //.verbose()             // Enable verbose logging
                .enableClassInfo()
                .enableAnnotationInfo()
                .whitelistPackages("moe.giga.discord.listeners")    // Scan com.xyz and subpackages
                .scan().use { scanResult ->
                    val routes = scanResult.getClassesWithAnnotation(BotListener::class.java.name)
                    routes.loadClasses().forEach(action)
                }
    }
}

fun main() {
    if (System.getProperty("file.encoding") == "UTF-8") {
        LucoaBot.setupBot()
    } else {
        Logger.error("Please relaunch with file.encoding set to UTF-8")
    }
}


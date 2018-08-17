package moe.giga.discord

import io.github.classgraph.ClassGraph
import kotliquery.HikariCP
import moe.giga.discord.commands.Command
import moe.giga.discord.listeners.BotListener
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.hooks.AnnotatedEventManager
import org.pmw.tinylog.Logger
import javax.security.auth.login.LoginException


object LucoaBot {
    //public static final int NORMAL_SHUTDOWN = 0;

    private const val INVALID_SETTINGS = 100

    private const val UNABLE_TO_CONNECT = 110
    private const val BAD_TOKEN = 111

    lateinit var handler: Handler

    val statistics = BotStatistics()

    @JvmStatic
    fun main(args: Array<String>) {
        if (System.getProperty("file.encoding") == "UTF-8") {
            setupBot()
        } else {
            Logger.error("Please relaunch with file.encoding set to UTF-8")
        }
    }

    private fun setupBot() {
        val settings = SettingsManager.instance.settings
        try {
            if (settings.datasource == null) {
                System.exit(LucoaBot.INVALID_SETTINGS)
                return
            }

            // TODO: clean this up
            HikariCP.default(settings.datasource, "", "")

            val jdaBuilder = JDABuilder(AccountType.BOT).setToken(settings.botToken)

            jdaBuilder.setEventManager(AnnotatedEventManager())

            handler = Handler(findCommands().apply { forEach { it.init(jdaBuilder) } })
            jdaBuilder.addEventListener(handler)

            findAnnotation("moe.giga.discord.listeners", BotListener::class.java) { clazz ->
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
        findImplementing("moe.giga.discord.commands", Command::class.java) { clazz ->
            commands.add(clazz.getDeclaredConstructor().newInstance())
        }
        return commands
    }

    private fun findAnnotation(path: String, annotation: Class<*>, action: (Class<*>) -> Unit) {
        ClassGraph()
                .verbose()             // Enable verbose logging
                .enableAllInfo()       // Scan classes, methods, fields, annotations
                .whitelistPackages(path)    // Scan com.xyz and subpackages
                .scan().use { scanResult ->
                    val routes = scanResult.getClassesWithAnnotation(annotation.name)
                    routes.loadClasses().forEach(action)
                }
    }

    private fun <T> findImplementing(path: String, interfaceClass: Class<T>, action: (Class<out T>) -> Unit) {
        ClassGraph()
                .verbose()             // Enable verbose logging
                .enableAllInfo()       // Scan classes, methods, fields, annotations
                .whitelistPackages(path)    // Scan com.xyz and subpackages
                .scan().use { scanResult ->
                    val routes = scanResult.getClassesImplementing(interfaceClass.name)
                    routes.loadClasses(interfaceClass).forEach(action)
                }
    }
}

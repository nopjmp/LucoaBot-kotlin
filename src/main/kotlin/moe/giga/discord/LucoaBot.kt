package moe.giga.discord

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import kotliquery.HikariCP
import moe.giga.discord.commands.Command
import moe.giga.discord.listeners.BotListener
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.hooks.AnnotatedEventManager
import org.pmw.tinylog.Logger
import java.lang.annotation.RetentionPolicy
import javax.security.auth.login.LoginException

object LucoaBot {
    //public static final int NORMAL_SHUTDOWN = 0;

    internal const val INVALID_SETTINGS = 100

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
            if (settings.datasource == null || settings.username == null || settings.password == null) {
                System.exit(LucoaBot.INVALID_SETTINGS)
                return
            }

            // TODO: clean this up
            HikariCP.default(settings.datasource, settings.username, settings.password)

            val jdaBuilder = JDABuilder(AccountType.BOT).setToken(settings.botToken)

            jdaBuilder.setEventManager(AnnotatedEventManager())

            handler = Handler(findCommands().apply { forEach { it.init(jdaBuilder) } })
            jdaBuilder.addEventListener(handler)

            findAnnotation("moe.giga.discord.listeners", BotListener::class.java) { clazz ->
                jdaBuilder.addEventListener(clazz.getDeclaredConstructor().newInstance())
            }

            jdaBuilder.buildAsync()
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

    private fun <T> findAnnotation(path: String, clazz: Class<T>, action: (Class<*>) -> Unit) {
        FastClasspathScanner(path)
                .setAnnotationVisibility(RetentionPolicy.RUNTIME)
                .matchClassesWithAnnotation(clazz, action)
                .scan()
    }

    private fun <T> findImplementing(path: String, clazz: Class<T>, action: (Class<out T>) -> Unit) {
        FastClasspathScanner(path)
                .setAnnotationVisibility(RetentionPolicy.RUNTIME)
                .matchClassesImplementing(clazz, action)
                .scan()
    }
}

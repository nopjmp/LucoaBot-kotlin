package moe.giga.discord

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.commands.Command
import moe.giga.discord.util.Database
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
            Database // initialise database first to prevent recursion
            setupBot()
        } else {
            Logger.error("Please relaunch with file.encoding set to UTF-8")
        }
    }

    private fun setupBot() {
        val settings = SettingsManager.instance.settings
        try {
            val jdaBuilder = JDABuilder(AccountType.BOT).setToken(settings.botToken)

            handler = Handler(findCommands().apply { forEach { it.init(jdaBuilder) } })
            jdaBuilder.setEventManager(AnnotatedEventManager())
            jdaBuilder.addEventListener(handler)

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
        FastClasspathScanner("moe.giga.discord.commands")
                .setAnnotationVisibility(RetentionPolicy.RUNTIME)
                .matchClassesWithAnnotation(IsCommand::class.java) { clazz ->
                    commands.add(clazz.getDeclaredConstructor().newInstance() as Command)
                }
                .scan()
        return commands
    }
}

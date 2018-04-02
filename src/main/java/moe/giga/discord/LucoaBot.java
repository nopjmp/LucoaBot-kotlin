package moe.giga.discord;

import moe.giga.discord.annotations.CommandInfo;
import moe.giga.discord.commands.Command;
import moe.giga.discord.commands.CommandData;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.pmw.tinylog.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public final class LucoaBot {
    //public static final int NORMAL_SHUTDOWN = 0;

    public static final int NEWLY_CREATED_CONFIG = 100;

    public static final int UNABLE_TO_CONNECT = 110;
    public static final int BAD_TOKEN = 111;
    private static JDA api;

    public static void main(String[] args) {
        if (System.getProperty("file.encoding").equals("UTF-8")) {
            setupBot();
        } else {
            Logger.error("Please relaunch with file.encoding set to UTF-8");
        }
    }

    private static void setupBot() {
        try {
            Settings settings = SettingsManager.getInstance().getSettings();
            JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT).setToken(settings.getBotToken());

            jdaBuilder.addEventListener(new CommandHandler(findCommands()));

            api = jdaBuilder.buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
            Logger.error("The bot token provided was incorrect.");
            System.exit(BAD_TOKEN);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(UNABLE_TO_CONNECT);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private static List<CommandData> findCommands() throws IOException, IllegalAccessException, InstantiationException {
        List<Class<?>> packageClasses = getAllClassesInPackageContaining(Command.class);

        List<CommandData> commands = new ArrayList<>();

        for (Class<?> clazz : packageClasses) {
            CommandInfo commandInfo = clazz.getAnnotation(CommandInfo.class);
            if (commandInfo == null) continue;

            commands.add(new CommandData((Command)clazz.newInstance(), commandInfo));
        }

        return commands;
    }

    private static List<Class<?>> getAllClassesInPackageContaining(Class<?> clazz)
            throws IOException
    {
        String clazzPackageName = clazz
                .getPackage()
                .getName();

        String clazzPath = clazz
                .getResource(".")
                .getPath();

        Path packagePath = Paths.get(clazzPath);

        final List<Class<?>> packageClasses = new ArrayList<>();

        Files.walkFileTree(packagePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(
                    Path file, BasicFileAttributes attrs)
                    throws IOException
            {
                String filename =
                        file.getName(file.getNameCount()-1).toString();

                if (filename.endsWith(".class")) {
                    String className = filename.replace(".class", "");

                    try {
                        Class<?> loadedClazz = Class.forName(
                                clazzPackageName + "." + className);

                        packageClasses.add(loadedClazz);
                    }
                    catch(ClassNotFoundException e) {
                        System.err.println(
                                "class not found: " + e.getMessage());
                    }
                }

                return super.visitFile(file, attrs);
            }
        });

        return packageClasses;
    }
}

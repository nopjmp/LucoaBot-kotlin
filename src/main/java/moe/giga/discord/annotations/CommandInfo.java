package moe.giga.discord.annotations;

import moe.giga.discord.permissions.AccessLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    String name();
    String description() default "";
    String usage() default "";
    String[] aliases() default {};

    boolean hidden() default false;
    boolean allowBots() default false;
    AccessLevel level() default AccessLevel.USER;
}

package ru.berdnikov.telegrambot.util.botSetup;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public interface BotCommands {
    List<BotCommand> LIST_OF_COMMANDS = List.of(
            new BotCommand("/start", "start bot"),
            new BotCommand("/logout", "Log out"),
            new BotCommand("/login", "Log in"),
            new BotCommand("/signin", "Sign in"),
            new BotCommand("/items", "Saved items"),
            new BotCommand("/newitem", "Add new item")
    );

    String LOGIN = "Input login and password like: login:yourlogin password:yourpassword";
    String SIGNIN = "Input login and password like: signin:yourlogin password:yourpassword";
    String NEW_ITEM = "Input link from filtorg.ru or 76monet.ru";

}

package ru.berdnikov.telegrambot.util.botSetup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BotConfig {
    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String token;

    public String getBotName() {
        return botName;
    }
    public String getToken() {
        return token;
    }
}

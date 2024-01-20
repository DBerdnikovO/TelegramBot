package ru.berdnikov.telegrambot.util.errors;

public class TelegramBotConfigNotCreated extends RuntimeException{
    public TelegramBotConfigNotCreated(String message) {
        super(message);
    }
}

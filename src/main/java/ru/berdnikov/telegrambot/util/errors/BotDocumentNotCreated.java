package ru.berdnikov.telegrambot.util.errors;

import java.io.IOException;

public class BotDocumentNotCreated extends RuntimeException {
    public BotDocumentNotCreated(String message) {
        super(message);
    }
}

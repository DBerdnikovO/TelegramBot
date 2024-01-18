package ru.berdnikov.telegrambot.util.button;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class Buttons {
    private static final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

    private static final InlineKeyboardButton START_BUTTON = new InlineKeyboardButton("Start");
    private static final InlineKeyboardButton HELP_BUTTON = new InlineKeyboardButton("Help");
    private static final InlineKeyboardButton LOGOUT_BUTTON = new InlineKeyboardButton("Logout");
    private static final InlineKeyboardButton LOGIN_BUTTON = new InlineKeyboardButton("Login");
    private static final InlineKeyboardButton SIGNIN_BUTTON = new InlineKeyboardButton("Signin");
    private static final InlineKeyboardButton ITEMS_BUTTON = new InlineKeyboardButton("items");


    public static InlineKeyboardMarkup inlineMarkup() {
        START_BUTTON.setCallbackData("/start");
        HELP_BUTTON.setCallbackData("/help");
        LOGOUT_BUTTON.setCallbackData("/logout");
        LOGIN_BUTTON.setCallbackData("/login");
        SIGNIN_BUTTON.setCallbackData("/signin");
        ITEMS_BUTTON.setCallbackData("/items");

        List<InlineKeyboardButton> rowInline = List.of(START_BUTTON, HELP_BUTTON, LOGOUT_BUTTON);
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }

//    public static ReplyKeyboardMarkup keyboardMarkup() {
//        List<KeyboardRow> keyboard = new ArrayList<>();
//
//        KeyboardRow row1 = new KeyboardRow();
//        row1.add("Login");
//        row1.add("Signin");
//
//        keyboard.add(row1);
//
//        keyboardMarkup.setKeyboard(keyboard);
//
//        return keyboardMarkup;
//    }
}
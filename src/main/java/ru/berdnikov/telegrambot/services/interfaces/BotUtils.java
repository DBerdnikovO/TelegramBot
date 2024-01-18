package ru.berdnikov.telegrambot.services.interfaces;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;

import java.io.IOException;

public interface BotUtils {
    void fileExist(String path);
    Font fontCreate() throws DocumentException, IOException;
}

package ru.berdnikov.telegrambot.services.interfaces;

import com.itextpdf.text.DocumentException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.berdnikov.telegrambot.dto.ResponseDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface BotCommandService {
    SendMessage startCommandReceived(Long chatId, String name);
    SendMessage sendMessage(Long chatId, String textToSend);
    File sendPdf(List<ResponseDTO> responseDTOList) throws IOException, DocumentException;
}

package ru.berdnikov.telegrambot.services.botService;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berdnikov.telegrambot.dto.ItemDTO;
import ru.berdnikov.telegrambot.services.itemService.ItemService;

import java.io.File;

@Service
@Transactional
public class TelegramResponseService {

    private final TelegramBotResponse telegramBotResponse;
    private final ItemService itemService;

    public TelegramResponseService(TelegramBotResponse telegramBotResponse, ItemService itemService) {
        this.telegramBotResponse = telegramBotResponse;
        this.itemService = itemService;
    }

    @Transactional(readOnly = true)
    public File searchAllByMoneyName(String money) {
        return telegramBotResponse.startSearchAndGeneratePdf(money);
    }

    public ItemDTO saveByLink(String url) {
        return telegramBotResponse.saveByLink(url);
    }

    @Transactional(readOnly = true)
    public File searchAllForUser(int id){
        return telegramBotResponse.sendPdf(itemService.getItemsByPersonId(id));
    }

}

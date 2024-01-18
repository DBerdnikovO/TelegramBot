package ru.berdnikov.telegrambot.services.botServices.check_and_test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.berdnikov.telegrambot.dto.ResponseDTO;
import ru.berdnikov.telegrambot.util.errors.BotDocumentNotCreated;
import ru.berdnikov.telegrambot.util.httpEnums.HttpValues;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumerMoney {
    private final Lock lock = new ReentrantLock();

    public void firstThread(HttpValues values, String money){
        lock.lock();
        search(values, money);
        lock.unlock();
    }

    public void secondThread(HttpValues values, String money){
        lock.lock();
        search(values, money);
        lock.unlock();
    }

    private List<ResponseDTO> search(HttpValues values, String money) {
        List<ResponseDTO> responseDTOS = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(values.url+money).get();
            Elements productCards = doc.select(values.productElements);
            Elements imgElements = doc.select(values.imageElements);
            Elements linkElements = doc.select(values.linkElements);
            for (int i = 0; i < productCards.size(); i++) {
                String title = productCards.get(i).select(values.title).text();
                String  price = productCards.get(i).select(values.price).text();
                String link = values.baseSearchUrl + linkElements.get(i).attr(values.ref);
                String imgSource = imgElements.get(i).attr(values.attr);
                responseDTOS.add(new ResponseDTO(title, price, imgSource, link));
            }
        } catch (IOException e) {
            throw new BotDocumentNotCreated("Document its not created!");
        }
        return responseDTOS;
    }
}

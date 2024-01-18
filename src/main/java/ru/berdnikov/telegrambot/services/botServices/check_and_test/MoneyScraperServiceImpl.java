package ru.berdnikov.telegrambot.services.botServices.check_and_test;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ru.berdnikov.telegrambot.dto.ResponseDTO;
import ru.berdnikov.telegrambot.services.interfaces.ScraperService;
import ru.berdnikov.telegrambot.util.httpEnums.HttpValues;
import ru.berdnikov.telegrambot.util.errors.BotDocumentNotCreated;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MoneyScraperServiceImpl implements ScraperService {


    public List<ResponseDTO> fetchMoney(String money) {
        return futeSearch(money);
    }

    private List<ResponseDTO> futeSearch(String money) {
        long startTime = System.currentTimeMillis();
        List<ResponseDTO> result = search(money);
        long endTime = System.currentTimeMillis();
        System.out.println("futeSearch execution time: " + (endTime - startTime) + " milliseconds");
        return result;
    }

    private List<ResponseDTO> search(String money) {
        int parallelism = Runtime.getRuntime().availableProcessors();
        System.out.println(parallelism);
        ExecutorService executorService = Executors.newFixedThreadPool(parallelism);
        List<HttpValues> httpValuesList = Arrays.asList(HttpValues._76MONET_URL, HttpValues.FILTORG_URL);
        List<ResponseDTO> result = new ArrayList<>();
        try {
            List<Callable<List<ResponseDTO>>> tasks = new ArrayList<>();
            for (HttpValues httpValues : httpValuesList) {
                tasks.add(() -> search(httpValues, money));
            }
            List<Future<List<ResponseDTO>>> futures = executorService.invokeAll(tasks);
            for (Future<List<ResponseDTO>> future : futures) {
                result.addAll(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            executorService.shutdown();
        }
        return result;
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

    public ResponseDTO saveByLink(String url) {
        try {
            Pattern pattern = Pattern.compile("https:.+");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                HttpValues values = null;
                String link = matcher.group();
                if (matcher.group().contains("filtorg")) {
                    values = HttpValues.FILTORG_URL;
                } else if (matcher.group().contains("76monet")) {
                    values = HttpValues._76MONET_URL;
                }
                Document doc = Jsoup.connect(link).get();
                String title = doc.selectFirst(values.title_header).text();
                String price = doc.select(values.price_header).first().text();
                String imgSource = doc.select(values.image_header).first().attr(values.attr_header);
                return new ResponseDTO(title,price,imgSource, link);
            }
            } catch (IOException e) {
                throw new BotDocumentNotCreated("Document its not created!");
            }
        return null;
    }

}

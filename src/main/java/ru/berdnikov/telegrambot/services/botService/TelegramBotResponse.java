package ru.berdnikov.telegrambot.services.botService;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import ru.berdnikov.telegrambot.dto.ItemDTO;
import ru.berdnikov.telegrambot.util.errors.BotDocumentNotCreated;
import ru.berdnikov.telegrambot.util.httpEnums.HttpValues;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBotResponse {

    private final ResourceLoader resourceLoader;

    @Value("${bot.outpuFileName}")
    private String fileName;
    @Value("${bot.pathToErrorImage}")
    private String pathToErrorImage;
    @Value("${bot.baseFontPath}")
    private String pathToFont;

    public TelegramBotResponse(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public File startSearchAndGeneratePdf(String money) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        List<HttpValues> httpValuesList = Arrays.asList(HttpValues._76MONET_URL, HttpValues.FILTORG_URL);
        List<ItemDTO> allItemDTOS = new ArrayList<>();

        try {
            List<CompletableFuture<List<ItemDTO>>> futures = new ArrayList<>();

            for (HttpValues httpValues : httpValuesList) {
                CompletableFuture<List<ItemDTO>> future = CompletableFuture.supplyAsync(() -> search(httpValues, money), executorService);
                futures.add(future);
            }

            for (CompletableFuture<List<ItemDTO>> future : futures) {
                allItemDTOS.addAll(future.join());
            }
        } finally {
            executorService.shutdown();
        }

        return sendPdf(allItemDTOS);
    }

    private List<ItemDTO> search(HttpValues values, String money) {
        List<ItemDTO> itemDTOS = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(values.url + money).get();
            Elements productCards = doc.select(values.productElements);
            Elements imgElements = doc.select(values.imageElements);
            Elements linkElements = doc.select(values.linkElements);
            for (int i = 0; i < productCards.size(); i++) {
                String title = productCards.get(i).select(values.title).text();
                String price = productCards.get(i).select(values.price).text();
                String link = values.baseSearchUrl + linkElements.get(i).attr(values.ref);
                String imgSource = imgElements.get(i).attr(values.attr);
                itemDTOS.add(new ItemDTO(title, price, imgSource, link));
            }
        } catch (IOException e) {
            throw new BotDocumentNotCreated("Document its not created!");
        }
        return itemDTOS;
    }

    public ItemDTO saveByLink(String url) {
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
                return new ItemDTO(title,price,imgSource, link);
            }
        } catch (IOException e) {
            throw new BotDocumentNotCreated(e.getMessage());
        }
        return null;
    }

    public File sendPdf(List<ItemDTO> itemDTOList) {
        fileExist(fileName);

        try (OutputStream outputStream = new FileOutputStream(fileName)) {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            PdfPTable pdfPTable = new PdfPTable(2);

            // Загрузить изображение ошибки в массив байтов
            Resource errorImageResource = resourceLoader.getResource(pathToErrorImage);
            byte[] errorImageBytes = IOUtils.toByteArray(errorImageResource.getInputStream());

            for (ItemDTO itemDTO : itemDTOList) {
                String title = itemDTO.getTitle();
                String price = itemDTO.getPrice();
                String urlOfImage = itemDTO.getImage_url();
                String link = itemDTO.getLink();

                Image image = (!urlOfImage.toLowerCase().endsWith("2_.jpg") && !urlOfImage.toLowerCase().endsWith(".webp")) ? Image.getInstance(urlOfImage) : Image.getInstance(errorImageBytes);
                image.scaleAbsolute(100f, 100f);

                String line = title + ",\n " + price + ",\n" + link;

                PdfPCell pdfPCell1 = new PdfPCell(new Paragraph(line, fontCreate()));
                PdfPCell pdfPCell2 = new PdfPCell(image);

                pdfPCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfPCell2.setHorizontalAlignment(Element.ALIGN_CENTER);

                pdfPTable.addCell(pdfPCell1);
                pdfPTable.addCell(pdfPCell2);
            }

            document.add(pdfPTable);
            document.close();
        } catch (IOException | DocumentException e){
            throw new BotDocumentNotCreated(e.getMessage());
        }

        return new File(fileName);
    }

    public void fileExist(String path) {
        File file = new File(fileName);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Failed to delete existing file: " + fileName);
        }
    }

    public Font fontCreate() throws DocumentException, IOException {
        BaseFont baseFont = BaseFont.createFont(pathToFont, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font = new Font(baseFont);
        font.setSize(16);
        font.setColor(BaseColor.BLACK);
        return font;
    }
}

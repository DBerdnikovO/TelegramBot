package ru.berdnikov.telegrambot.services.botServices;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.berdnikov.telegrambot.dto.ResponseDTO;
import ru.berdnikov.telegrambot.services.interfaces.BotCommandService;
import ru.berdnikov.telegrambot.services.interfaces.BotUtils;

import java.io.*;
import java.util.List;


@Service
public class BotCommandServiceImpl implements BotCommandService, BotUtils {

    @Value("${bot.outpuFileName}")
    private String fileName;

    @Value("${bot.pathToErrorImage}")
    private String pathToErrorImage;

    @Value("${bot.baseFontPath}")
    private String pathToFont;

    private final ResourceLoader resourceLoader;

    @Autowired
    public BotCommandServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public SendMessage startCommandReceived(Long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you!" + "\n" +
                "Enter the currency whose official exchange rate" + "\n" +
                "you want to know in relation to BYN." + "\n" +
                "For example: USD";
        return sendMessage(chatId, answer);
    }

    @Override
    public SendMessage sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        return sendMessage;
    }


    @Override
    public File sendPdf(List<ResponseDTO> responseDTOList) throws IOException, DocumentException {
        fileExist(fileName);

        try (OutputStream outputStream = new FileOutputStream(fileName)) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            PdfPTable pdfPTable = new PdfPTable(2);

            // Загрузить изображение ошибки в массив байтов
            Resource errorImageResource = resourceLoader.getResource(pathToErrorImage);
            byte[] errorImageBytes = IOUtils.toByteArray(errorImageResource.getInputStream());

            for (ResponseDTO responseDTO : responseDTOList) {
                String title = responseDTO.getTitle();
                String price = responseDTO.getPrice();
                String urlOfImage = responseDTO.getImage_url();
                String link = responseDTO.getLink();

                Image image = (!urlOfImage.toLowerCase().endsWith("2_.jpg") && !urlOfImage.toLowerCase().endsWith(".webp"))
                        ? Image.getInstance(urlOfImage) : Image.getInstance(errorImageBytes);
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
        }

        return new File(fileName);
    }

    @Override
    public void fileExist(String path) {
        File file = new File(fileName);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Failed to delete existing file: " + fileName);
        }
    }

    @Override
    public Font fontCreate() throws DocumentException, IOException {
        BaseFont baseFont = BaseFont.createFont(pathToFont, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font = new Font(baseFont);
        font.setSize(16);
        font.setColor(BaseColor.BLACK);
        return font;
    }

}

package ru.berdnikov.telegrambot.controller;

import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.berdnikov.telegrambot.dto.PersonDTO;
import ru.berdnikov.telegrambot.dto.ResponseDTO;
import ru.berdnikov.telegrambot.entity.Person;
import ru.berdnikov.telegrambot.services.botServices.PeopleService;
import ru.berdnikov.telegrambot.services.itemServices.ItemService;
import ru.berdnikov.telegrambot.services.personServices.RegistrationService;
import ru.berdnikov.telegrambot.services.interfaces.BotCommandService;
import ru.berdnikov.telegrambot.services.interfaces.BotCommands;
import ru.berdnikov.telegrambot.services.interfaces.ScraperService;
import ru.berdnikov.telegrambot.util.botSetup.BotConfig;
import ru.berdnikov.telegrambot.util.button.Buttons;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class TelegramBotController extends TelegramLongPollingBot implements BotCommands {

    private final BotConfig botConfig;
    private final ItemService itemService;
    private final ScraperService scraperService;
    private final PeopleService peopleService;
    private final BotCommandService botCommandService;
    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;

    private UsernamePasswordAuthenticationToken authenticationToken = null;

    @Autowired
    public TelegramBotController(BotConfig botConfig,
                                 ItemService itemService,
                                 ScraperService scraperService,
                                 BotCommandService botCommandService,
                                 RegistrationService registrationService,
                                 AuthenticationManager authenticationManager,
                                 PeopleService peopleService) {
        this.botConfig = botConfig;
        this.itemService = itemService;
        this.scraperService = scraperService;
        this.botCommandService = botCommandService;
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
        this.peopleService = peopleService;
        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = 0;
        String userName = null;
        String receivedMessage;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            userName = update.getMessage().getFrom().getFirstName();

            if (update.getMessage().hasText()) {
                receivedMessage = update.getMessage().getText();
                botAnswerUtils(receivedMessage, chatId, userName);
            }

        }  else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            receivedMessage = update.getCallbackQuery().getData();

            botAnswerUtils(receivedMessage, chatId, userName);
        }
    }

    //ИСПОЛЬЗУЙ LINKEDLIST!! для удаления и вывода
    // Или LinkedHaspMap
    // Переделай под многопоточку!
    // Используй producer - consumer паттерн
    // Count down latch!! Смотри semaphore
    private void botAnswerUtils(String receivedMessage, long chatId, String userName) {
        switch (receivedMessage) {
            case "/start" ->
                    startBot(chatId, userName);
            case "/help" ->
                    sendText(chatId, HELP_TEXT);
            case "/logout" ->
                    logout(chatId);
            case "/login" ->
                    sendText(chatId,LOGIN);
            case "/signin" ->
                    sendText(chatId,SIGNIN);
            case "/items" ->
                    sendSavedItem(chatId);
            case "/newitem" ->
                sendText(chatId, NEW_ITEM);
            default ->
                    fetchRequest(receivedMessage, chatId);
        }
    }

    private void fetchRequest(String receivedMessage, long chatId) {
        if (receivedMessage.startsWith("signin")) {
            processSignInCommand(receivedMessage, chatId);
        } else if (receivedMessage.startsWith("login")) {
            processLoginCommand(receivedMessage, chatId);
        } else if (receivedMessage.startsWith("add")){
            createNewItem(chatId,receivedMessage);
        }
        else {
            try {
                Authentication authentication = authenticationManager.authenticate(authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                List<ResponseDTO> responseDTOList = scraperService.fetchMoney(receivedMessage);
                sendPdfDocument(chatId, botCommandService.sendPdf(responseDTOList));
            } catch (IOException | DocumentException | UsernameNotFoundException e) {
                handlePdfDocumentError(chatId, e);
            }
        }
    }

    private void processSignInCommand(String receivedMessage, long chatId) {
        Pattern pattern = Pattern.compile("signin:(\\S+) password:(\\S+)");
        Matcher matcher = pattern.matcher(receivedMessage);
        if (matcher.find()) {
            String login = matcher.group(1);
            String password = matcher.group(2);
            PersonDTO personDTO = new PersonDTO(login, password);
            sendText(chatId, registrationService.register(personDTO));
            authenticateAndSendWelcome(chatId, personDTO);
        }
    }

    private void processLoginCommand(String receivedMessage, long chatId) {
        Pattern pattern = Pattern.compile("login:(\\S+) password:(\\S+)");
        Matcher matcher = pattern.matcher(receivedMessage);
        if (matcher.find()) {
            String login = matcher.group(1);
            String password = matcher.group(2);
            PersonDTO personDTO = new PersonDTO(login, password);
            authenticateAndSendWelcome(chatId, personDTO);
        } else {
            sendText(chatId, "Login and password not found in the input string.");
        }
    }

    private void authenticateAndSendWelcome(long chatId, PersonDTO personDTO) {
        authenticationToken = new UsernamePasswordAuthenticationToken(
                personDTO.getUsername(),
                personDTO.getPassword()
        );
        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            sendText(chatId, "Welcome back " + authentication.getName());
        } catch (UsernameNotFoundException e) {
            sendText(chatId, "User not found!");
        }
    }

    private void logout(long chatId) {
        if (authenticationToken != null) {
            SecurityContextHolder.clearContext();
            sendText(chatId, "Your log out successfully!");
        } else {
            sendText(chatId, "Your not login!");
        }
    }

    private void handlePdfDocumentError(long chatId, Exception e) {
        sendText(chatId, "Error processing PDF document: " + e.getMessage());
    }

    private void startBot(long chatId, String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Hi, " + userName + "! I'm a Telegram bot to track coin prices. " +
                "Please register or log in with your account!");
        message.setReplyMarkup(Buttons.inlineMarkup());
//        message.setReplyMarkup(Buttons.keyboardMarkup());
        try {
            execute(message);
            System.out.println("Reply sent");
        } catch (TelegramApiException e){
            System.out.println(e.getMessage());
        }
    }

    private void sendText(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
            System.out.println("Reply sent");
        } catch (TelegramApiException e){
            System.out.println(e.getMessage());
        }
    }

    private void sendPdfDocument(Long chatId, File save) {

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));

        try (FileInputStream fileInputStream = new FileInputStream(save)) {
            InputFile inputFile = new InputFile(fileInputStream, save.getName());
            sendDocument.setDocument(inputFile);
            execute(sendDocument);
        } catch (FileNotFoundException | TelegramApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createNewItem(long chatId, String message) {
        if (authenticationToken != null) {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Person person = peopleService.getByUsername(authentication.getName());
            ResponseDTO responseDTO = scraperService.saveByLink(message);
            itemService.addItem(responseDTO,person);
            sendText(chatId, "Added");
        } else {
            sendText(chatId, "Your not login");
        }
    }

    private void sendSavedItem(long chatId) {
        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Person person = peopleService.getByUsername(authentication.getName());
            sendPdfDocument(chatId, botCommandService.sendPdf(itemService.getItemsByPersonId(person.getId())));
        } catch (IOException | DocumentException | UsernameNotFoundException e) {
            handlePdfDocumentError(chatId, e);
        }
    }
}
package ru.berdnikov.telegrambot.controller;

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
import ru.berdnikov.telegrambot.dto.ItemDTO;
import ru.berdnikov.telegrambot.dto.PersonDTO;
import ru.berdnikov.telegrambot.entity.Person;
import ru.berdnikov.telegrambot.services.personService.PersonService;
import ru.berdnikov.telegrambot.services.botService.TelegramResponseService;
import ru.berdnikov.telegrambot.services.itemService.ItemService;
import ru.berdnikov.telegrambot.services.personService.PersonRegistrationService;
import ru.berdnikov.telegrambot.util.botSetup.BotCommands;
import ru.berdnikov.telegrambot.util.botSetup.BotConfig;
import ru.berdnikov.telegrambot.util.button.Buttons;
import ru.berdnikov.telegrambot.util.errors.AuthUsernameNotFoundException;
import ru.berdnikov.telegrambot.util.errors.BotDocumentNotCreated;
import ru.berdnikov.telegrambot.util.errors.TelegramBotConfigNotCreated;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class TelegramBotController extends TelegramLongPollingBot implements BotCommands {

    private final BotConfig botConfig;
    private final ItemService itemService;
    private final TelegramResponseService telegramResponseService;
    private final PersonService personService;
    private final PersonRegistrationService personRegistrationService;
    private final AuthenticationManager authenticationManager;

    private UsernamePasswordAuthenticationToken authenticationToken = null;

    @Autowired
    public TelegramBotController(BotConfig botConfig,
                                 ItemService itemService,
                                 TelegramResponseService telegramResponseService,
                                 PersonRegistrationService personRegistrationService,
                                 AuthenticationManager authenticationManager,
                                 PersonService personService) {
        this.botConfig = botConfig;
        this.itemService = itemService;
        this.telegramResponseService = telegramResponseService;
        this.personRegistrationService = personRegistrationService;
        this.authenticationManager = authenticationManager;
        this.personService = personService;
        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e){
            throw new TelegramBotConfigNotCreated(e.getMessage());
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
        long chatId;
        String userName;
        String receivedMessage;

        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            userName = update.getMessage().getFrom().getFirstName();
            receivedMessage = update.getMessage().getText();
            botAnswerUtils(receivedMessage, chatId, userName);
        }  else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            receivedMessage = update.getCallbackQuery().getData();
            botAnswerUtils(receivedMessage, chatId, userName);
        }
    }


    private void botAnswerUtils(String receivedMessage, long chatId, String userName) {
        switch (receivedMessage) {
            case "/start" ->
                    startBot(chatId, userName);
            case "/logout" ->
                    logout(chatId);
            case "/login" ->
                    sendMessage(chatId,LOGIN);
            case "/signin" ->
                    sendMessage(chatId,SIGNIN);
            case "/items" ->
                    sendSavedItem(chatId);
            case "/newitem" ->
                    sendMessage(chatId, NEW_ITEM);
            default ->
                    fetchRequest(receivedMessage, chatId);
        }
    }

    private void fetchRequest(String receivedMessage, long chatId) {
        if (receivedMessage.startsWith("create")) {
            processCreateUserCommand(receivedMessage, chatId);
        } else if (receivedMessage.startsWith("login")) {
            processLoginCommand(receivedMessage, chatId);
        } else if (receivedMessage.startsWith("add")){
            createNewItem(chatId,receivedMessage);
        }
        else {
            if (authenticationToken != null) {
                try {
                    Authentication authentication = authenticationManager.authenticate(authenticationToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    sendPdfDocument(chatId, telegramResponseService.searchAllByMoneyName(receivedMessage));
                } catch (UsernameNotFoundException e) {
                    sendMessage(chatId, "Your not login");
                    throw new AuthUsernameNotFoundException(e.getMessage());
                }
            } else {
                sendMessage(chatId, "Your not login");
            }
        }
    }

    private void processCreateUserCommand(String receivedMessage, long chatId) {
        Pattern pattern = Pattern.compile("create:(\\S+) password:(\\S+)");
        Matcher matcher = pattern.matcher(receivedMessage);
        if (matcher.find()) {
            String login = matcher.group(1);
            String password = matcher.group(2);
            PersonDTO personDTO = new PersonDTO(login, password);
            sendMessage(chatId, personRegistrationService.register(personDTO));
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
            sendMessage(chatId, "Login and password not found in the input string.");
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
            sendMessage(chatId, "Welcome back " + authentication.getName());
        } catch (UsernameNotFoundException e) {
            sendMessage(chatId, "User not found!");
            throw new AuthUsernameNotFoundException(e.getMessage());
        }
    }

    private void logout(long chatId) {
        if (authenticationToken != null) {
            SecurityContextHolder.clearContext();
            sendMessage(chatId, "Your log out successfully!");
        } else {
            sendMessage(chatId, "Your not login!");
        }
    }


    private void startBot(long chatId, String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Hi, " + userName + "! I'm a Telegram bot to track coin prices. " +
                "Please register or log in with your account!");
        message.setReplyMarkup(Buttons.inlineMarkup());
        try {
            execute(message);
            System.out.println("Reply sent");
        } catch (TelegramApiException e){
            throw new TelegramBotConfigNotCreated(e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        try {
            execute(message);
            System.out.println("Reply sent");
        } catch (TelegramApiException e){
            throw new TelegramBotConfigNotCreated(e.getMessage());
        }
    }

    private void sendPdfDocument(Long chatId, File save) {

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));

        try (FileInputStream fileInputStream = new FileInputStream(save)) {
            InputFile inputFile = new InputFile(fileInputStream, save.getName());
            sendDocument.setDocument(inputFile);
            execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new TelegramBotConfigNotCreated(e.getMessage());
        } catch (IOException e) {
            throw new BotDocumentNotCreated(e.getMessage());
        }
    }


    private void createNewItem(long chatId, String message) {
        if (authenticationToken != null) {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Person person = personService.getByUsername(authentication.getName());
            ItemDTO itemDTO = telegramResponseService.saveByLink(message);
            itemService.addItem(itemDTO,person);
            sendMessage(chatId, "Added");
        } else {
            sendMessage(chatId, "Your not login");
        }
    }

    private void sendSavedItem(long chatId) {
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Person person = personService.getByUsername(authentication.getName());
        sendPdfDocument(chatId, telegramResponseService.searchAllForUser(person.getId()));
    }
}
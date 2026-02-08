package com.bot.homework.service;

import com.bot.homework.config.BotConfig;
import com.bot.homework.service.utils.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class MainService extends TelegramLongPollingBot implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MainService.class);
    private final BotConfig config;
    private final StartService startService;
    private final RegistrationService registrationService;
    private final HelpService helpService;

    public MainService(
            BotConfig config,
            StartService startService,
            RegistrationService registrationService, HelpService helpService) {
        this.config = config;
        this.startService = startService;
        this.registrationService = registrationService;
        this.helpService = helpService;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            Long telegramId = msg.getFrom().getId();
            Long chatId = msg.getChatId();
            String text = msg.getText();

            if (!this.registrationService.isRegistered(telegramId)) {

                switch (text) {
                    case "/start" -> this.startService.handleStart(msg);
                    case "/register" -> this.registrationService.startRegistration(telegramId, chatId);
                    default -> {
                        if (this.registrationService.isRegistering(telegramId)) {
                            this.registrationService.handle(msg);
                        } else {
                            sendMessage(chatId, "Сначала зарегистрируйтесь");
                        }
                    }
                }
            } else {
                switch (text) {
                    case "/help" -> this.helpService.handle(chatId);
                    case "/edit_personal_info" -> this.registrationService.editPersonalInfo(telegramId, chatId);
                    default -> {
                        if (this.registrationService.isEditing(telegramId)) {
                            this.registrationService.handleEditMessage(msg);
                        } else {
                            sendMessage(chatId, "Неизвестная комманда 🤔");
                        }
                    }
                }
            }
        }

        if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();
            String data = callback.getData();
            Long telegramId = callback.getFrom().getId();
            Message message = (Message) callback.getMessage();
            Long chatId = message.getChatId();

            switch (data) {
                case "REGISTER" -> this.registrationService.startRegistration(telegramId, chatId);
                case "ROLE_TEACHER", "ROLE_PUPIL" ->
                        this.registrationService.handleRoleCallback(telegramId, chatId, data);
                case "BACK_TO_ROLE", "BACK_TO_FIRSTNAME", "BACK_TO_LASTNAME" ->
                        this.registrationService.handleBackCallback(telegramId, chatId, data);
                case "EDIT_FIRSTNAME", "EDIT_LASTNAME", "EDIT_PATRONYMIC" ->
                        this.registrationService.handleEditCallback(telegramId, chatId, data);
            }
        }
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Cannot send the message", e);
        }
    }

}

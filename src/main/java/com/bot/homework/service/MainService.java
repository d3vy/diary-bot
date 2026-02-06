package com.bot.homework.service;

import com.bot.homework.config.BotConfig;
import com.bot.homework.registration.RegistrationStep;
import com.bot.homework.registration.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;

@Service
public class MainService extends TelegramLongPollingBot implements MessageSender {

    private final BotConfig config;
    private final StartService startService;
    private final RegistrationService registrationService;

    public MainService(
            BotConfig config,
            StartService startService,
            RegistrationService registrationService
    ) {
        this.config = config;
        this.startService = startService;
        this.registrationService = registrationService;
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

            if (!registrationService.isRegistered(telegramId)) {

                switch (text) {
                    case "/start" -> startService.handleStart(msg);
                    case "/register" -> registrationService.startRegistration(telegramId, chatId);
                    default -> {
                        if (registrationService.isRegistering(telegramId)) {
                            registrationService.handle(msg);
                        } else {
                            sendMessage(chatId, "Сначала зарегистрируйтесь");
                        }
                    }
                }

            } else {
                sendMessage(chatId, "Вы уже зарегистрированы");
            }
        }

        if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();
            Long telegramId = callback.getFrom().getId();
            Long chatId = callback.getMessage().getChatId();
            String data = callback.getData();

            switch (data) {
                case "REGISTER" ->
                        registrationService.startRegistration(telegramId, chatId);

                case "ROLE_TEACHER", "ROLE_PUPIL" ->
                        registrationService.handleRoleCallback(telegramId, chatId, data);
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteMessage(Long chatId, Integer messageId) {}
}

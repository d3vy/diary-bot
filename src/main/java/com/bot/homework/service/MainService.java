package com.bot.homework.service;

import com.bot.homework.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class MainService extends TelegramLongPollingBot implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MainService.class);
    private final BotConfig config;
    private final StartService startService;
    private final RegistrationService registrationService;

    public MainService(BotConfig config, StartService startService, RegistrationService registrationService) {
        this.config = config;
        this.startService = startService;
        this.registrationService = registrationService;
    }

    @Override
    public String getBotUsername() {
        return this.config.getBotName();
    }

    @Override
    public String getBotToken() {
        return this.config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            switch (message) {
                case "/start":
                    this.startService.handleStart(update.getMessage());
                    break;
                case "/register":
                    this.registrationService.handle(update.getMessage());
                    break;

            }
        }
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        SendMessage msg = new SendMessage(chatId.toString(), text);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            log.error("Send error", e);
        }
    }

    @Override
    public void send(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Send error", e);
        }
    }

}

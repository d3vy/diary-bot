package com.bot.homework.service;

import com.bot.homework.config.BotConfig;
import com.vdurmont.emoji.EmojiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MainService extends TelegramLongPollingBot implements MessageSender {

    private final BotConfig config;
    private final StartService startService;
    private static final Logger log = LoggerFactory.getLogger(MainService.class);

    public MainService(BotConfig config, StartService startService) {
        this.config = config;
        this.startService = startService;
    }

    @Override
    public String getBotUsername() {
        return this.config.getBotName();
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
                case "/help":

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

}

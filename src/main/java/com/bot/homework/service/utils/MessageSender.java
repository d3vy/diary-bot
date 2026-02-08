package com.bot.homework.service.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface MessageSender {
    void sendMessage(Long chatId, String text);
    void send(SendMessage message);
}
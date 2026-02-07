package com.bot.homework.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface MessageSender {
    void sendMessage(Long chatId, String text);
    void send(SendMessage message);
    void deleteMessage(Long chatId, Integer messageId);}
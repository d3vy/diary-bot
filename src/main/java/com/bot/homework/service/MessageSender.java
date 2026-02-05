package com.bot.homework.service;

public interface MessageSender {
    void sendMessage(Long chatId, String text);
}
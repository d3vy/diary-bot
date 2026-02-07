package com.bot.homework.service;

import com.bot.homework.model.registration.RegistrationContext;

public interface MessageEditor {
    void storeMessageId(Long telegramId, Integer messageId);
    void cleanChat(Long chatId, RegistrationContext context);
}

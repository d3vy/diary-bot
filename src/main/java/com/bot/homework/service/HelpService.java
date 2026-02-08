package com.bot.homework.service;

import com.bot.homework.service.utils.MessageSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class HelpService {
    private final MessageSender sender;

    public HelpService(@Lazy MessageSender sender) {
        this.sender = sender;
    }

    public void handle(Long chatId) {
        String text = """
                You can get this message again by typing /help
                
                
                /command — usage
                """;
        this.sender.sendMessage(chatId, text);
    }
}

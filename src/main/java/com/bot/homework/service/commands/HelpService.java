package com.bot.homework.service.commands;

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
             
                /help — все комманды
                /edit_personal_info — можете изменить информацию о себе
                """;
        this.sender.sendMessage(chatId, text);
    }
}

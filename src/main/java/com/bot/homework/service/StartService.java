package com.bot.homework.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Service
public class StartService {

    private final MessageSender sender;

    public StartService(@Lazy MessageSender sender) {
        this.sender = sender;
    }

    public void handleStart(Message message) {
        String text = EmojiParser.parseToUnicode(
                "Здравствуйте, " + message.getFrom().getFirstName() +
                        " 👋\nНажмите кнопку ниже для регистрации"
        );

        InlineKeyboardButton registerButton = new InlineKeyboardButton();
        registerButton.setText("Регистрация");
        registerButton.setCallbackData("REGISTER");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                List.of(List.of(registerButton))
        );

        SendMessage msg = new SendMessage(message.getChatId().toString(), text);
        msg.setReplyMarkup(keyboard);

        this.sender.send(msg);
    }
}
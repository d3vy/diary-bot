package com.bot.homework.service;

import com.bot.homework.model.User;
import com.bot.homework.repository.PupilRepository;
import com.bot.homework.repository.TeacherRepository;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class StartService {

    private final MessageSender sender;

    public StartService(@Lazy MessageSender sender, TeacherRepository teacherRepository, PupilRepository pupilRepository) {
        this.sender = sender;
    }

    public void handleStart(Message message) {
        greeting(message.getChatId(), message.getFrom().getFirstName());
    }

    private void greeting(Long chatId, String name) {
        String text = EmojiParser.parseToUnicode(
                "Hello, " + name + " nice to meet you! :smirk_cat:" +
                        "//info about bot//"
        );
        this.sender.sendMessage(chatId, text);
    }
}
package com.bot.homework.service;

import com.bot.homework.model.User;
import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.objects.Message;

public class StartService {

    private final MessageSender sender;
    private final UserRepository userRepository;

    public StartService(MessageSender sender) {
        this.sender = sender;
    }

    public void handleStart(Message message) {
        greeting(message.getChatId(), message.getFrom().getFirstName());
        registerUser(message);
    }

    private void greeting(Long chatId, String name) {
        String text = EmojiParser.parseToUnicode(
                "Hello, " + name + " nice to meet you! :smirk_cat:"
        );
        this.sender.sendMessage(chatId, text);
    }

    private void registerUser(Message message) {
        if (thia.userRepository.findById(message.getChatId()).isEmpty()) {

            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstname(chat.getFirstName());
            user.setLastname(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setRegisterAt(System.currentTimeMillis());

            userRepository.save(user);

            log.info("User {} saved", user);
        }
    }
}
package com.bot.homework.service;

import com.bot.homework.config.BotConfig;
import com.bot.homework.registration.RegistrationStep;
import com.bot.homework.registration.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class MainService extends TelegramLongPollingBot implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MainService.class);
    private final BotConfig config;
    private final StartService startService;
    private final RegistrationService registrationService;

    public MainService(BotConfig config, StartService startService, RegistrationService registrationService) {
        this.config = config;
        this.startService = startService;
        this.registrationService = registrationService;
    }

    @Override
    public String getBotUsername() {
        return this.config.getBotName();
    }

    @Override
    public String getBotToken() {
        return this.config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long telegramId = message.getFrom().getId();
            Long chatId = message.getChatId();
            String text = message.getText();

            if (!this.registrationService.isRegistered(telegramId)) {
                switch (text) {
                    case "/start" -> this.startService.handleStart(message);
                    case "/register" -> this.registrationService.handle(message);
                    default -> {
                        if (this.registrationService.isRegistering(telegramId)) {
                            this.registrationService.handle(message);
                        } else {
                            sendMessage(chatId, "Сначала зарегистрируйтесь командой /register");
                        }
                    }
                }
            } else {
                switch (text) {
                    case "/start" -> sendMessage(chatId, "Вы уже зарегистрированы");
                    default -> sendMessage(chatId, "Unknown command");
                }
            }

        } else if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();
            Long telegramId = callback.getFrom().getId();
            Long chatId = callback.getMessage().getChatId();
            String data = callback.getData();

            if (this.registrationService.isRegistering(telegramId)) {
                var context = this.registrationService.getContext(telegramId);

                if (context.getStep() == RegistrationStep.CHOOSE_ROLE) {
                    if ("ROLE_TEACHER".equals(data)) context.setRole(UserRole.TEACHER);
                    else if ("ROLE_PUPIL".equals(data)) context.setRole(UserRole.PUPIL);

                    context.setStep(RegistrationStep.ENTER_FIRSTNAME);
                    sendMessage(chatId, "Введите ваше имя");
                }
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

    @Override
    public void send(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Send error", e);
        }
    }

}

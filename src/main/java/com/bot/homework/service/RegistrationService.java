package com.bot.homework.service;

import com.bot.homework.model.Pupil;
import com.bot.homework.model.Teacher;
import com.bot.homework.registration.RegistrationContext;
import com.bot.homework.registration.RegistrationStep;
import com.bot.homework.registration.UserRole;
import com.bot.homework.repository.PupilRepository;
import com.bot.homework.repository.TeacherRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationService {

    private final Map<Long, RegistrationContext> contexts = new ConcurrentHashMap<>();

    private final MessageSender sender;
    private final TeacherRepository teacherRepository;
    private final PupilRepository pupilRepository;

    public RegistrationService(@Lazy MessageSender sender, TeacherRepository teacherRepository, PupilRepository pupilRepository) {
        this.sender = sender;
        this.teacherRepository = teacherRepository;
        this.pupilRepository = pupilRepository;
    }

    public void handle(Message message) {
        Long telegramId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String text = message.getText();

        RegistrationContext context = this.contexts
                .computeIfAbsent(telegramId, id -> new RegistrationContext());

        switch (context.getStep()) {
            case NONE -> {
                context.setStep(RegistrationStep.CHOOSE_ROLE);
                askRole(chatId);
            }

            case CHOOSE_ROLE -> {
                if (text.equals("👨‍🏫 Учитель")) {
                    context.setRole(UserRole.TEACHER);
                } else if (text.equals("🎓 Ученик")) {
                    context.setRole(UserRole.PUPIL);
                } else {
                    sender.sendMessage(chatId, "Выберите вариант, нажав на кнопку");
                    return;
                }
                context.setStep(RegistrationStep.ENTER_FIRSTNAME);
                this.sender.sendMessage(chatId, "Введите ваше имя");
            }

            case ENTER_FIRSTNAME -> {
                context.setFirstname(text);
                context.setStep(RegistrationStep.ENTER_LASTNAME);
                this.sender.sendMessage(chatId, "Введите фамилию");
            }

            case ENTER_LASTNAME -> {
                context.setLastname(text);
                context.setStep(RegistrationStep.ENTER_PATRONYMIC);
                this.sender.sendMessage(chatId, "Введите отчество или напишите '-'");
            }

            case ENTER_PATRONYMIC -> {
                context.setPatronymic(text.equals("-") ? null : text);
                saveUser(telegramId, context);
                this.contexts.remove(telegramId);

                SendMessage msg = new SendMessage(chatId.toString(), "Вы зарегистрированы ✅");
                msg.setReplyMarkup(new ReplyKeyboardRemove(true));
                this.sender.send(msg);
            }
        }
    }

    private void askRole(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), "Кто вы?");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("👨‍🏫 Учитель");
        row.add("🎓 Ученик");

        keyboard.setKeyboard(List.of(row));
        message.setReplyMarkup(keyboard);

        this.sender.send(message);
    }

    private void saveUser(Long telegramId, RegistrationContext context) {
        if (context.getRole() == UserRole.TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setTelegramId(telegramId);
            teacher.setFirstname(context.getFirstname());
            teacher.setLastname(context.getLastname());
            teacher.setPatronymic(context.getPatronymic());
            this.teacherRepository.save(teacher);

        } else if (context.getRole() == UserRole.PUPIL) {
            Pupil pupil = new Pupil();
            pupil.setTelegramId(telegramId);
            pupil.setFirstname(context.getFirstname());
            pupil.setLastname(context.getLastname());
            pupil.setPatronymic(context.getPatronymic());
            this.pupilRepository.save(pupil);
        }
    }


}

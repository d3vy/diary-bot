package com.bot.homework.service;

import com.bot.homework.model.Pupil;
import com.bot.homework.model.Teacher;
import com.bot.homework.registration.RegistrationContext;
import com.bot.homework.registration.RegistrationStep;
import com.bot.homework.registration.UserRole;
import com.bot.homework.repository.PupilRepository;
import com.bot.homework.repository.TeacherRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationService {

    private final Map<Long, RegistrationContext> contexts = new ConcurrentHashMap<>();

    private final MessageSender sender;
    private final TeacherRepository teacherRepository;
    private final PupilRepository pupilRepository;

    public RegistrationService(
            @Lazy MessageSender sender,
            TeacherRepository teacherRepository,
            PupilRepository pupilRepository
    ) {
        this.sender = sender;
        this.teacherRepository = teacherRepository;
        this.pupilRepository = pupilRepository;
    }

    public void startRegistration(Long telegramId, Long chatId) {
        RegistrationContext context = new RegistrationContext();
        context.setStep(RegistrationStep.CHOOSE_ROLE);
        this.contexts.put(telegramId, context);
        this.askRole(chatId);
    }

    public void handleRoleCallback(Long telegramId, Long chatId, String data) {
        RegistrationContext context = this.contexts.get(telegramId);
        if (context == null) return;
        if (context.getStep() != RegistrationStep.CHOOSE_ROLE) return;

        if ("ROLE_TEACHER".equals(data)) {
            context.setRole(UserRole.TEACHER);
        } else if ("ROLE_PUPIL".equals(data)) {
            context.setRole(UserRole.PUPIL);
        } else {
            return;
        }

        context.setStep(RegistrationStep.ENTER_FIRSTNAME);
        this.sender.sendMessage(chatId, "Введите ваше имя");
    }

    public void handle(Message message) {
        Long telegramId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String text = message.getText();

        RegistrationContext context = this.contexts.get(telegramId);
        if (context == null) return;

        switch (context.getStep()) {

            case ENTER_FIRSTNAME -> {
                context.setFirstname(text);
                context.setStep(RegistrationStep.ENTER_LASTNAME);
                this.sender.sendMessage(chatId, "Введите фамилию");
            }

            case ENTER_LASTNAME -> {
                context.setLastname(text);
                context.setStep(RegistrationStep.ENTER_PATRONYMIC);
                this.sender.sendMessage(chatId, "Введите отчество или '-'");
            }

            case ENTER_PATRONYMIC -> {
                context.setPatronymic("-".equals(text) ? null : text);
                this.saveUser(telegramId, context);
                this.contexts.remove(telegramId);

                SendMessage msg = new SendMessage(chatId.toString(), "Вы зарегистрированы ✅");
                msg.setReplyMarkup(new ReplyKeyboardRemove(true));
                this.sender.send(msg);
            }
        }
    }

    public boolean isRegistering(Long telegramId) {
        return this.contexts.containsKey(telegramId);
    }

    public boolean isRegistered(Long telegramId) {
        return this.teacherRepository.existsByTelegramId(telegramId)
                || this.pupilRepository.existsByTelegramId(telegramId);
    }

    private void askRole(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), "Кто вы?");

        InlineKeyboardButton teacher = new InlineKeyboardButton();
        teacher.setText("👨‍🏫 Учитель");
        teacher.setCallbackData("ROLE_TEACHER");

        InlineKeyboardButton pupil = new InlineKeyboardButton();
        pupil.setText("🎓 Ученик");
        pupil.setCallbackData("ROLE_PUPIL");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                List.of(List.of(teacher, pupil))
        );
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
        }

        if (context.getRole() == UserRole.PUPIL) {
            Pupil pupil = new Pupil();
            pupil.setTelegramId(telegramId);
            pupil.setFirstname(context.getFirstname());
            pupil.setLastname(context.getLastname());
            pupil.setPatronymic(context.getPatronymic());
            this.pupilRepository.save(pupil);
        }
    }
}
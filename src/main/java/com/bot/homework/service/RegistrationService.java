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
            PupilRepository pupilRepository) {
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
        askFirstname(chatId);
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
                askLastname(chatId);
            }

            case ENTER_LASTNAME -> {
                context.setLastname(text);
                context.setStep(RegistrationStep.ENTER_PATRONYMIC);
                askPatronymic(chatId);
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

    public void handleBackCallback(Long telegramId, Long chatId, String data) {
        RegistrationContext context = this.contexts.get(telegramId);
        if (context == null) return;

        switch (data){
            case "BACK_TO_ROLE" -> {
                context.setStep(RegistrationStep.CHOOSE_ROLE);
                askRole(chatId);
            }
            case "BACK_TO_FIRSTNAME" -> {
                context.setStep(RegistrationStep.ENTER_FIRSTNAME);
                askFirstname(chatId);
            }
            case "BACK_TO_LASTNAME" -> {
                context.setStep(RegistrationStep.ENTER_LASTNAME);
                askLastname(chatId);
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
        String text = "Кто вы?";
        SendMessage message = new SendMessage(chatId.toString(), text);

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

    private void askFirstname(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), "Введите ваше имя");

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("👈 Вернуться");
        backButton.setCallbackData("BACK_TO_ROLE");

        InlineKeyboardMarkup keyboard =
                new InlineKeyboardMarkup(List.of(List.of(backButton)));

        message.setReplyMarkup(keyboard);

        this.sender.send(message);
    }

    private void askLastname(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), "Введите вашу фамилию");

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("👈 Вернуться");
        backButton.setCallbackData("BACK_TO_FIRSTNAME");

        InlineKeyboardMarkup keyboard =
                new InlineKeyboardMarkup(List.of(List.of(backButton)));

        message.setReplyMarkup(keyboard);

        this.sender.send(message);
    }

    private void askPatronymic(Long chatId) {

        SendMessage message = new SendMessage(chatId.toString(), "Введите отчество или '-'");

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("👈 Вернуться");
        backButton.setCallbackData("BACK_TO_LASTNAME");

        InlineKeyboardMarkup keyboard =
                new InlineKeyboardMarkup(List.of(List.of(backButton)));

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
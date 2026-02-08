package com.bot.homework.service;

import com.bot.homework.model.registration.edit.EditPersonalInfoContext;
import com.bot.homework.model.registration.edit.EditPersonalInfoStep;
import com.bot.homework.model.user.Pupil;
import com.bot.homework.model.user.Teacher;
import com.bot.homework.model.registration.RegistrationContext;
import com.bot.homework.model.registration.RegistrationStep;
import com.bot.homework.model.registration.UserRole;
import com.bot.homework.repository.EditPersonalInfoRepository;
import com.bot.homework.repository.PupilRepository;
import com.bot.homework.repository.RegistrationContextRepository;
import com.bot.homework.repository.TeacherRepository;
import com.bot.homework.service.utils.MessageSender;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Service
@Transactional
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);
    private final MessageSender sender;
    private final TeacherRepository teacherRepository;
    private final PupilRepository pupilRepository;
    private final RegistrationContextRepository contextRepository;
    private final EditPersonalInfoRepository editRepository;
    private final HelpService helpService;

    public RegistrationService(
            @Lazy MessageSender sender,
            TeacherRepository teacherRepository,
            PupilRepository pupilRepository,
            RegistrationContextRepository contextRepository, EditPersonalInfoRepository editRepository,
            HelpService helpService) {
        this.sender = sender;
        this.teacherRepository = teacherRepository;
        this.pupilRepository = pupilRepository;
        this.contextRepository = contextRepository;
        this.editRepository = editRepository;
        this.helpService = helpService;
    }

    public void startRegistration(Long telegramId, Long chatId) {
        RegistrationContext context = new RegistrationContext();
        context.setTelegramId(telegramId);
        context.setStep(RegistrationStep.CHOOSE_ROLE);
        this.contextRepository.save(context);
        this.askRole(chatId);
    }

    public void handleRoleCallback(Long telegramId, Long chatId, String data) {
        RegistrationContext context
                = this.contextRepository.findById(telegramId).orElse(null);
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

    public void handleBackCallback(Long telegramId, Long chatId, String data) {
        RegistrationContext context
                = this.contextRepository.findById(telegramId).orElse(null);
        if (context == null) return;

        switch (data) {
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

    public void handle(Message message) {
        Long telegramId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String text = message.getText();

        RegistrationContext context
                = this.contextRepository.findById(telegramId).orElse(null);
        if (context == null) return;

        switch (context.getStep()) {

            case ENTER_FIRSTNAME -> {
                context.setFirstname(text);
                context.setStep(RegistrationStep.ENTER_LASTNAME);
                this.contextRepository.save(context);
                askLastname(chatId);
            }

            case ENTER_LASTNAME -> {
                context.setLastname(text);
                context.setStep(RegistrationStep.ENTER_PATRONYMIC);
                this.contextRepository.save(context);
                askPatronymic(chatId);
            }

            case ENTER_PATRONYMIC -> {
                context.setPatronymic("-".equals(text) ? null : text);
                saveUser(telegramId, context);

                this.contextRepository.delete(context);

                SendMessage msg = new SendMessage(chatId.toString(), "Вы зарегистрированы ✅");
                msg.setReplyMarkup(new ReplyKeyboardRemove(true));
                this.sender.send(msg);
                this.helpService.handle(chatId);
            }
        }
    }


    public boolean isRegistering(Long telegramId) {
        return this.contextRepository.findById(telegramId).isPresent();
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

    public void handleEditCallback(Long telegramId, Long chatId, String data) {

        EditPersonalInfoContext context =
                this.editRepository.findById(telegramId).orElse(null);
        if (context == null) return;

        switch (data) {
            case "EDIT_FIRSTNAME" -> {
                context.setStep(EditPersonalInfoStep.ENTER_FIRSTNAME);
                this.editRepository.save(context);
                this.sender.send(new SendMessage(chatId.toString(), "Введите новое имя"));
            }
            case "EDIT_LASTNAME" -> {
                context.setStep(EditPersonalInfoStep.ENTER_LASTNAME);
                this.editRepository.save(context);
                this.sender.send(new SendMessage(chatId.toString(), "Введите новую фамилию"));
            }
            case "EDIT_PATRONYMIC" -> {
                context.setStep(EditPersonalInfoStep.ENTER_PATRONYMIC);
                this.editRepository.save(context);
                this.sender.send(new SendMessage(chatId.toString(), "Введите новое отчество или '-'"));
            }
        }
    }

    public void handleEditMessage(Message message) {

        Long telegramId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String text = message.getText();

        EditPersonalInfoContext context =
                editRepository.findById(telegramId).orElse(null);
        if (context == null) return;

        switch (context.getStep()) {

            case ENTER_FIRSTNAME -> {
                updateFirstname(telegramId, text);
                finishEdit(context, chatId);
            }

            case ENTER_LASTNAME -> {
                updateLastname(telegramId, text);
                finishEdit(context, chatId);
            }

            case ENTER_PATRONYMIC -> {
                updatePatronymic(telegramId, "-".equals(text) ? null : text);
                finishEdit(context, chatId);
            }
        }
    }

    private void updateFirstname(Long telegramId, String firstname) {

        this.teacherRepository.findByTelegramId(telegramId)
                .ifPresent(t -> {
                    t.setFirstname(firstname);
                    this.teacherRepository.save(t);
                });

        this.pupilRepository.findByTelegramId(telegramId)
                .ifPresent(p -> {
                    p.setFirstname(firstname);
                    this.pupilRepository.save(p);
                });
    }

    private void updateLastname(Long telegramId, String lastname) {
        this.teacherRepository.findByTelegramId(telegramId)
                .ifPresent(t -> {
                    t.setLastname(lastname);
                    this.teacherRepository.save(t);
                });

        this.pupilRepository.findByTelegramId(telegramId)
                .ifPresent(p -> {
                    p.setLastname(lastname);
                    this.pupilRepository.save(p);
                });
    }

    private void updatePatronymic(Long telegramId, String patronymic) {
        this.teacherRepository.findByTelegramId(telegramId)
                .ifPresent(t -> {
                    t.setPatronymic(patronymic);
                    this.teacherRepository.save(t);
                });

        this.pupilRepository.findByTelegramId(telegramId)
                .ifPresent(p -> {
                    p.setPatronymic(patronymic);
                    this.pupilRepository.save(p);
                });
    }

    private void finishEdit(EditPersonalInfoContext context, Long chatId) {
        editRepository.delete(context);
        sender.send(new SendMessage(chatId.toString(), "Данные обновлены ✅"));
        helpService.handle(chatId);
    }

    public void editPersonalInfo(Long telegramId, Long chatId) {
        if (!isRegistered(telegramId)) {
            this.sender.send(new SendMessage(chatId.toString(), "Вы не зарегистрированы"));
            return;
        }

        EditPersonalInfoContext context = new EditPersonalInfoContext();
        context.setTelegramId(telegramId);
        context.setStep(EditPersonalInfoStep.CHOOSE_FIELD);

        this.editRepository.save(context);
        askWhatToEdit(chatId);
    }

    private void askWhatToEdit(Long chatId) {

        SendMessage message = new SendMessage(chatId.toString(), "Что вы хотите изменить?");

        InlineKeyboardButton first = new InlineKeyboardButton("Имя");
        first.setCallbackData("EDIT_FIRSTNAME");

        InlineKeyboardButton last = new InlineKeyboardButton("Фамилию");
        last.setCallbackData("EDIT_LASTNAME");

        InlineKeyboardButton patr = new InlineKeyboardButton("Отчество");
        patr.setCallbackData("EDIT_PATRONYMIC");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                List.of(
                        List.of(first, last),
                        List.of(patr)
                )
        );

        message.setReplyMarkup(keyboard);
        this.sender.send(message);
    }

    public boolean isEditing(Long telegramId) {
        return this.editRepository.findById(telegramId).isPresent();
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
package com.bot.homework.service;

import com.bot.homework.model.registration.edit.EditPersonalInfoContext;
import com.bot.homework.model.registration.edit.EditPersonalInfoStep;
import com.bot.homework.repository.EditPersonalInfoRepository;
import com.bot.homework.repository.PupilRepository;
import com.bot.homework.repository.TeacherRepository;
import com.bot.homework.service.utils.MessageSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Service
public class EditPersonalInfoService {

    private final MessageSender sender;
    private final EditPersonalInfoRepository editRepository;
    private final TeacherRepository teacherRepository;
    private final PupilRepository pupilRepository;
    private final HelpService helpService;


    public EditPersonalInfoService(
            EditPersonalInfoRepository editRepository,
            @Lazy MessageSender sender,
            TeacherRepository teacherRepository,
            PupilRepository pupilRepository,
            HelpService helpService
    ) {
        this.editRepository = editRepository;
        this.sender = sender;
        this.teacherRepository = teacherRepository;
        this.pupilRepository = pupilRepository;
        this.helpService = helpService;
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
}

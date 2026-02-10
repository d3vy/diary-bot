package com.bot.homework.service.commands;

import com.bot.homework.model.group.Group;
import com.bot.homework.model.group.GroupCreationContext;
import com.bot.homework.model.group.GroupCreationStep;
import com.bot.homework.model.user.Teacher;
import com.bot.homework.repository.group.GroupCreationContextRepository;
import com.bot.homework.repository.group.GroupRepository;
import com.bot.homework.repository.user.TeacherRepository;
import com.bot.homework.service.utils.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Service
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);
    private final GroupRepository groupRepository;
    private final TeacherRepository teacherRepository;
    private final GroupCreationContextRepository contextRepository;
    private final MessageSender sender;

    public GroupService(
            GroupRepository groupRepository,
            TeacherRepository teacherRepository,
            GroupCreationContextRepository contextRepository,
            @Lazy MessageSender sender
    ) {
        this.groupRepository = groupRepository;
        this.teacherRepository = teacherRepository;
        this.contextRepository = contextRepository;
        this.sender = sender;
    }

    public void startGroupCreation(Long telegramId, Long chatId) {
        GroupCreationContext context = new GroupCreationContext();
        context.setTelegramId(telegramId);
        context.setStep(GroupCreationStep.ASK_NUMBER);
        this.contextRepository.save(context);
        askNumber(chatId);
    }

    public void handle(Message message) {
        Long telegramId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String text = message.getText();

        GroupCreationContext context
                = this.contextRepository.findById(telegramId).orElse(null);

        if (context == null) return;

        switch (context.getStep()) {
            case ASK_NUMBER -> {
                context.setNumber(text);
                context.setStep(GroupCreationStep.ASK_NAME);
                this.contextRepository.save(context);
                askName(chatId);
            }
            case ASK_NAME -> {
                context.setName(text);
                context.setStep(GroupCreationStep.ASK_SUBJECT);
                this.contextRepository.save(context);
                askSubject(chatId);
            }
            case ASK_SUBJECT -> {
                context.setSubject(text);
                context.setStep(GroupCreationStep.ASK_START_TIME);
                this.contextRepository.save(context);
                askStartTime(chatId);
            }
            case ASK_START_TIME -> {
                context.setStartTime(text);
                saveGroup(telegramId, context);
                this.contextRepository.delete(context);

                SendMessage msg = new SendMessage(chatId.toString(), "Группа создана ✅");
                msg.setReplyMarkup(new ReplyKeyboardRemove(true));
                this.sender.send(msg);
            }
        }
    }

    public boolean isCreating(Long telegramId) {
        return this.contextRepository.existsById(telegramId);
    }

    // addPupilToGroup
    // deletePupilFromGroup
    // addHomework

    private void askNumber(Long chatId) {
        String text = "Введите номер группы";
        SendMessage message = new SendMessage(chatId.toString(), text);

        this.sender.send(message);
    }

    private void askName(Long chatId) {
        String text = "Введите название группы";
        SendMessage message = new SendMessage(chatId.toString(), text);

        this.sender.send(message);
    }

    private void askSubject(Long chatId) {
        String text = "Введите предмет группы";
        SendMessage message = new SendMessage(chatId.toString(), text);

        this.sender.send(message);
    }

    private void askStartTime(Long chatId) {
        String text = "Введите время начала занятия";
        SendMessage message = new SendMessage(chatId.toString(), text);

        this.sender.send(message);
    }

    private void saveGroup(Long telegramId, GroupCreationContext context) {
        Teacher teacher = this.teacherRepository.findByTelegramId(telegramId).orElse(null);
        if (teacher == null) {
            log.error("Teacher is null, can't create a group");
            return;
        }
        Group group = new Group();
        group.setTeacher(teacher);
        group.setName(context.getName());
        group.setNumber(context.getNumber());
        group.setSubject(context.getSubject());
        group.setStartOfLessonTime(context.getStartTime());

        this.groupRepository.save(group);
    }
}

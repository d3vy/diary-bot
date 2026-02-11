package com.bot.homework.service.commands;

import com.bot.homework.model.group.Group;
import com.bot.homework.model.group.GroupCreationContext;
import com.bot.homework.model.group.GroupCreationStep;
import com.bot.homework.model.user.pupil.AddPupilContext;
import com.bot.homework.model.user.pupil.AddPupilStep;
import com.bot.homework.model.user.pupil.Pupil;
import com.bot.homework.model.user.Teacher;
import com.bot.homework.repository.group.GroupCreationContextRepository;
import com.bot.homework.repository.group.GroupRepository;
import com.bot.homework.repository.user.AddPupilContextRepository;
import com.bot.homework.repository.user.PupilRepository;
import com.bot.homework.repository.user.TeacherRepository;
import com.bot.homework.service.utils.MessageSender;
import jakarta.transaction.Transactional;
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
    private final PupilRepository pupilRepository;
    private final GroupCreationContextRepository groupCreationContextRepository;
    private final AddPupilContextRepository addPupilContextRepository;
    private final MessageSender sender;

    public GroupService(
            GroupRepository groupRepository,
            TeacherRepository teacherRepository,
            PupilRepository pupilRepository,
            GroupCreationContextRepository groupCreationContextRepository,
            AddPupilContextRepository addPupilContextRepository,
            @Lazy MessageSender sender
    ) {
        this.groupRepository = groupRepository;
        this.teacherRepository = teacherRepository;
        this.pupilRepository = pupilRepository;
        this.groupCreationContextRepository = groupCreationContextRepository;
        this.addPupilContextRepository = addPupilContextRepository;
        this.sender = sender;
    }

    public void startGroupCreation(Long telegramId, Long chatId) {
        GroupCreationContext context = new GroupCreationContext();
        context.setTelegramId(telegramId);
        context.setStep(GroupCreationStep.ASK_NUMBER);
        this.groupCreationContextRepository.save(context);
        askNumber(chatId);
    }

    public void handleGroupCreation(Message message) {
        Long telegramId = message.getFrom().getId();
        Long chatId = message.getChatId();
        String text = message.getText();

        GroupCreationContext context
                = this.groupCreationContextRepository.findById(telegramId).orElse(null);

        if (context == null) return;

        switch (context.getStep()) {
            case ASK_NUMBER -> {
                context.setNumber(text);
                context.setStep(GroupCreationStep.ASK_NAME);
                this.groupCreationContextRepository.save(context);
                askName(chatId);
            }
            case ASK_NAME -> {
                context.setName(text);
                context.setStep(GroupCreationStep.ASK_SUBJECT);
                this.groupCreationContextRepository.save(context);
                askSubject(chatId);
            }
            case ASK_SUBJECT -> {
                context.setSubject(text);
                context.setStep(GroupCreationStep.ASK_START_TIME);
                this.groupCreationContextRepository.save(context);
                askStartTime(chatId);
            }
            case ASK_START_TIME -> {
                context.setStartTime(text);
                saveGroup(telegramId, context);
                this.groupCreationContextRepository.delete(context);

                SendMessage msg = new SendMessage(chatId.toString(), "Группа создана ✅");
                msg.setReplyMarkup(new ReplyKeyboardRemove(true));
                this.sender.send(msg);
            }
        }
    }

    public boolean isCreating(Long telegramId) {
        return this.groupCreationContextRepository.existsById(telegramId);
    }

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

    @Transactional
    public void startAddPupilToGroup(Long teacherId, Long chatId) {
        AddPupilContext context = new AddPupilContext();
        context.setTeacherId(teacherId);
        context.setChatId(chatId);
        context.setStep(AddPupilStep.WAIT_PUPIL_FULL_NAME);

        this.addPupilContextRepository.save(context);
        this.sender.sendMessage(chatId, "Введите ФИО ученика");
    }

    public Boolean isAddingPupilToGroup(Long teacherId) {
        return this.addPupilContextRepository.existsById(teacherId);
    }

    @Transactional
    public void handleAddPupilToGroup(Message msg) {
        Long teacherId = msg.getFrom().getId();
        AddPupilContext context = this.addPupilContextRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Context is not found"));

        switch (context.getStep()) {
            case WAIT_PUPIL_FULL_NAME -> {
                context.setPupilFullName(msg.getText());
                context.setStep(AddPupilStep.WAIT_GROUP_NAME);
                this.sender.sendMessage(context.getChatId(), "Введите название группы");
            }
            case WAIT_GROUP_NAME -> {
                context.setGroupName(msg.getText());
                finish(context);
                this.addPupilContextRepository.delete(context);
            }
        }
    }

    private void finish(AddPupilContext ctx) {
        Pupil pupil = this.pupilRepository.findByFullName(ctx.getPupilFullName())
                .orElseThrow(() -> new IllegalArgumentException("Pupil not found"));

        Group group = this.groupRepository.findByName(ctx.getGroupName())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (!group.getTeacher().getId().equals(ctx.getTeacherId())) {
            throw new IllegalStateException("Это не ваша группа");
        }

        group.getPupils().add(pupil);
    }

    @Transactional
    public void joinGroup(Long pupilId, Integer groupId, boolean isPermittedToJoin) {

        if (!isPermittedToJoin) {
            throw new IllegalStateException("Joining is not permitted");
        }

        PupilGroup pg = getPupilAndGroup(pupilId, groupId);
        pg.group().getPupils().add(pg.pupil());
    }

    @Transactional
    public void deletePupilFromGroup(Long teacherId, Integer groupId, Long pupilId) {

        PupilGroup pg = getPupilAndGroup(pupilId, groupId);
        Group group = pg.group();
        Pupil pupil = pg.pupil();

        if (!group.getTeacher().getId().equals(teacherId)) {
            throw new IllegalStateException("It's not the teacher's group");
        }

        if (!group.getPupils().remove(pupil)) {
            throw new IllegalArgumentException("Pupil not in group");
        }
    }

    private PupilGroup getPupilAndGroup(Long pupilId, Integer groupId) {
        Pupil pupil = this.pupilRepository.findByTelegramId(pupilId)
                .orElseThrow(() -> new IllegalArgumentException("Pupil not found"));

        Group group = this.groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        return new PupilGroup(pupil, group);
    }

    private record PupilGroup(Pupil pupil, Group group) {
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

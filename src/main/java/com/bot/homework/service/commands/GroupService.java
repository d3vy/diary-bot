package com.bot.homework.service.commands;

import com.bot.homework.model.group.Group;
import com.bot.homework.model.group.GroupCreationContext;
import com.bot.homework.model.group.GroupCreationStep;
import com.bot.homework.model.group.join.JoinRequest;
import com.bot.homework.model.group.join.JoinRequestStatus;
import com.bot.homework.model.Subject;
import com.bot.homework.model.user.pupil.AddPupilContext;
import com.bot.homework.model.user.pupil.AddPupilStep;
import com.bot.homework.model.user.pupil.Pupil;
import com.bot.homework.model.user.Teacher;
import com.bot.homework.repository.group.GroupCreationContextRepository;
import com.bot.homework.repository.group.GroupRepository;
import com.bot.homework.repository.group.JoinRequestRepository;
import com.bot.homework.repository.subject.SubjectRepository;
import com.bot.homework.repository.user.AddPupilContextRepository;
import com.bot.homework.repository.user.PupilRepository;
import com.bot.homework.repository.user.TeacherRepository;
import com.bot.homework.service.utils.MessageSender;
import com.bot.homework.service.utils.PupilGroup;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {

    private final MessageSender sender;
    private static final Logger log = LoggerFactory.getLogger(GroupService.class);
    private final GroupRepository groupRepository;
    private final TeacherRepository teacherRepository;
    private final PupilRepository pupilRepository;
    private final GroupCreationContextRepository groupCreationContextRepository;
    private final AddPupilContextRepository addPupilContextRepository;
    private final SubjectRepository subjectRepository;
    private final JoinRequestRepository joinRequestRepository;

    public GroupService(
            @Lazy MessageSender sender,
            GroupRepository groupRepository,
            TeacherRepository teacherRepository,
            PupilRepository pupilRepository,
            GroupCreationContextRepository groupCreationContextRepository,
            AddPupilContextRepository addPupilContextRepository,
            SubjectRepository subjectRepository,
            JoinRequestRepository joinRequestRepository
    ) {
        this.groupRepository = groupRepository;
        this.teacherRepository = teacherRepository;
        this.pupilRepository = pupilRepository;
        this.groupCreationContextRepository = groupCreationContextRepository;
        this.addPupilContextRepository = addPupilContextRepository;
        this.sender = sender;
        this.subjectRepository = subjectRepository;
        this.joinRequestRepository = joinRequestRepository;
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
                Subject subject = this.subjectRepository.findByNameIgnoreCase(text).orElseThrow(
                        () -> new IllegalArgumentException("Subject is not found")
                );
                context.setSubject(subject);
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

    public void askGroupToJoinSubject(Long chatId) {
        String text = "Группу по какому предмету хотите найти?";
        SendMessage message = new SendMessage(chatId.toString(), text);
        List<Subject> subjects = this.subjectRepository.findAll();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        for (Subject subject : subjects) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(subject.getName());
            button.setCallbackData("SUBJECT_" + subject.getId());
            buttons.add(button);
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(buttons));

        message.setReplyMarkup(keyboard);

        this.sender.send(message);
    }


    public void showAllGroupsBySubject(Subject subject, Long chatId) {

        List<Group> groups = this.groupRepository.findBySubject(subject);

        if (groups.isEmpty()) {
            this.sender.sendMessage(chatId, "По этому предмету групп пока нет");
            return;
        }

        SendMessage message = new SendMessage(chatId.toString(), "Выберите группу:");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Group group : groups) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(group.getName());
            button.setCallbackData("JOIN_GROUP_" + group.getId());

            rows.add(List.of(button));
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        message.setReplyMarkup(keyboard);

        this.sender.send(message);
    }

    public Subject getSubjectById(Integer subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
    }

    @Transactional
    public void createJoinRequest(Long pupilId, Integer groupId, Long chatId) {
        PupilGroup pg = getPupilAndGroup(pupilId, groupId);
        Pupil pupil = pg.pupil();
        Group group = pg.group();

        if (this.joinRequestRepository.existsByPupilTelegramIdAndGroupId(pupilId, groupId)) {
            this.sender.sendMessage(chatId, "Вы уже отправили заявку");
            return;
        } else if (group.getPupils().contains(pupil)) {
            this.sender.sendMessage(chatId, "Вы уже состоите в этой группе");
            return;
        }

        JoinRequest request = new JoinRequest();
        request.setPupil(pupil);
        request.setGroup(group);
        request.setStatus(JoinRequestStatus.PENDING);

        this.joinRequestRepository.save(request);

        sender.sendMessage(chatId, "Заявка отправлена преподавателю ✅");
    }

    public void showJoinGroupRequests(Long teacherId, Long chatId) {
        List<JoinRequest> requests = this.joinRequestRepository.findByGroupTeacherTelegramId(teacherId);

        if (requests.isEmpty()) {
            this.sender.sendMessage(chatId, "Нет новых заявок");
            return;
        }

        sendJoinRequestToTeacher(requests.getFirst(), chatId);
    }

    private void sendJoinRequestToTeacher(JoinRequest request, Long chatId) {
        Pupil pupil = request.getPupil();
        Group group = request.getGroup();
        String text = """
                Новая заявка:
                Ученик: %s %s
                Группа: %s
                """.formatted(
                pupil.getFirstname(),
                pupil.getLastname(),
                group.getName()
        );

        InlineKeyboardButton approve = new InlineKeyboardButton();
        approve.setText("Подтвердить ✅");
        approve.setCallbackData("APPROVE_" + request.getId());

        InlineKeyboardButton reject = new InlineKeyboardButton();
        reject.setText("Отклонить ❌");
        reject.setCallbackData("REJECT_" + request.getId());

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(approve, reject)));

        SendMessage msg = new SendMessage(chatId.toString(), text);
        msg.setReplyMarkup(markup);

        this.sender.send(msg);
    }

    @Transactional
    public void approveRequest(Integer requestId) {
        JoinRequest request = this.joinRequestRepository.findById(requestId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Request not found")
                );
        if (request.getStatus() != JoinRequestStatus.PENDING) return;

        Pupil pupil = request.getPupil();
        Group group = request.getGroup();
        pupil.getStudyGroups().add(group);
        group.getPupils().add(pupil);

        this.joinRequestRepository.delete(request);
        showJoinGroupRequests(
                request.getGroup().getTeacher().getTelegramId(),
                request.getGroup().getTeacher().getTelegramId()
        );

    }

    @Transactional
    public void rejectRequest(Integer requestId) {

        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow();

        if (request.getStatus() != JoinRequestStatus.PENDING) return;

        this.joinRequestRepository.delete(request);
        showJoinGroupRequests(
                request.getGroup().getTeacher().getTelegramId(),
                request.getGroup().getTeacher().getTelegramId()
        );
    }

    public void showAllGroupsByTeacherId(Long telegramId, Long chatId) {
        List<Group> groups = this.groupRepository.findByTeacherTelegramId(telegramId);

        if (groups.isEmpty()) {
            this.sender.sendMessage(chatId, "У вас пока нет групп");
            return;
        }

        SendMessage message = new SendMessage(chatId.toString(), "Выберите группу");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Group group : groups) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(group.getName());
            button.setCallbackData("VIEW_PUPILS_IN_GROUP_" + group.getId());

            rows.add(List.of(button));
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        message.setReplyMarkup(keyboard);

        this.sender.send(message);
    }

    public void showAllPupilsInGroupByGroupId(Integer groupId, Long chatId) {

        Group group = this.groupRepository.findByIdWithPupils(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        List<Pupil> pupils = group.getPupils();

        if (pupils.isEmpty()) {
            this.sender.sendMessage(chatId, "В этой группе нет учеников");
            return;
        }

        SendMessage message = new SendMessage(chatId.toString(), "Выберите ученика, чтобы удалить его");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Pupil pupil : pupils) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String patronymic = pupil.getPatronymic();
            String fullName = pupil.getFirstname() + " " + pupil.getLastname() +
                    (patronymic != null && !patronymic.isBlank()
                            ? " " + patronymic
                            : "");
            button.setText(fullName);
            button.setCallbackData("REMOVE_PUPIL_" + groupId + "_" + pupil.getTelegramId());

            rows.add(List.of(button));
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        message.setReplyMarkup(keyboard);

        this.sender.send(message);

    }

    @Transactional
    public void removePupilFromGroup(Integer groupId, Long pupilId, Long teacherId, Long chatId) {

        PupilGroup pg = getPupilAndGroup(pupilId, groupId);
        Pupil pupil = pg.pupil();
        Group group = pg.group();


        if (!group.getTeacher().getTelegramId().equals(teacherId)) {
            throw new IllegalStateException("Это не ваша группа");
        }

        if (!pupil.getStudyGroups().remove(group)) {
            throw new IllegalArgumentException("Ученик не состоит в группе");
        }

        group.getPupils().remove(pupil);
        this.sender.sendMessage(chatId, "Ученик удалён из группы ✅");
    }

    private PupilGroup getPupilAndGroup(Long pupilId, Integer groupId) {
        Pupil pupil = this.pupilRepository.findByTelegramId(pupilId)
                .orElseThrow(() -> new IllegalArgumentException("Pupil not found"));

        Group group = this.groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        return new PupilGroup(pupil, group);
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

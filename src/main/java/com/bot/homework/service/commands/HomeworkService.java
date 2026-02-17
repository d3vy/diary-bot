package com.bot.homework.service.commands;

import com.bot.homework.model.group.Group;
import com.bot.homework.model.user.pupil.Pupil;
import com.bot.homework.repository.group.GroupRepository;
import com.bot.homework.repository.user.PupilRepository;
import com.bot.homework.service.utils.MessageSender;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class HomeworkService {

    private final Map<Long, Integer> homeworkContext = new ConcurrentHashMap<>();

    private final MessageSender sender;
    private final PupilRepository pupilRepository;
    private final GroupRepository groupRepository;

    public HomeworkService(
            @Lazy MessageSender sender,
            PupilRepository pupilRepository,
            GroupRepository groupRepository
    ) {
        this.sender = sender;
        this.pupilRepository = pupilRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public void showHomework(Long telegramId, Long chatId) {

        Pupil pupil = this.pupilRepository.findByTelegramId(telegramId)
                .orElse(null);

        if (pupil == null) {
            this.sender.sendMessage(chatId, "Вас нет в списке учеников");
            return;
        }

        List<Group> pupilGroups = pupil.getStudyGroups();

        if (pupilGroups == null || pupilGroups.isEmpty()) {
            this.sender.sendMessage(chatId, "Вы не состоите ни в одной группе");
            return;
        }


        Map<String, String> homeworksMap = pupilGroups.stream()
                .filter(g -> g.getHomework() != null && !g.getHomework().isBlank())
                .collect(Collectors
                        .toMap(Group::getName, Group::getHomework));


        if (homeworksMap.isEmpty()) {
            this.sender.sendMessage(chatId, "У вас нет домашнего задания");
            return;
        }

        homeworksMap.forEach((name, homework) -> {
            this.sender.sendMessage(chatId, name + "👉" + homework);
        });
    }

    public void askGroupForSettingHomework(Long telegramId, Long chatId) {
        String text = "Какой группе хотите задать домашнее задание?";
        SendMessage message = new SendMessage(chatId.toString(), text);
        List<Group> teacherGroups = this.groupRepository.findByTeacherTelegramId(telegramId);
        if (teacherGroups == null || teacherGroups.isEmpty()) {
            this.sender.sendMessage(chatId, "У вас нет групп");
            return;
        }

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (Group group : teacherGroups) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(group.getName());
            button.setCallbackData("HOMEWORK_FOR_GROUP_" + group.getId());
            buttons.add(button);
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(buttons));

        message.setReplyMarkup(keyboard);

        this.sender.send(message);
    }

    public void askHomeworkTask(Long telegramId, Integer groupId, Long chatId) {
        this.homeworkContext.put(telegramId, groupId);
        this.sender.sendMessage(chatId, "Напишите задание");
    }

    public boolean isSettingHomework(Long telegramId) {
        return this.homeworkContext.containsKey(telegramId);
    }

    @Transactional
    public void handleHomeworkInput(Long telegramId, Long chatId, String text) {


        Integer groupId = this.homeworkContext.get(telegramId);
        if (groupId == null) {
            this.sender.sendMessage(chatId, "Ошибка состояния. Попробуйте снова.");
            return;
        }

        Group group = this.groupRepository.findById(groupId)
                .orElse(null);
        if (group == null) {
            this.sender.sendMessage(chatId, "Группа не найдена");
            return;
        } else if (!group.getTeacher().getTelegramId().equals(telegramId)) {
            this.sender.sendMessage(chatId, "Это не ваша группа");
            return;
        }

        group.setHomework(text);

        this.homeworkContext.remove(telegramId);


        this.sender.sendMessage(chatId, "Домашнее задание сохранено ✅");
    }
}

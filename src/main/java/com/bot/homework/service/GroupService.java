package com.bot.homework.service;

import com.bot.homework.model.group.Group;
import com.bot.homework.model.user.Teacher;
import com.bot.homework.repository.GroupRepository;
import com.bot.homework.repository.TeacherRepository;
import com.bot.homework.service.utils.MessageSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final TeacherRepository teacherRepository;
    private final MessageSender messageSender;

    public GroupService(
            GroupRepository groupRepository, TeacherRepository teacherRepository,
            @Lazy MessageSender messageSender
    ) {
        this.groupRepository = groupRepository;
        this.teacherRepository = teacherRepository;
        this.messageSender = messageSender;
    }

    public void createGroup(Long telegramId, Long chatId) {
        Integer number = askGroupNumber(chatId);
        String name = askGroupName(chatId);
        String subject = askGroupSubject(chatId);
        LocalTime startTime = LocalTime.parse(askLessonStartTime(chatId));

        Group group = new Group();
        group.setNumber(number);
        group.setName(name);
        group.setSubject(subject);

        if (this.groupRepository.findById(number).isEmpty()) {
            Teacher teacher = this.teacherRepository.findByTelegramId(telegramId).orElse(null);
            group.setTeacher(teacher);
            this.groupRepository.save(group);
            this.messageSender.sendMessage(chatId, "Группа создана 👌");
        } else {
            String text = "Группа уже существует";
            this.messageSender.sendMessage(chatId, text);
        }
    }

    private CharSequence askLessonStartTime(Long chatId) {

    }

    private String askGroupSubject(Long chatId) {


    }

    private String askGroupName(Long chatId) {


    }

    private Integer askGroupNumber(Long chatId) {


    }


}

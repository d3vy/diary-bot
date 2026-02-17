package com.bot.homework.model.group;

import com.bot.homework.model.Subject;
import com.bot.homework.model.user.Teacher;
import jakarta.persistence.*;

@Entity
@Table(schema = "study_group", name = "group_creation_context")
public class GroupCreationContext {

    @Id
    private Long telegramId;

    GroupCreationStep step;
    String number;
    String name;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    Subject subject;

    String startTime;
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    Teacher teacher;

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public GroupCreationContext(
            Long telegramId,
            GroupCreationStep step,
            String number,
            String name,
            Subject subject,
            String startTime,
            Teacher teacher
    ) {
        this.telegramId = telegramId;
        this.step = step;
        this.number = number;
        this.name = name;
        this.subject = subject;
        this.startTime = startTime;
        this.teacher = teacher;
    }

    public GroupCreationContext() {
    }

    public GroupCreationStep getStep() {
        return step;
    }

    public void setStep(GroupCreationStep step) {
        this.step = step;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }
}

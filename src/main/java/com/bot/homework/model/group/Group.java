package com.bot.homework.model.group;

import com.bot.homework.model.subject.Subject;
import com.bot.homework.model.user.pupil.Pupil;
import com.bot.homework.model.user.Teacher;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(schema = "study_group", name = "study_groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String number;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @NotNull
    private String startOfLessonTime;

    @NotNull
    private String name;

    private String homework;
    private String homeworkDate;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "teacherId", nullable = false)
    private Teacher teacher;

    @ManyToMany(mappedBy = "studyGroups")
    private List<Pupil> pupils;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHomework() {
        return homework;
    }

    public void setHomework(String homework) {
        this.homework = homework;
    }

    public String getHomeworkDate() {
        return homeworkDate;
    }

    public void setHomeworkDate(String homeworkDate) {
        this.homeworkDate = homeworkDate;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public List<Pupil> getPupils() {
        return pupils;
    }

    public void setPupils(List<Pupil> pupils) {
        this.pupils = pupils;
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

    public String getStartOfLessonTime() {
        return startOfLessonTime;
    }

    public void setStartOfLessonTime(String startOfLessonTime) {
        this.startOfLessonTime = startOfLessonTime;
    }

    public Group() {
    }

    public Group(
            Integer id,
            Teacher teacher,
            List<Pupil> pupils,
            String number,
            String name,
            Subject subject,
            String startOfLessonTime,
            String homework,
            String homeworkDate
    ) {
        this.id = id;
        this.number = number;
        this.subject = subject;
        this.startOfLessonTime = startOfLessonTime;
        this.name = name;
        this.homework = homework;
        this.homeworkDate = homeworkDate;
        this.teacher = teacher;
        this.pupils = pupils;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}

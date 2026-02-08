package com.bot.homework.model.group;

import com.bot.homework.model.user.Pupil;
import com.bot.homework.model.user.Teacher;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(schema = "groups", name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer number;
    private String subject;
    private LocalTime startOfLessonTime;
    private String name;
    private String homework;
    private LocalDate homeworkDate;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToMany(mappedBy = "groups")
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

    public LocalDate getHomeworkDate() {
        return homeworkDate;
    }

    public void setHomeworkDate(LocalDate homeworkDate) {
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

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalTime getStartOfLessonTime() {
        return startOfLessonTime;
    }

    public void setStartOfLessonTime(LocalTime startOfLessonTime) {
        this.startOfLessonTime = startOfLessonTime;
    }

    public Group() {
    }

    public Group(
            Integer id,
            Integer number,
            String subject,
            LocalTime startOfLessonTime,
            String name,
            String homework,
            LocalDate homeworkDate,
            Teacher teacher,
            List<Pupil> pupils
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}

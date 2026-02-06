package com.bot.homework.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(schema = "groups", name = "groups")
public class Group {
    @Id
    private Integer id;
    private String homework;
    private Date homeworkDate;

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

    public Date getHomeworkDate() {
        return homeworkDate;
    }

    public void setHomeworkDate(Date homeworkDate) {
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

    public Group() {
    }

    public Group(Integer id, String homework, Date homeworkDate, Teacher teacher, List<Pupil> pupils) {
        this.id = id;
        this.homework = homework;
        this.homeworkDate = homeworkDate;
        this.teacher = teacher;
        this.pupils = pupils;
    }
}

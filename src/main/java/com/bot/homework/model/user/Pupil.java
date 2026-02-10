package com.bot.homework.model.user;

import com.bot.homework.model.group.Group;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(schema = "pupil", name = "pupils")
public class Pupil extends User {
    private Integer grade;

    @ManyToMany
    @JoinTable(
            schema = "pupil",
            name = "pupil_study_group",
            joinColumns = @JoinColumn(name = "pupil_id"),
            inverseJoinColumns = @JoinColumn(name = "study_group_id")
    )
    private List<Group> studyGroups;

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public List<Group> getStudyGroups() {
        return studyGroups;
    }

    public void setStudyGroups(List<Group> studyGroups) {
        this.studyGroups = studyGroups;
    }

    public Pupil() {
    }

    public Pupil(Integer grade, List<Group> studyGroups) {
        this.grade = grade;
        this.studyGroups = studyGroups;
    }
}

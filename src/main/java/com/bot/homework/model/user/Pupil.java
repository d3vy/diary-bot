package com.bot.homework.model.user;

import com.bot.homework.model.needtochangename.Group;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(schema = "pupils", name = "pupils")
public class Pupil extends User {
    private Integer grade;

    @ManyToMany
    @JoinTable(
            schema = "pupils",
            name = "pupil_groups",
            joinColumns = @JoinColumn(name = "pupil_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private List<Group> groups;

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Pupil() {
    }

    public Pupil(Integer grade, List<Group> groups) {
        this.grade = grade;
        this.groups = groups;
    }
}

package com.bot.homework.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(schema = "teachers", name = "teachers")
public class Teacher extends User {
    @OneToMany(mappedBy = "teacher")
    private List<Group> groups;

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Teacher() {
    }

    public Teacher(List<Group> groups) {
        this.groups = groups;
    }
}

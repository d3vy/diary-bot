package com.bot.homework.model.user;

import com.bot.homework.model.group.Group;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(schema = "teacher", name = "teachers")
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

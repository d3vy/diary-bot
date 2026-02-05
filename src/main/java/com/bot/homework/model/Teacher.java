package com.bot.homework.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "teachers")
public class Teacher extends User {
    private List<Group> groups;
}

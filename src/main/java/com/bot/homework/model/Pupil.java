package com.bot.homework.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "pupils")
public class Pupil extends User {
    private Integer grade;
    private List<Group> groups;
}

package com.bot.homework.model.group;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(schema = "group", name = "group_creation_context")
public class GroupCreationContext {
    GroupCreationStep step;
    Integer number;
    String name;
    String subject;
    LocalTime startTime;
}

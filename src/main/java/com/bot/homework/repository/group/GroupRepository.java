package com.bot.homework.repository.group;

import com.bot.homework.model.group.Group;
import com.bot.homework.model.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
    Optional<Group> findByName(String name);

    List<Group> findBySubject(Subject subject);
}

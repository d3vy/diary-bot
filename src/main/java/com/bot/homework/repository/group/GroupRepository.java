package com.bot.homework.repository.group;

import com.bot.homework.model.group.Group;
import com.bot.homework.model.subject.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
    Optional<Group> findByName(String name);

    List<Group> findBySubject(Subject subject);

    List<Group> findByTeacherTelegramId(Long telegramId);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.pupils WHERE g.id = :groupId")
    Optional<Group> findByIdWithPupils(@Param("groupId") Integer groupId);


}

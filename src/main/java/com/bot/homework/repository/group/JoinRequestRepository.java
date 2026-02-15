package com.bot.homework.repository.group;

import com.bot.homework.model.group.join.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Integer> {
    List<JoinRequest> findByGroupTeacherTelegramId(Long teacherId);
    Boolean existsByPupilTelegramIdAndGroupId(Long pupilId, Integer groupId);
}

package com.bot.homework.repository.user;

import com.bot.homework.model.user.pupil.Pupil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PupilRepository extends JpaRepository<Pupil, Long> {
    Optional<Pupil> findByTelegramId(Long telegramId);

    boolean existsByTelegramId(Long telegramId);


    @Query(
            value = """
                    SELECT *
                    FROM pupils p
                    WHERE p.first_name = split_part(:fullName, ' ', 1)
                      AND p.last_name  = split_part(:fullName, ' ', 2)
                    """,
            nativeQuery = true
    )
    Optional<Pupil> findByFullName(@Param("fullName") String fullName);

    List<Pupil> findByStudyGroupsId(Integer groupId);

    @Modifying
    @Query(
            value = "DELETE FROM pupil.pupil_study_group WHERE pupil_id = :pupilId AND study_group_id = :groupId",
            nativeQuery = true
    )
    void removeFromGroup(@Param("pupilId") Long pupilId, @Param("groupId") Integer groupId);
}

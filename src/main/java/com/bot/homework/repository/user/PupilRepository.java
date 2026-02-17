package com.bot.homework.repository.user;

import com.bot.homework.model.group.Group;
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
                    FROM pupil.pupils p
                    WHERE p.firstname = split_part(:fullName, ' ', 1)
                      AND p.lastname  = split_part(:fullName, ' ', 2)
                    """,
            nativeQuery = true
    )
    Optional<Pupil> findByFullName(@Param("fullName") String fullName);

    Optional<List<Group>> findStudyGroupsById(Long pupilId);

}

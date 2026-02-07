package com.bot.homework.repository;

import com.bot.homework.model.user.Pupil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PupilRepository extends JpaRepository<Pupil,Long> {
    Optional<Pupil> findByTelegramId(Long telegramId);
    boolean existsByTelegramId(Long telegramId);
}

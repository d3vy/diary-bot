package com.bot.homework.repository;

import com.bot.homework.model.registration.RegistrationContext;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationContextRepository extends JpaRepository<RegistrationContext, Long> {
}

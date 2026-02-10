package com.bot.homework.repository.registration;

import com.bot.homework.model.registration.RegistrationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationContextRepository extends JpaRepository<RegistrationContext, Long> {
}

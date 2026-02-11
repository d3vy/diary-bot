package com.bot.homework.repository.user;

import com.bot.homework.model.user.pupil.AddPupilContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddPupilContextRepository extends JpaRepository<AddPupilContext, Long> {
}

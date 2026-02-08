package com.bot.homework.repository;

import com.bot.homework.model.edit.EditPersonalInfoContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditPersonalInfoRepository extends JpaRepository<EditPersonalInfoContext, Long> {
}

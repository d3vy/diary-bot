package com.bot.homework.service;

import com.bot.homework.model.user.UserRole;
import com.bot.homework.model.user.User;
import com.bot.homework.repository.PupilRepository;
import com.bot.homework.repository.TeacherRepository;
import org.springframework.stereotype.Service;

@Service
public class UserRoleService {

    private final TeacherRepository teacherRepository;
    private final PupilRepository pupilRepository;

    public UserRoleService(TeacherRepository teacherRepository, PupilRepository pupilRepository) {
        this.teacherRepository = teacherRepository;
        this.pupilRepository = pupilRepository;
    }

    public UserRole defineUserRoleByTelegramId(Long telegramId) {
        User user = this.teacherRepository.findByTelegramId(telegramId).orElse(null);
        if (user == null) {
            user = this.pupilRepository.findByTelegramId(telegramId).orElse(null);
        } else {
            return UserRole.TEACHER;
        }
        if (user == null) {
            return UserRole.NOT_REGISTERED;
        }
        return UserRole.PUPIL;
    }
}

package com.bot.homework.model.registration;

import com.bot.homework.model.user.UserRole;
import jakarta.persistence.*;

@Entity
@Table(schema = "registration", name = "registration_context")
public class RegistrationContext {

    @Id
    @Column(nullable = false, unique = true)
    private Long telegramId;

    @Enumerated(EnumType.STRING)
    private RegistrationStep step;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String firstname;
    private String lastname;
    private String patronymic;

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public RegistrationStep getStep() {
        return step;
    }

    public void setStep(RegistrationStep step) {
        this.step = step;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }
}

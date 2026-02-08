package com.bot.homework.model.edit;

import com.bot.homework.model.user.UserRole;
import jakarta.persistence.*;

@Entity
@Table(schema = "registration", name = "edit_personal_info_context")
public class EditPersonalInfoContext {

    @Id
    @Column(nullable = false, unique = true)
    private Long telegramId;

    @Enumerated(EnumType.STRING)
    private EditPersonalInfoStep step;

    @Enumerated(EnumType.STRING)
    private UserRole edited_role;

    private String edited_firstname;
    private String edited_lastname;
    private String edited_patronymic;

    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public EditPersonalInfoStep getStep() {
        return step;
    }

    public void setStep(EditPersonalInfoStep step) {
        this.step = step;
    }

    public UserRole getEdited_role() {
        return edited_role;
    }

    public void setEdited_role(UserRole edited_role) {
        this.edited_role = edited_role;
    }

    public String getEdited_firstname() {
        return edited_firstname;
    }

    public void setEdited_firstname(String edited_firstname) {
        this.edited_firstname = edited_firstname;
    }

    public String getEdited_lastname() {
        return edited_lastname;
    }

    public void setEdited_lastname(String edited_lastname) {
        this.edited_lastname = edited_lastname;
    }

    public String getEdited_patronymic() {
        return edited_patronymic;
    }

    public void setEdited_patronymic(String edited_patronymic) {
        this.edited_patronymic = edited_patronymic;
    }
}

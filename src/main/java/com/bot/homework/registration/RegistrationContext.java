package com.bot.homework.registration;

public class RegistrationContext {

    private UserRole role;
    private RegistrationStep step = RegistrationStep.NONE;

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
}

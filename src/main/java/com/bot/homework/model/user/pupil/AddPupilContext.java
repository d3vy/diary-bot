package com.bot.homework.model.user.pupil;

import jakarta.persistence.*;

@Entity
@Table(schema = "pupil", name = "add_pupil_context")
public class AddPupilContext {

    @Id
    private Long teacherId;

    private Long chatId;

    @Enumerated(EnumType.STRING)
    private AddPupilStep step;

    private String pupilFullName;
    private String groupName;

    public AddPupilContext(
            Long teacherId,
            Long chatId,
            AddPupilStep step,
            String pupilFullName,
            String groupName
    ) {
        this.teacherId = teacherId;
        this.chatId = chatId;
        this.step = step;
        this.pupilFullName = pupilFullName;
        this.groupName = groupName;
    }

    public AddPupilContext() {
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public AddPupilStep getStep() {
        return step;
    }

    public void setStep(AddPupilStep step) {
        this.step = step;
    }

    public String getPupilFullName() {
        return pupilFullName;
    }

    public void setPupilFullName(String pupilFullName) {
        this.pupilFullName = pupilFullName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}

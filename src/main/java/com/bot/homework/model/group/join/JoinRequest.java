package com.bot.homework.model.group.join;

import com.bot.homework.model.group.Group;
import com.bot.homework.model.user.pupil.Pupil;
import jakarta.persistence.*;

@Entity
@Table(schema = "study_group", name = "join_requests")
public class JoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Pupil pupil;

    @ManyToOne
    private Group group;

    @Enumerated(EnumType.STRING)
    private JoinRequestStatus status;

    public JoinRequest(Integer id, Pupil pupil, Group group, JoinRequestStatus status) {
        this.id = id;
        this.pupil = pupil;
        this.group = group;
        this.status = status;
    }

    public JoinRequest() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Pupil getPupil() {
        return pupil;
    }

    public void setPupil(Pupil pupil) {
        this.pupil = pupil;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public JoinRequestStatus getStatus() {
        return status;
    }

    public void setStatus(JoinRequestStatus status) {
        this.status = status;
    }
}

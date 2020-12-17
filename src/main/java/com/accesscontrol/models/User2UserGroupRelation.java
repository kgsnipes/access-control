package com.accesscontrol.models;

import com.accesscontrol.config.AtomicIntegerConverter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
public class User2UserGroupRelation extends AbstractModel{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    @NotNull(message = "usergroup cannot be null")
    private String userGroupCode;


    @NotNull(message = "userid cannot be null")
    private String userId;

    @CreationTimestamp
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    private LocalDateTime updateDateTime;

    @Convert(converter = AtomicIntegerConverter.class)
    private AtomicInteger version;


    public User2UserGroupRelation() {
        this.version=new AtomicInteger(1);
    }

    public User2UserGroupRelation(String userGroupCode,  String userId) {
        this.userGroupCode = userGroupCode;
        this.userId = userId;
        this.version=new AtomicInteger(1);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserGroupCode() {
        return userGroupCode;
    }

    public void setUserGroupCode(String userGroupCode) {
        this.userGroupCode = userGroupCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public LocalDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(LocalDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public AtomicInteger getVersion() {
        return version;
    }

    public void setVersion(AtomicInteger version) {
        this.version = version;
    }
}

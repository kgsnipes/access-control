package com.accesscontrol.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class UserGroup2UserGroupRelation extends AbstractModel{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    private String parentUserGroupCode;

    private String childUserGroupCode;

    @CreationTimestamp
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    private LocalDateTime updateDateTime;

    public UserGroup2UserGroupRelation() {
    }

    public UserGroup2UserGroupRelation(String childUserGroupId, String parentUserGroupId) {
        this.parentUserGroupCode = parentUserGroupId;
        this.childUserGroupCode = childUserGroupId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParentUserGroupCode() {
        return parentUserGroupCode;
    }

    public void setParentUserGroupCode(String parentUserGroupCode) {
        this.parentUserGroupCode = parentUserGroupCode;
    }

    public String getChildUserGroupCode() {
        return childUserGroupCode;
    }

    public void setChildUserGroupCode(String childUserGroupCode) {
        this.childUserGroupCode = childUserGroupCode;
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
}

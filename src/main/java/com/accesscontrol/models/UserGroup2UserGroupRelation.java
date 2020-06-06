package com.accesscontrol.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class UserGroup2UserGroupRelation {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private Long parentUserGroupId;

    private Long childUserGroupId;

    public UserGroup2UserGroupRelation() {
    }

    public UserGroup2UserGroupRelation(Long parentUserGroupId, Long childUserGroupId) {
        this.parentUserGroupId = parentUserGroupId;
        this.childUserGroupId = childUserGroupId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentUserGroupId() {
        return parentUserGroupId;
    }

    public void setParentUserGroupId(Long parentUserGroupId) {
        this.parentUserGroupId = parentUserGroupId;
    }

    public Long getChildUserGroupId() {
        return childUserGroupId;
    }

    public void setChildUserGroupId(Long childUserGroupId) {
        this.childUserGroupId = childUserGroupId;
    }
}

package com.accesscontrol.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AccessPermission2UserGroupRelation {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private Long accessPermissionId;

    private String userGroupCode;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccessPermissionId() {
        return accessPermissionId;
    }

    public void setAccessPermissionId(Long accessPermissionId) {
        this.accessPermissionId = accessPermissionId;
    }

    public String getUserGroupId() {
        return userGroupCode;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupCode = userGroupId;
    }
}

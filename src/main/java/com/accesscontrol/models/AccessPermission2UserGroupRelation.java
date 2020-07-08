package com.accesscontrol.models;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class AccessPermission2UserGroupRelation extends AbstractModel{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "accessPermissionId  cannot be null")
    private Long accessPermissionId;

    @NotNull(message = "userGroupCode cannot be null")
    private String userGroupCode;


    @NotNull(message = "Enabled flag cannot be null")
    private Boolean enabled;

    public AccessPermission2UserGroupRelation() {
    }

    public AccessPermission2UserGroupRelation( Long accessPermissionId,  String userGroupCode,  Boolean enabled) {
        this.accessPermissionId = accessPermissionId;
        this.userGroupCode = userGroupCode;
        this.enabled = enabled;
    }

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

    public String getUserGroupCode() {
        return userGroupCode;
    }

    public void setUserGroupCode(String userGroupCode) {
        this.userGroupCode = userGroupCode;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

}

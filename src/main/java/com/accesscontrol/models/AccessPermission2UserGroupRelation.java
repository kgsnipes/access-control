package com.accesscontrol.models;

import com.accesscontrol.config.AtomicIntegerConverter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

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

    @CreationTimestamp
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    private LocalDateTime updateDateTime;

    @Convert(converter = AtomicIntegerConverter.class)
    private AtomicInteger version;

    public AccessPermission2UserGroupRelation() {
        this.version=new AtomicInteger(1);
    }

    public AccessPermission2UserGroupRelation( Long accessPermissionId,  String userGroupCode,  Boolean enabled) {
        this.accessPermissionId = accessPermissionId;
        this.userGroupCode = userGroupCode;
        this.enabled = enabled;
        this.version=new AtomicInteger(1);
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

package com.accesscontrol.models;

import com.accesscontrol.config.AtomicIntegerConverter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Convert(converter = AtomicIntegerConverter.class)
    private AtomicInteger version;

    public UserGroup2UserGroupRelation() {
        this.version=new AtomicInteger(1);
    }

    public UserGroup2UserGroupRelation(String childUserGroupId, String parentUserGroupId) {
        this.parentUserGroupCode = parentUserGroupId;
        this.childUserGroupCode = childUserGroupId;
        this.version=new AtomicInteger(1);
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

    public AtomicInteger getVersion() {
        return version;
    }

    public void setVersion(AtomicInteger version) {
        this.version = version;
    }
}

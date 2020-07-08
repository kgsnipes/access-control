package com.accesscontrol.models;

import com.opencsv.bean.CsvBindByName;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class UserGroup2UserGroupRelation extends AbstractModel{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "parentgroupcode")
    private String parentUserGroupCode;
    @CsvBindByName(column = "usergroupcode")
    private String childUserGroupCode;

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
}

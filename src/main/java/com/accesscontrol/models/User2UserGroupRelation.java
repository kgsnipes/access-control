package com.accesscontrol.models;

import com.opencsv.bean.CsvBindByName;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class User2UserGroupRelation extends AbstractModel{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "usergroupcode")
    @NotNull(message = "usergroup cannot be null")
    private String userGroupCode;

    @CsvBindByName(column = "userid")
    @NotNull(message = "userid cannot be null")
    private String userId;

    public User2UserGroupRelation() {
    }

    public User2UserGroupRelation(String userGroupCode,  String userId) {
        this.userGroupCode = userGroupCode;
        this.userId = userId;
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


}

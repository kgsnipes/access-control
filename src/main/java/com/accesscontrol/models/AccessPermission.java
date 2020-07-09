package com.accesscontrol.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(indexes = { @Index(name="accessPermissionIndex",columnList ="permission,resource") })
public class AccessPermission extends AbstractModel{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    @NotNull(message = "Permission cannot be null")
    private String permission;


    @NotNull(message = "Permission cannot be null")
    private String resource;

    public AccessPermission() {
    }

    public AccessPermission( String permission,  String resource) {
        this.permission = permission;
        this.resource = resource;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

}

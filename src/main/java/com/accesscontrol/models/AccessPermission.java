package com.accesscontrol.models;

import com.opencsv.bean.CsvBindByName;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class AccessPermission {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "permission")
    @NotNull(message = "Permission cannot be null")
    private String permission;

    @CsvBindByName(column = "resource")
    @NotNull(message = "Permission cannot be null")
    private String resource;


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

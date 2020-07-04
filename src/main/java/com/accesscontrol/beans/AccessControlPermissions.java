package com.accesscontrol.beans;

import java.util.Arrays;
import java.util.List;

public class AccessControlPermissions {

    private List<String> permissions= Arrays.asList("READ","WRITE","DELETE","EXECUTE");

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}

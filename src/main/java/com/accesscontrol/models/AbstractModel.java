package com.accesscontrol.models;

import java.util.Objects;

public abstract class AbstractModel {

    public abstract Long getId();

    @Override
    public int hashCode() {
        if(Objects.nonNull(getId()))
            return getId().hashCode();
        else
            return new Long(System.currentTimeMillis()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getId().equals(((AbstractModel)obj).getId());
    }

    @Override
    public String toString() {
        return getId().toString();
    }
}

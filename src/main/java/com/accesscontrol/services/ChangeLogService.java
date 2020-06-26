package com.accesscontrol.services;

import com.accesscontrol.beans.AccessControlContext;

public interface ChangeLogService {

    void logChange(Long pk, String type,String action, Object previousState, Object newState, AccessControlContext context);
}

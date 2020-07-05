package com.accesscontrol.repository;

import com.accesscontrol.models.AccessPermission;
import com.accesscontrol.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessPermissionRepository extends JpaRepository<AccessPermission,Long> {

    AccessPermission findByPermissionTypeAndPermission(String permissionType,String permission);

}

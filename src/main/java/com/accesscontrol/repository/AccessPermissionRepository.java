package com.accesscontrol.repository;

import com.accesscontrol.models.AccessPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessPermissionRepository extends JpaRepository<AccessPermission,Long> {

    AccessPermission findByResourceAndPermission(String resource,String permission);

    @Query("SELECT ap FROM AccessPermission ap JOIN AccessPermission2UserGroupRelation apug on apug.accessPermissionId=ap.id WHERE apug.userGroupCode= :userGroupCode and apug.enabled= :enabledFlag ")
    Page<AccessPermission> findPermissionByUserGroupCode(@Param("userGroupCode") String userGroupCode,@Param("enabledFlag") Boolean enabled, Pageable pageable);

    @Query("SELECT ap FROM AccessPermission ap JOIN AccessPermission2UserGroupRelation apug on apug.accessPermissionId=ap.id WHERE apug.userGroupCode= :userGroupCode")
    Page<AccessPermission> findPermissionByUserGroupCode(@Param("userGroupCode") String userGroupCode, Pageable pageable);


    @Query("SELECT ap FROM AccessPermission ap JOIN AccessPermission2UserGroupRelation apug on apug.accessPermissionId=ap.id WHERE apug.userGroupCode= :userGroupCode and apug.enabled= :enabledFlag  and ap.resource=:resource")
    Page<AccessPermission> findPermissionByUserGroupCodeAndResource(@Param("userGroupCode") String userGroupCode,@Param("resource") String resource,@Param("enabledFlag") Boolean enabled, Pageable pageable);

    @Query("SELECT ap FROM AccessPermission ap JOIN AccessPermission2UserGroupRelation apug on apug.accessPermissionId=ap.id WHERE apug.userGroupCode= :userGroupCode  and ap.resource=:resource")
    Page<AccessPermission> findPermissionByUserGroupCodeAndResource(@Param("userGroupCode") String userGroupCode,@Param("resource") String resource, Pageable pageable);
}

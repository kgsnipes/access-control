package com.accesscontrol.repository;


import com.accesscontrol.models.AccessPermission2UserGroupRelation;
import com.accesscontrol.models.User2UserGroupRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessPermission2UserGroupRelationRepository extends JpaRepository<AccessPermission2UserGroupRelation,Long> {

    Page<AccessPermission2UserGroupRelation> findByUserGroupCode(String userGroupCode, Pageable pageable);

    AccessPermission2UserGroupRelation findByUserGroupCodeAndAccessPermissionId(String userGroupCode, Long accessPermissionId);

}

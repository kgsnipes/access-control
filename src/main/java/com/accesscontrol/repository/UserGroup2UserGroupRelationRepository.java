package com.accesscontrol.repository;


import com.accesscontrol.models.User2UserGroupRelation;
import com.accesscontrol.models.UserGroup2UserGroupRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroup2UserGroupRelationRepository extends JpaRepository<UserGroup2UserGroupRelation,Long> {

    List<UserGroup2UserGroupRelation> findByParentUserGroupCode(String userGroupCode);

    List<UserGroup2UserGroupRelation> findByChildUserGroupCode(String userGroupCode);

    UserGroup2UserGroupRelation findByChildUserGroupCodeAndParentUserGroupCode(String childUserGroupCode, String parentUserGroupCode);

}

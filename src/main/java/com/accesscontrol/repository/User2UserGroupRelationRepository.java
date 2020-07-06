package com.accesscontrol.repository;


import com.accesscontrol.beans.PageResult;
import com.accesscontrol.models.User2UserGroupRelation;
import com.accesscontrol.models.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface User2UserGroupRelationRepository extends JpaRepository<User2UserGroupRelation,Long> {

    List<User2UserGroupRelation> findByUserGroupCode(String userGroupCode);

    List<User2UserGroupRelation> findByUserId(String userId);

    User2UserGroupRelation findByUserIdAndUserGroupCode(String userId,String userGroupCode);

}

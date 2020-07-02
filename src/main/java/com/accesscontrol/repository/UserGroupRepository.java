package com.accesscontrol.repository;


import com.accesscontrol.models.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup,Long> {

    UserGroup findByCode(String code);

    UserGroup findByName(String name);

    @Query("SELECT u FROM UserGroup u WHERE u.code LIKE CONCAT('%',:searchterm,'%') or u.name LIKE CONCAT('%',:searchterm,'%') ")
    Page<UserGroup> findUserGroups(@Param("searchterm") String searchterm, Pageable pageable);

}

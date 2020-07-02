package com.accesscontrol.repository;

import com.accesscontrol.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    User findByUserId(String userId);

    @Query("SELECT u FROM User u WHERE u.userId LIKE CONCAT('%',:searchterm,'%') or u.firstName LIKE CONCAT('%',:searchterm,'%') or u.lastName LIKE CONCAT('%',:searchterm,'%')")
    Page<User> findUsers(@Param("searchterm") String searchterm, Pageable pageable);

}

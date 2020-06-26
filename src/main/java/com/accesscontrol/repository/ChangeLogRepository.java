package com.accesscontrol.repository;

import com.accesscontrol.models.ChangeLog;
import com.accesscontrol.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangeLogRepository extends JpaRepository<ChangeLog,Long> {

}

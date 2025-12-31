package com.abed.perfumeshop.admin.repo;

import com.abed.perfumeshop.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepo extends JpaRepository<Admin, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long excludedAdminId);

    Optional<Admin> findByEmail(String email);

}

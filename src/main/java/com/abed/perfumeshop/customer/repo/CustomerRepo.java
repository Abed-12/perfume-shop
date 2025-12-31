package com.abed.perfumeshop.customer.repo;

import com.abed.perfumeshop.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long excludedCustomerId);

    Optional<Customer> findByEmail(String email);

    @Query("SELECT c FROM Customer c " +
            "WHERE (:email IS null or c.email = :email)")
    Page<Customer> findAllByFilter(@Param("email") String email, Pageable pageable);

}

package com.abed.perfumeshop.customer.repo;

import com.abed.perfumeshop.common.projection.EmailRecipientProjection;
import com.abed.perfumeshop.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long excludedCustomerId);

    Optional<Customer> findByEmail(String email);

    @Query("SELECT c.email AS email, " +
            "CONCAT(c.firstName, ' ', c.lastName) AS name, " +
            "true AS isCustomer " +
            "FROM Customer c")
    List<EmailRecipientProjection> findAllEmailProjections();

    @Query("SELECT c FROM Customer c " +
            "WHERE (:email IS NULL OR c.email = :email)")
    Page<Customer> findAllOrByEmail(@Param("email") String email, Pageable pageable);

}

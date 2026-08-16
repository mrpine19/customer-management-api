package com.br.customer.repository;

import com.br.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(value = "SELECT * FROM customer WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))", nativeQuery = true)
    List<Customer> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query(value = "SELECT * FROM customer WHERE LOWER(status) = LOWER(:status)", nativeQuery = true)
    List<Customer> findByStatusIgnoreCase(@Param("status") String status);
}

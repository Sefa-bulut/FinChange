package com.example.finchange.customer.repository;

import com.example.finchange.customer.model.CustomerConsents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerConsentsRepository extends JpaRepository<CustomerConsents, Integer> {
    List<CustomerConsents> findByClientId(int clientId);
}

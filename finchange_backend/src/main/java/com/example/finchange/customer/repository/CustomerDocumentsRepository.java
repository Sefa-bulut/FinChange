package com.example.finchange.customer.repository;

import com.example.finchange.customer.model.CustomerDocuments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerDocumentsRepository extends JpaRepository<CustomerDocuments, Integer> {
    List<CustomerDocuments> findByClientId(int clientId);
}

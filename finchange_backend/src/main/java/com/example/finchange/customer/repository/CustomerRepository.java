package com.example.finchange.customer.repository;

import com.example.finchange.customer.model.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;


public interface CustomerRepository extends JpaRepository<Customers, Integer>,JpaSpecificationExecutor<Customers> {

    boolean existsByCustomerCode(String customerCode);
    
    boolean existsByEmail(String email);
    
    boolean existsByIdNumber(String tcKimlikNo);
    
    boolean existsByTaxIDNumber(String vergiKimlikNo);
    // Musteri guncelleme icin gerekli olanlar
    Optional<Customers> findByEmailAndIdNot(String email, Integer id);
    Optional<Customers> findByIdNumberAndIdNot(String tcKimlikNo, Integer id);
    Optional<Customers> findByTaxIDNumberAndIdNot(String vergiKimlikNo, Integer id);
    Optional<Customers> findByMersisNoAndIdNot(String mersisNo, Integer id);
    Optional<Customers> findByCustomerCode(String musteriKodu);


    long countByIdIn(List<Integer> ids);
    List<Customers> findAllByStatus(String status);


}

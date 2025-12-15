package com.example.finchange.customer.repository;

import com.example.finchange.customer.dto.CurrencyBalanceDTO;
import com.example.finchange.customer.model.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Integer> {
    List<CustomerAccount> findByCustomer_Id(Integer customerId);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByCustomer_IdAndAccountName(Integer customerId, String accountName);
    List<CustomerAccount> findByCustomerIdIn(List<Integer> customerIds);

    @Query("SELECT CASE WHEN (ca.balance - ca.blockedBalance) >= :amount THEN true ELSE false END " +
            "FROM CustomerAccount ca WHERE ca.id = :accountId")
    boolean hasSufficientAvailableBalance(@Param("accountId") Integer accountId, @Param("amount") BigDecimal amount);


    List<CustomerAccount> findByCustomer_IdAndActive(Integer customerId, Boolean active);


    @Query("SELECT ca.currency AS currency, SUM(ca.balance) AS totalBalance " +
            "FROM CustomerAccount ca " +
            "WHERE ca.customer.id = :customerId " +
            "GROUP BY ca.currency")
    List<CurrencyBalanceDTO> findTotalBalancesByCurrency(@Param("customerId") Integer customerId);
}




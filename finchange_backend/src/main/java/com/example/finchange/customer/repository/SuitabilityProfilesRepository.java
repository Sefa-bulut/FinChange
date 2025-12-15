package com.example.finchange.customer.repository;

import com.example.finchange.customer.model.SuitabilityProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SuitabilityProfilesRepository extends JpaRepository<SuitabilityProfiles, Integer> {

    // Bu metod verimli bir şekilde müşterinin aktif olan son yerindelik profilini bulur
    Optional<SuitabilityProfiles> findFirstByCustomerIdAndIsActiveTrueOrderByIdDesc(Integer customerId);

}

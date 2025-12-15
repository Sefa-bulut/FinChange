package com.example.finchange.customer.service.impl;

import com.example.finchange.customer.dto.CustomerCreateRequest;
import com.example.finchange.customer.dto.CustomerUpdateRequest;
import com.example.finchange.customer.dto.CustomerDetailDto;
import com.example.finchange.customer.dto.CustomerListDto;
import com.example.finchange.customer.dto.SuitabilityProfileDto;
import com.example.finchange.customer.dto.EligibleCustomerAccountDto;
import com.example.finchange.customer.dto.EligibleCustomerDto;
import com.example.finchange.customer.exception.CustomerNotFoundException;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.customer.model.CustomerType;
import com.example.finchange.customer.model.Customers;
import com.example.finchange.customer.model.SuitabilityProfiles;
import com.example.finchange.customer.repository.CustomerAccountRepository;
import com.example.finchange.customer.repository.CustomerRepository;
import com.example.finchange.customer.repository.SuitabilityProfilesRepository;
import com.example.finchange.customer.service.CustomerService;
import com.example.finchange.customer.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final SuitabilityProfilesRepository profileRepository;
    private final DocumentService documentService;
    private final CustomerAccountRepository customerAccountRepository; 

    @Override
    @Transactional
    public Integer createClient(CustomerCreateRequest request, MultipartFile vergiLevhasi, MultipartFile kvkkBelgesi,
                                MultipartFile portfoyYonetimSozlesmesi, MultipartFile elektronikBildirimIzni) {
        // 1. Benzersizlik kontrolü
        if (customerRepository.existsByCustomerCode(request.getMusteriKodu())) {
            throw new IllegalArgumentException("Bu müşteri kodu zaten kullanımda: " + request.getMusteriKodu());
        }
        
        // E-posta benzersizlik kontrolü
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kullanımda: " + request.getEmail());
        }
        
        // TC Kimlik No benzersizlik kontrolü (bireysel müşteri için)
        if (request.getCustomerType() == CustomerType.GERCEK && request.getTcKimlikNo() != null) {
            if (customerRepository.existsByIdNumber(request.getTcKimlikNo())) {
                throw new IllegalArgumentException("Bu T.C. Kimlik No zaten kullanımda: " + request.getTcKimlikNo());
            }
        }
        
        // Vergi Kimlik No benzersizlik kontrolü (kurumsal müşteri için)
        if (request.getCustomerType() == CustomerType.TUZEL && request.getVergiKimlikNo() != null) {
            if (customerRepository.existsByTaxIDNumber(request.getVergiKimlikNo())) {
                throw new IllegalArgumentException("Bu Vergi Kimlik No zaten kullanımda: " + request.getVergiKimlikNo());
            }
        }

        // 2. İşlemi yapan kullanıcı ID'sini JWT'den güvenli bir şekilde al
        Integer currentUserId = getCurrentUserId();

        // 3. Yeni Customer nesnesini oluştur ve DTO'dan gelen verilerle doldur
        Customers customer = new Customers();
        customer.setCustomerCode(request.getMusteriKodu());
        customer.setCustomerType(request.getCustomerType());
        customer.setPhone(request.getTelefon());
        customer.setEmail(request.getEmail());
        customer.setStatus("Aktif"); // Varsayılan durum

        // Müşteri tipine göre özel alanları doldur
        if (request.getCustomerType() == CustomerType.GERCEK) {
            // BİREYSEL MÜŞTERİ: ikametgah_adresi kullan
            customer.setResidenceAddress(request.getAdres());
            customer.setName(request.getAd());
            customer.setLastName(request.getSoyad());
            customer.setIdNumber(request.getTcKimlikNo());
            customer.setDateOfBirth(request.getDogumTarihi());
        } else { // TUZEL
            // KURUMSAL MÜŞTERİ: sadece merkez_adresi kullan
            customer.setCenterAddress(request.getAdres());
            customer.setCompanyTitle(request.getSirketUnvani());
            customer.setTaxIDNumber(request.getVergiKimlikNo());
            customer.setMersisNo(request.getMersisNo());
            customer.setAuthorizedPersonNameSurname(request.getYetkiliKisiAdSoyad());
        }

        // Denetim alanlarını manuel set ediyoruz
        customer.setRegisteredUserId(currentUserId);

        // Müşteriyi önce kaydet
        Customers savedCustomer = customerRepository.save(customer);

        // 4. Yerindelik profilini oluştur ve kaydet
        SuitabilityProfiles profile = new SuitabilityProfiles();
        profile.setCustomer(savedCustomer); // Customer entity'sinin referansını set ediyoruz
        profile.setIsActive(true);

        // DTO'dan gelen profil bilgilerini set et
        SuitabilityProfileDto profileDto = request.getSuitabilityProfile();
        profile.setYatirimAmaci(profileDto.getYatirimAmaci());
        profile.setRiskToleransi(profileDto.getRiskToleransi());
        profile.setYatirimSuresi(profileDto.getYatirimSuresi());
        profile.setMaliDurum(profileDto.getMaliDurum());
        profile.setYatirimDeneyimi(profileDto.getYatirimDeneyimi());
        profile.setLikiditeIhtiyaci(profileDto.getLikiditeIhtiyaci());
        profile.setVergiDurumu(profileDto.getVergiDurumu());
        profile.setSirketYatirimStratejisi(profileDto.getSirketYatirimStratejisi());
        profile.setRiskYonetimiPolitikasi(profileDto.getRiskYonetimiPolitikasi());
        profile.setFinansalDurumTuzel(profileDto.getFinansalDurumTuzel());
        profile.setYatirimSuresiVadeTuzel(profileDto.getYatirimSuresiVadeTuzel());

        profileRepository.save(profile);

        // Belgeleri yükle
        try {
            System.out.println("DOSYA UPLOAD AKTIF - MinIO düzeltildi!");
            if (vergiLevhasi != null && !vergiLevhasi.isEmpty()) {
                documentService.uploadDocument(savedCustomer.getId(), vergiLevhasi, "VERGI_LEVHASI");
            }
            
            if (kvkkBelgesi != null && !kvkkBelgesi.isEmpty()) {
                documentService.uploadDocument(savedCustomer.getId(), kvkkBelgesi, "KVKK");
            }
            
            if (portfoyYonetimSozlesmesi != null && !portfoyYonetimSozlesmesi.isEmpty()) {
                documentService.uploadDocument(savedCustomer.getId(), portfoyYonetimSozlesmesi, "PORTFOY_YONETIM_SOZLESMESI");
            }
            
            if (elektronikBildirimIzni != null && !elektronikBildirimIzni.isEmpty()) {
                documentService.uploadDocument(savedCustomer.getId(), elektronikBildirimIzni, "ELEKTRONIK_BILDIRIM_IZNI");
            }
        } catch (Exception e) {
            System.out.println("Dosya upload hatası: " + e.getMessage());
            throw new RuntimeException("Belge yükleme sırasında hata oluştu: " + e.getMessage(), e);
        }

        return savedCustomer.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerListDto> getAllClients(String name, String email, String clientCode, String customerType, String status) {
        Specification<Customers> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("ad")), "%" + name.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("soyad")), "%" + name.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("sirketUnvani")), "%" + name.toLowerCase() + "%")
                ));
            }
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (clientCode != null && !clientCode.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("customerCode")), "%" + clientCode.toLowerCase() + "%"));
            }
            if (customerType != null && !customerType.isBlank()) {
                predicates.add(cb.equal(root.get("customerType"), CustomerType.valueOf(customerType.toUpperCase())));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return customerRepository.findAll(spec).stream()
            .map(c -> {
                String gorunenAd = c.getCustomerType() == CustomerType.GERCEK ?
                        c.getName() + " " + c.getLastName() : c.getCompanyTitle();
                return new CustomerListDto(
                        c.getId(), c.getCustomerCode(), gorunenAd, c.getCustomerType(), c.getStatus(),
                        c.getEmail(), c.getPhone(), c.getName(), c.getLastName(), c.getCompanyTitle(), c.getStatus()
                );
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDetailDto getClientById(Integer id) {
        Customers c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı: " + id));

        SuitabilityProfiles profile = profileRepository
                .findFirstByCustomerIdAndIsActiveTrueOrderByIdDesc(id)
                .orElse(null);

        CustomerDetailDto dto = new CustomerDetailDto();
        
        // --- TEMEL BİLGİLER ---
        dto.setId(c.getId());
        dto.setMusteriKodu(c.getCustomerCode());
        dto.setCustomerType(c.getCustomerType());
        dto.setDurum(c.getStatus());

        // --- ORTAK İLETİŞİM BİLGİLERİ ---
        dto.setTelefon(c.getPhone());
        dto.setEmail(c.getEmail());

        // --- MÜŞTERİ TİPİNE GÖRE ÖZEL BİLGİLER ---
        if (c.getCustomerType() == CustomerType.GERCEK) {
            // Bireysel Müşteri
            dto.setAd(c.getName());
            dto.setSoyad(c.getLastName());
            dto.setGorunenAd(c.getName() + " " + c.getLastName());
            dto.setTckn(c.getIdNumber());
            dto.setDogumTarihi(c.getDateOfBirth());
            dto.setAdres(c.getResidenceAddress()); // Bireysel için ikametgah adresi
        } else { // TUZEL
            // Kurumsal Müşteri
            dto.setSirketUnvani(c.getCompanyTitle());
            dto.setGorunenAd(c.getCompanyTitle());
            dto.setVergiNo(c.getTaxIDNumber());
            dto.setMersisNo(c.getMersisNo());
            dto.setYetkiliKisiAdSoyad(c.getAuthorizedPersonNameSurname());
            dto.setAdres(c.getCenterAddress()); // Kurumsal için merkez adresi
        }

        // --- YERİNDELİK PROFİLİ BİLGİLERİ ---
        if (profile != null) {
            SuitabilityProfileDto profileDto = new SuitabilityProfileDto();
            
            // Bireysel Alanlar
            profileDto.setYatirimAmaci(profile.getYatirimAmaci());
            profileDto.setYatirimSuresi(profile.getYatirimSuresi());
            profileDto.setRiskToleransi(profile.getRiskToleransi());
            profileDto.setMaliDurum(profile.getMaliDurum());
            profileDto.setYatirimDeneyimi(profile.getYatirimDeneyimi());
            profileDto.setLikiditeIhtiyaci(profile.getLikiditeIhtiyaci());
            profileDto.setVergiDurumu(profile.getVergiDurumu());

            // Kurumsal Alanlar
            profileDto.setSirketYatirimStratejisi(profile.getSirketYatirimStratejisi());
            profileDto.setRiskYonetimiPolitikasi(profile.getRiskYonetimiPolitikasi());
            profileDto.setFinansalDurumTuzel(profile.getFinansalDurumTuzel());
            profileDto.setYatirimSuresiVadeTuzel(profile.getYatirimSuresiVadeTuzel());
            
            dto.setSuitabilityProfile(profileDto);
        }

        return dto;
    }

    private Integer getCurrentUserId() {
        
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            return Integer.parseInt(jwt.getClaimAsString("userId"));
        }
        return -1;
    }
@Override
    @Transactional
    public CustomerDetailDto updateClient(Integer id, CustomerUpdateRequest request) {
        Customers customerToUpdate = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Güncellenecek müşteri bulunamadı: " + id));

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(customerToUpdate.getEmail())) {
            customerRepository.findByEmailAndIdNot(request.getEmail(), id).ifPresent(c -> {
                throw new IllegalArgumentException("Bu e-posta adresi zaten başka bir müşteriye ait.");
            });
            customerToUpdate.setEmail(request.getEmail());
        }

        if (customerToUpdate.getCustomerType() == CustomerType.GERCEK && request.getTcKimlikNo() != null && !request.getTcKimlikNo().equals(customerToUpdate.getIdNumber())) {
            customerRepository.findByIdNumberAndIdNot(request.getTcKimlikNo(), id).ifPresent(c -> {
                throw new IllegalArgumentException("Bu T.C. Kimlik No zaten başka bir müşteriye ait.");
            });
            customerToUpdate.setIdNumber(request.getTcKimlikNo());
        }

        if (customerToUpdate.getCustomerType() == CustomerType.TUZEL && request.getVergiKimlikNo() != null && !request.getVergiKimlikNo().equals(customerToUpdate.getTaxIDNumber())) {
            customerRepository.findByTaxIDNumberAndIdNot(request.getVergiKimlikNo(), id).ifPresent(c -> {
                throw new IllegalArgumentException("Bu Vergi Kimlik No zaten başka bir müşteriye ait.");
            });
            customerToUpdate.setTaxIDNumber(request.getVergiKimlikNo());
        }

        if (customerToUpdate.getCustomerType() == CustomerType.TUZEL && request.getMersisNo() != null && !request.getMersisNo().equals(customerToUpdate.getMersisNo())) {
            customerRepository.findByMersisNoAndIdNot(request.getMersisNo(), id).ifPresent(c -> {
                throw new IllegalArgumentException("Bu Mersis No zaten başka bir müşteriye ait.");
            });
            customerToUpdate.setMersisNo(request.getMersisNo());
        }

        if (request.getTelefon() != null) {
            customerToUpdate.setPhone(request.getTelefon());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
        customerToUpdate.setStatus(request.getStatus());
        }
        if (request.getAdres() != null) {
            if (customerToUpdate.getCustomerType() == CustomerType.GERCEK) {
                customerToUpdate.setResidenceAddress(request.getAdres());
            } else {
                customerToUpdate.setCenterAddress(request.getAdres());
            }
        }

        if (customerToUpdate.getCustomerType() == CustomerType.GERCEK) {
            if (request.getAd() != null) customerToUpdate.setName(request.getAd());
            if (request.getSoyad() != null) customerToUpdate.setLastName(request.getSoyad());
            if (request.getDogumTarihi() != null) customerToUpdate.setDateOfBirth(request.getDogumTarihi());
        } else { // TUZEL
            if (request.getSirketUnvani() != null) customerToUpdate.setCompanyTitle(request.getSirketUnvani());
            if (request.getYetkiliKisiAdSoyad() != null) customerToUpdate.setAuthorizedPersonNameSurname(request.getYetkiliKisiAdSoyad());
        }

        if (request.getSuitabilityProfile() != null) {
            profileRepository.findFirstByCustomerIdAndIsActiveTrueOrderByIdDesc(id)
                    .ifPresent(oldProfile -> {
                        oldProfile.setIsActive(false);
                        profileRepository.save(oldProfile);
                    });

            SuitabilityProfiles newProfile = new SuitabilityProfiles();
            newProfile.setCustomer(customerToUpdate);
            newProfile.setIsActive(true);

            SuitabilityProfileDto profileDto = request.getSuitabilityProfile();
            newProfile.setYatirimAmaci(profileDto.getYatirimAmaci());
            newProfile.setYatirimSuresi(profileDto.getYatirimSuresi());
            newProfile.setRiskToleransi(profileDto.getRiskToleransi());
            newProfile.setMaliDurum(profileDto.getMaliDurum());
            newProfile.setYatirimDeneyimi(profileDto.getYatirimDeneyimi());
            newProfile.setLikiditeIhtiyaci(profileDto.getLikiditeIhtiyaci());
            newProfile.setVergiDurumu(profileDto.getVergiDurumu());
            newProfile.setSirketYatirimStratejisi(profileDto.getSirketYatirimStratejisi());
            newProfile.setRiskYonetimiPolitikasi(profileDto.getRiskYonetimiPolitikasi());
            newProfile.setFinansalDurumTuzel(profileDto.getFinansalDurumTuzel());
            newProfile.setYatirimSuresiVadeTuzel(profileDto.getYatirimSuresiVadeTuzel());

            profileRepository.save(newProfile);
        }

        customerRepository.save(customerToUpdate);

        return getClientById(id);

}

    @Override
    public Customers getCustomerByCustomerCode(String customerCode) {
        return customerRepository.findByCustomerCode(customerCode.trim())
                .orElseThrow(() -> new CustomerNotFoundException("Müşteri bulunamadı: " + customerCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAccount> getCustomerAccounts(Integer customerId) {
        return customerAccountRepository.findByCustomer_Id(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibleCustomerDto> getEligibleClients() {
        List<Customers> activeCustomers = customerRepository.findAllByStatus("Aktif");
        if (activeCustomers.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> customerIds = activeCustomers.stream().map(Customers::getId).collect(Collectors.toList());
        
        List<CustomerAccount> allAccounts = customerAccountRepository.findByCustomerIdIn(customerIds);

        Map<Integer, List<CustomerAccount>> accountsByCustomerId = allAccounts.stream()
                .collect(Collectors.groupingBy(acc -> acc.getCustomer().getId()));

        return activeCustomers.stream().map(customer -> {
            List<CustomerAccount> customerAccounts = accountsByCustomerId.getOrDefault(customer.getId(), new ArrayList<>());
            
            List<EligibleCustomerAccountDto> accountDtos = customerAccounts.stream().map(account ->
                EligibleCustomerAccountDto.builder()
                    .id(account.getId())
                    .accountName(account.getAccountName())
                    .currency(account.getCurrency())
                    .balance(account.getBalance())
                    .blockedBalance(account.getBlockedBalance())
                    .build()
            ).collect(Collectors.toList());

            String gorunenAd = customer.getCustomerType() == CustomerType.GERCEK ?
                    customer.getName() + " " + customer.getLastName() : customer.getCompanyTitle();

            return EligibleCustomerDto.builder()
                    .id(customer.getId())
                    .musteriKodu(customer.getCustomerCode())
                    .gorunenAd(gorunenAd)
                    .accounts(accountDtos)
                    .build();
        }).collect(Collectors.toList());
    }

}
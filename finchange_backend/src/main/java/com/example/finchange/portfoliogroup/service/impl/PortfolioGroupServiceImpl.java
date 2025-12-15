package com.example.finchange.portfoliogroup.service.impl;

import com.example.finchange.portfoliogroup.service.PortfolioGroupService;
import com.example.finchange.common.util.SecurityUtils;
import com.example.finchange.customer.model.Customers;
import com.example.finchange.customer.repository.CustomerRepository;
import com.example.finchange.customer.repository.CustomerAccountRepository;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.portfolio.repository.CustomerAssetRepository;
import com.example.finchange.portfolio.model.CustomerAsset;
import com.example.finchange.portfoliogroup.dto.AddMembersRequest;
import com.example.finchange.portfoliogroup.dto.CreateGroupRequest;
import com.example.finchange.portfoliogroup.dto.GroupMemberResponse;
import com.example.finchange.portfoliogroup.dto.GroupResponse;
import com.example.finchange.portfoliogroup.exception.GroupNotFoundException;
import com.example.finchange.portfoliogroup.mapper.PortfolioGroupMapper;
import com.example.finchange.portfoliogroup.model.PortfolioGroup;
import com.example.finchange.portfoliogroup.model.PortfolioGroupMember;
import com.example.finchange.portfoliogroup.repository.PortfolioGroupMemberRepository;
import com.example.finchange.portfoliogroup.repository.PortfolioGroupRepository;
import com.example.finchange.user.exception.UserNotFoundException;
import com.example.finchange.user.model.User;
import com.example.finchange.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioGroupServiceImpl implements PortfolioGroupService {

    private final PortfolioGroupRepository groupRepository;
    private final PortfolioGroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final CustomerAssetRepository customerAssetRepository; 
    private final SecurityUtils securityUtils;
    private final PortfolioGroupMapper groupMapper;
    
    @Override
    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Mevcut kullanıcı bulunamadı."));

        groupRepository.findByGroupNameAndOwner_Id(request.getGroupName(), currentUserId)
                .ifPresent(g -> {
                    throw new IllegalStateException("Bu isimde bir grubunuz zaten mevcut.");
                });

        PortfolioGroup newGroup = PortfolioGroup.builder()
                .groupName(request.getGroupName())
                .owner(owner)
                .status("ACTIVE")
                .build();

        PortfolioGroup savedGroup = groupRepository.save(newGroup);
        return groupMapper.toGroupResponse(savedGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getGroupsByCurrentUser() {
        Integer currentUserId = securityUtils.getCurrentUserId();
        return groupRepository.findByOwner_Id(currentUserId).stream()
                .map(groupMapper::toGroupResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMemberFromGroup(Integer groupId, Integer customerId) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        findGroupAndVerifyOwner(groupId, currentUserId);
        memberRepository.deleteByPortfolioGroup_IdAndCustomer_Id(groupId, customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getActiveMembersByGroupId(Integer groupId) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        findGroupAndVerifyOwner(groupId, currentUserId);

        List<Customers> customers = memberRepository.findActiveMembersByGroupId(groupId);
        List<Integer> customerIds = customers.stream().map(Customers::getId).collect(Collectors.toList());

        List<CustomerAccount> accounts = customerAccountRepository.findByCustomerIdIn(customerIds);
        List<CustomerAsset> assets = customerAssetRepository.findByCustomerIdIn(customerIds); 

        Map<Integer, List<CustomerAccount>> accountsByCustomer = accounts.stream().collect(Collectors.groupingBy(ca -> ca.getCustomer().getId()));
        Map<Integer, List<CustomerAsset>> assetsByCustomer = assets.stream().collect(Collectors.groupingBy(CustomerAsset::getCustomerId)); 

        return customers.stream()
                .map(customer -> {
                    List<CustomerAccount> customerAccounts = accountsByCustomer.getOrDefault(customer.getId(), Collections.emptyList());
                    List<CustomerAsset> customerAssets = assetsByCustomer.getOrDefault(customer.getId(), Collections.emptyList());
                    Map<Integer, CustomerAsset> assetMap = customerAssets.stream().collect(Collectors.toMap(CustomerAsset::getAssetId, Function.identity(), (a,b)->a));

                    return groupMapper.toGroupMemberResponseWithAccountsAndAssets(customer, customerAccounts, assetMap);
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void addMembersToGroup(Integer groupId, AddMembersRequest request) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        PortfolioGroup group = findGroupAndVerifyOwner(groupId, currentUserId);


        List<Integer> customerIds = request.getCustomerIds();
        long existingCustomerCount = customerRepository.countByIdIn(customerIds);
        if (existingCustomerCount != customerIds.size()) {
            throw new UserNotFoundException("Listede var olmayan veya geçersiz müşteri ID'leri bulundu.");
        }


        List<PortfolioGroupMember> newMembers = customerIds.stream()
                .map(customerId -> {
                    Customers customerProxy = customerRepository.getReferenceById(customerId);
                    
                    return new PortfolioGroupMember(group, customerProxy);
                })
                .collect(Collectors.toList());

        memberRepository.saveAll(newMembers);
    }

    private PortfolioGroup findGroupAndVerifyOwner(Integer groupId, Integer ownerId) {
        PortfolioGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Grup bulunamadı: " + groupId));

        if (!group.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Bu grup üzerinde işlem yapma yetkiniz yok.");
        }
        return group;
    }
}
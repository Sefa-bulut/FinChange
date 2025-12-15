package com.example.finchange.portfoliogroup.mapper;

import com.example.finchange.customer.model.Customers;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.portfoliogroup.dto.GroupMemberResponse;
import com.example.finchange.portfoliogroup.dto.GroupResponse;
import com.example.finchange.portfoliogroup.model.PortfolioGroup;
import com.example.finchange.portfolio.model.CustomerAsset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring") 
public interface PortfolioGroupMapper {

    @Mapping(source = "owner.id", target = "ownerUserId")
    @Mapping(source = "createdAt", target = "createdAt")
    GroupResponse toGroupResponse(PortfolioGroup group);

    @Mapping(source = "id", target = "customerId")
    @Mapping(source = "customerCode", target = "customerCode")
    @Mapping(source = "customer", target = "fullName", qualifiedByName = "toFullName")
    @Mapping(target = "accounts", ignore = true)
    GroupMemberResponse toGroupMemberResponse(Customers customer);

    default GroupMemberResponse toGroupMemberResponseWithAccounts(Customers customer, List<CustomerAccount> accounts) {
        return GroupMemberResponse.builder()
                .customerId(customer.getId())
                .customerCode(customer.getCustomerCode())
                .fullName(toFullName(customer))
                .accounts(accounts.stream()
                        .map(this::toCustomerAccountInfo)
                        .collect(Collectors.toList()))
                .build();
    }

    default GroupMemberResponse.CustomerAccountInfo toCustomerAccountInfo(CustomerAccount account) {
        BigDecimal availableBalance = account.getBalance().subtract(account.getBlockedBalance());
        return GroupMemberResponse.CustomerAccountInfo.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .blockedBalance(account.getBlockedBalance())
                .availableBalance(availableBalance)
                .build();
    }

    default GroupMemberResponse toGroupMemberResponseWithAccountsAndAssets(Customers customer, List<CustomerAccount> accounts, Map<Integer, CustomerAsset> assetMap) {
        List<GroupMemberResponse.CustomerAssetInfo> assetInfos = assetMap.values().stream()
            .map(asset -> GroupMemberResponse.CustomerAssetInfo.builder()
                .bistCode(asset.getAsset().getBistCode())
                .totalLot(asset.getTotalLot())
                .blockedLot(asset.getBlockedLot())
                .availableLots(asset.getAvailableLots())
                .build())
            .collect(Collectors.toList());

        return GroupMemberResponse.builder()
                .customerId(customer.getId())
                .customerCode(customer.getCustomerCode())
                .fullName(toFullName(customer))
                .accounts(accounts.stream()
                        .map(account -> toCustomerAccountInfoWithAssets(account, assetInfos))
                        .collect(Collectors.toList()))
                .build();
    }

    default GroupMemberResponse.CustomerAccountInfo toCustomerAccountInfoWithAssets(CustomerAccount account, List<GroupMemberResponse.CustomerAssetInfo> assets) {
        BigDecimal availableBalance = account.getBalance().subtract(account.getBlockedBalance());
        return GroupMemberResponse.CustomerAccountInfo.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .blockedBalance(account.getBlockedBalance())
                .availableBalance(availableBalance)
                .assets(assets)
                .build();
    }

    @Named("toFullName")
    default String toFullName(Customers customer) {
        if (customer == null) {
            return null;
        }
        return "GERCEK".equals(customer.getCustomerType().name()) 
            ? customer.getName() + " " + customer.getLastName()
            : customer.getCompanyTitle();
    }
}
package com.example.finchange.portfoliogroup.mapper;

import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.customer.model.Customers;
import com.example.finchange.portfolio.model.Asset;
import com.example.finchange.portfolio.model.CustomerAsset;
import com.example.finchange.portfoliogroup.dto.GroupMemberResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PortfolioGroupMapper default method tests")
class PortfolioGroupMapperTest {

    PortfolioGroupMapper mapper = new PortfolioGroupMapper() {
        @Override public com.example.finchange.portfoliogroup.dto.GroupResponse toGroupResponse(com.example.finchange.portfoliogroup.model.PortfolioGroup group) { return null; }
        @Override public GroupMemberResponse toGroupMemberResponse(Customers customer) { return null; }
        @Override public GroupMemberResponse toGroupMemberResponseWithAccounts(Customers customer, java.util.List<CustomerAccount> accounts) { return PortfolioGroupMapper.super.toGroupMemberResponseWithAccounts(customer, accounts); }
        @Override public GroupMemberResponse.CustomerAccountInfo toCustomerAccountInfo(CustomerAccount account) { return PortfolioGroupMapper.super.toCustomerAccountInfo(account); }
        @Override public GroupMemberResponse toGroupMemberResponseWithAccountsAndAssets(Customers customer, java.util.List<CustomerAccount> accounts, java.util.Map<Integer, CustomerAsset> assetMap) { return PortfolioGroupMapper.super.toGroupMemberResponseWithAccountsAndAssets(customer, accounts, assetMap); }
        @Override public GroupMemberResponse.CustomerAccountInfo toCustomerAccountInfoWithAssets(CustomerAccount account, java.util.List<GroupMemberResponse.CustomerAssetInfo> assets) { return PortfolioGroupMapper.super.toCustomerAccountInfoWithAssets(account, assets); }
        @Override public String toFullName(Customers customer) { return PortfolioGroupMapper.super.toFullName(customer); }
    };

    @Test
    @DisplayName("toCustomerAccountInfo: availableBalance = balance - blockedBalance")
    void toCustomerAccountInfo_availableBalance() {
        CustomerAccount acc = new CustomerAccount();
        acc.setId(1);
        acc.setAccountNumber("ACC-1");
        acc.setAccountName("Main");
        acc.setCurrency("TRY");
        acc.setBalance(new BigDecimal("1000.00"));
        acc.setBlockedBalance(new BigDecimal("125.25"));

        GroupMemberResponse.CustomerAccountInfo info = mapper.toCustomerAccountInfo(acc);
        assertThat(info.getAvailableBalance()).isEqualByComparingTo("874.75");
        assertThat(info.getAccountNumber()).isEqualTo("ACC-1");
        assertThat(info.getCurrency()).isEqualTo("TRY");
    }

    @Test
    @DisplayName("toGroupMemberResponseWithAccountsAndAssets: assets mapped with bistCode and lots")
    void toGroupMemberResponseWithAccountsAndAssets_mapsAssets() {
        Customers cust = new Customers();
        cust.setId(100);
        cust.setCustomerCode("BRYSL-100");
        cust.setName("Ada");
        cust.setLastName("Kaya");
        cust.setCustomerType(com.example.finchange.customer.model.CustomerType.GERCEK);

        CustomerAccount acc = new CustomerAccount();
        acc.setId(1); acc.setAccountNumber("A1"); acc.setAccountName("Spot"); acc.setCurrency("TRY");
        acc.setBalance(new BigDecimal("500")); acc.setBlockedBalance(new BigDecimal("0"));

        Asset asset = new Asset();
        asset.setId(10); asset.setBistCode("AKBNK");

        CustomerAsset ca = new CustomerAsset();
        ca.setCustomerId(100); ca.setAssetId(10); ca.setTotalLot(15); ca.setBlockedLot(5);
        ca.setAsset(asset);

        Map<Integer, CustomerAsset> assetMap = Map.of(10, ca);

        GroupMemberResponse out = mapper.toGroupMemberResponseWithAccountsAndAssets(cust, List.of(acc), assetMap);
        assertThat(out.getCustomerId()).isEqualTo(100);
        assertThat(out.getAccounts()).hasSize(1);
        GroupMemberResponse.CustomerAccountInfo accInfo = out.getAccounts().get(0);
        assertThat(accInfo.getAssets()).hasSize(1);
        GroupMemberResponse.CustomerAssetInfo assetInfo = accInfo.getAssets().get(0);
        assertThat(assetInfo.getBistCode()).isEqualTo("AKBNK");
        assertThat(assetInfo.getTotalLot()).isEqualTo(15);
        assertThat(assetInfo.getBlockedLot()).isEqualTo(5);
        assertThat(assetInfo.getAvailableLots()).isEqualTo(10);
    }
}

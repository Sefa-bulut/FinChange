package com.example.finchange.portfoliogroup.service;

import com.example.finchange.common.util.SecurityUtils;
import com.example.finchange.customer.model.CustomerAccount;
import com.example.finchange.customer.model.Customers;
import com.example.finchange.customer.repository.CustomerAccountRepository;
import com.example.finchange.customer.repository.CustomerRepository;
import com.example.finchange.portfolio.model.CustomerAsset;
import com.example.finchange.portfolio.repository.CustomerAssetRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioGroupServiceImpl tests")
class PortfolioGroupServiceImplTest {

    @Mock PortfolioGroupRepository groupRepository;
    @Mock PortfolioGroupMemberRepository memberRepository;
    @Mock UserRepository userRepository;
    @Mock CustomerRepository customerRepository;
    @Mock CustomerAccountRepository customerAccountRepository;
    @Mock CustomerAssetRepository customerAssetRepository;
    @Mock SecurityUtils securityUtils;
    @Mock PortfolioGroupMapper groupMapper;

    @InjectMocks
    com.example.finchange.portfoliogroup.service.impl.PortfolioGroupServiceImpl service;

    @BeforeEach
    void setup() {
        when(securityUtils.getCurrentUserId()).thenReturn(10);
    }

    @Test
    @DisplayName("createGroup: başarılı akış, aynı isimde yok, kaydedilir ve map edilir")
    void createGroup_success() {
        CreateGroupRequest req = new CreateGroupRequest();
        req.setGroupName("VIP-1");

        User owner = new User();
        owner.setId(10);
        when(userRepository.findById(10)).thenReturn(Optional.of(owner));
        when(groupRepository.findByGroupNameAndOwner_Id("VIP-1", 10)).thenReturn(Optional.empty());

        PortfolioGroup saved = PortfolioGroup.builder().id(77).groupName("VIP-1").owner(owner).status("ACTIVE").build();
        when(groupRepository.save(any(PortfolioGroup.class))).thenReturn(saved);

        GroupResponse mapped = mock(GroupResponse.class);
        when(groupMapper.toGroupResponse(saved)).thenReturn(mapped);

        GroupResponse res = service.createGroup(req);
        assertThat(res).isSameAs(mapped);

        ArgumentCaptor<PortfolioGroup> pg = ArgumentCaptor.forClass(PortfolioGroup.class);
        verify(groupRepository).save(pg.capture());
        assertThat(pg.getValue().getGroupName()).isEqualTo("VIP-1");
        assertThat(pg.getValue().getOwner()).isSameAs(owner);
    }

    @Test
    @DisplayName("createGroup: aynı isimde grup varsa IllegalStateException")
    void createGroup_duplicate_throws() {
        CreateGroupRequest req = new CreateGroupRequest();
        req.setGroupName("VIP-1");
        when(userRepository.findById(10)).thenReturn(Optional.of(new User()));
        when(groupRepository.findByGroupNameAndOwner_Id("VIP-1", 10)).thenReturn(Optional.of(new PortfolioGroup()));

        assertThatThrownBy(() -> service.createGroup(req))
                .isInstanceOf(IllegalStateException.class);
        verify(groupRepository, never()).save(any());
    }

    @Test
    @DisplayName("addMembersToGroup: geçersiz müşteri ID'leri sayımı tutmazsa UserNotFoundException")
    void addMembers_invalidIds_throws() {
        User owner = new User(); owner.setId(10);
        PortfolioGroup group = PortfolioGroup.builder().id(5).owner(owner).groupName("G").status("ACTIVE").build();
        when(groupRepository.findById(5)).thenReturn(Optional.of(group));

        AddMembersRequest req = new AddMembersRequest();
        req.setCustomerIds(Arrays.asList(101, 102, 103));

        when(customerRepository.countByIdIn(req.getCustomerIds())).thenReturn(2L); // 3 beklenirdi

        assertThatThrownBy(() -> service.addMembersToGroup(5, req))
                .isInstanceOf(UserNotFoundException.class);
        verify(memberRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("removeMemberFromGroup: owner doğrulandıktan sonra delete çağrılır")
    void removeMember_ownerVerified_deletes() {
        User owner = new User(); owner.setId(10);
        PortfolioGroup group = PortfolioGroup.builder().id(5).owner(owner).groupName("G").status("ACTIVE").build();
        when(groupRepository.findById(5)).thenReturn(Optional.of(group));

        service.removeMemberFromGroup(5, 123);

        verify(memberRepository, times(1)).
                deleteByPortfolioGroup_IdAndCustomer_Id(eq(5), eq(123));
    }

    @Test
    @DisplayName("getActiveMembersByGroupId: owner doğrulanır, üyeler hesap/varlıkla birlikte map edilir")
    void getActiveMembers_mapsAccountsAndAssets() {
        User owner = new User(); owner.setId(10);
        PortfolioGroup group = PortfolioGroup.builder().id(9).owner(owner).groupName("G").status("ACTIVE").build();
        when(groupRepository.findById(9)).thenReturn(Optional.of(group));

        Customers c1 = new Customers(); c1.setId(100);
        Customers c2 = new Customers(); c2.setId(200);
        when(memberRepository.findActiveMembersByGroupId(9)).thenReturn(Arrays.asList(c1, c2));

        CustomerAccount a1 = new CustomerAccount(); a1.setId(1); a1.setCustomer(c1);
        CustomerAccount a2 = new CustomerAccount(); a2.setId(2); a2.setCustomer(c2);
        when(customerAccountRepository.findByCustomerIdIn(Arrays.asList(100,200)))
                .thenReturn(Arrays.asList(a1, a2));

        CustomerAsset ca1 = new CustomerAsset(); ca1.setCustomerId(100); ca1.setAssetId(50);
        CustomerAsset ca2 = new CustomerAsset(); ca2.setCustomerId(200); ca2.setAssetId(60);
        when(customerAssetRepository.findByCustomerIdIn(Arrays.asList(100,200)))
                .thenReturn(Arrays.asList(ca1, ca2));

        when(groupMapper.toGroupMemberResponseWithAccountsAndAssets(any(Customers.class), anyList(), anyMap()))
                .thenAnswer(inv -> mock(GroupMemberResponse.class));

        List<GroupMemberResponse> out = service.getActiveMembersByGroupId(9);
        assertThat(out).hasSize(2);

        verify(groupMapper, times(2))
                .toGroupMemberResponseWithAccountsAndAssets(any(Customers.class), anyList(), anyMap());
    }

    @Test
    @DisplayName("getActiveMembersByGroupId: owner değilse AccessDeniedException")
    void getActiveMembers_accessDenied() {
        User owner = new User(); owner.setId(99);
        PortfolioGroup group = PortfolioGroup.builder().id(9).owner(owner).groupName("G").status("ACTIVE").build();
        when(groupRepository.findById(9)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> service.getActiveMembersByGroupId(9))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}

package com.example.finchange.portfoliogroup.service;

import com.example.finchange.portfoliogroup.dto.AddMembersRequest;
import com.example.finchange.portfoliogroup.dto.CreateGroupRequest;
import com.example.finchange.portfoliogroup.dto.GroupMemberResponse;
import com.example.finchange.portfoliogroup.dto.GroupResponse;

import java.util.List;

public interface PortfolioGroupService {
    GroupResponse createGroup(CreateGroupRequest request);
    List<GroupResponse> getGroupsByCurrentUser();
    void addMembersToGroup(Integer groupId, AddMembersRequest request);
    void removeMemberFromGroup(Integer groupId, Integer customerId);
    List<GroupMemberResponse> getActiveMembersByGroupId(Integer groupId);
}
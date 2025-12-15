package com.example.finchange.portfoliogroup.controller;

import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.portfoliogroup.dto.AddMembersRequest;
import com.example.finchange.portfoliogroup.dto.CreateGroupRequest;
import com.example.finchange.portfoliogroup.dto.GroupMemberResponse;
import com.example.finchange.portfoliogroup.dto.GroupResponse;
import com.example.finchange.portfoliogroup.service.PortfolioGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio-groups")
@RequiredArgsConstructor
public class PortfolioGroupController {

    private final PortfolioGroupService portfolioGroupService;

    @PostMapping
    @PreAuthorize("hasAuthority('client:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessResponse<GroupResponse> createGroup(@RequestBody @Valid CreateGroupRequest request) {
        GroupResponse createdGroup = portfolioGroupService.createGroup(request);
        return SuccessResponse.success(createdGroup, "Grup başarıyla oluşturuldu.");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('client:read:all')")
    public SuccessResponse<List<GroupResponse>> getMyGroups() {
        List<GroupResponse> groups = portfolioGroupService.getGroupsByCurrentUser();
        return SuccessResponse.success(groups);
    }

    @PostMapping("/{groupId}/members")
    @PreAuthorize("hasAuthority('client:update:status')")
    public SuccessResponse<Void> addMembersToGroup(@PathVariable Integer groupId, @RequestBody @Valid AddMembersRequest request) {
        portfolioGroupService.addMembersToGroup(groupId, request);
        return SuccessResponse.success("Üyeler gruba başarıyla eklendi.");
    }

    @DeleteMapping("/{groupId}/members/{customerId}")
    @PreAuthorize("hasAuthority('client:update:status')")
    public SuccessResponse<Void> removeMemberFromGroup(@PathVariable Integer groupId, @PathVariable Integer customerId) {
        portfolioGroupService.removeMemberFromGroup(groupId, customerId);
        return SuccessResponse.success("Üye gruptan başarıyla çıkarıldı.");
    }

    @GetMapping("/{groupId}/members")
    @PreAuthorize("hasAuthority('client:read:all')")
    public SuccessResponse<List<GroupMemberResponse>> getActiveMembers(@PathVariable Integer groupId) {
        List<GroupMemberResponse> members = portfolioGroupService.getActiveMembersByGroupId(groupId);
        return SuccessResponse.success(members);
    }
}
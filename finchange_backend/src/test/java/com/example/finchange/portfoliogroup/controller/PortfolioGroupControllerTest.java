package com.example.finchange.portfoliogroup.controller;

import com.example.finchange.portfoliogroup.dto.AddMembersRequest;
import com.example.finchange.portfoliogroup.dto.CreateGroupRequest;
import com.example.finchange.portfoliogroup.dto.GroupMemberResponse;
import com.example.finchange.portfoliogroup.dto.GroupResponse;
import com.example.finchange.portfoliogroup.service.PortfolioGroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PortfolioGroupController.class)
@AutoConfigureMockMvc(addFilters = false)
class PortfolioGroupControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean PortfolioGroupService portfolioGroupService;

    @Test
    @WithMockUser(authorities = "client:create")
    @DisplayName("POST /api/portfolio-groups -> 201 Created ve SuccessResponse dönmeli")
    void createGroup_created() throws Exception {
        CreateGroupRequest req = new CreateGroupRequest();
        req.setGroupName("VIP-1");

        GroupResponse resp = GroupResponse.builder()
                .id(1)
                .groupName("VIP-1")
                .status("ACTIVE")
                .ownerUserId(10)
                .build();

        when(portfolioGroupService.createGroup(any(CreateGroupRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/portfolio-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess", is(true)))
                .andExpect(jsonPath("$.result.groupName", is("VIP-1")))
                .andExpect(jsonPath("$.result.status", is("ACTIVE")));
    }

    @Test
    @WithMockUser(authorities = "client:read:all")
    @DisplayName("GET /api/portfolio-groups -> 200 OK ve SuccessResponse<List<GroupResponse>>")
    void getMyGroups_ok() throws Exception {
        GroupResponse g1 = GroupResponse.builder().id(1).groupName("G1").status("ACTIVE").ownerUserId(10).build();
        GroupResponse g2 = GroupResponse.builder().id(2).groupName("G2").status("ACTIVE").ownerUserId(10).build();
        when(portfolioGroupService.getGroupsByCurrentUser()).thenReturn(List.of(g1, g2));

        mockMvc.perform(get("/api/portfolio-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess", is(true)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].groupName", is("G1")))
                .andExpect(jsonPath("$.result[1].groupName", is("G2")));
    }

    @Test
    @WithMockUser(authorities = "client:update:status")
    @DisplayName("POST /api/portfolio-groups/{id}/members -> 200 OK ve SuccessResponse mesaj")
    void addMembers_ok() throws Exception {
        AddMembersRequest req = new AddMembersRequest();
        req.setCustomerIds(List.of(100, 200));

        doNothing().when(portfolioGroupService).addMembersToGroup(eq(5), any(AddMembersRequest.class));

        mockMvc.perform(post("/api/portfolio-groups/{groupId}/members", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess", is(true)))
                .andExpect(jsonPath("$.message", not(emptyOrNullString())));
    }

    @Test
    @WithMockUser(authorities = "client:update:status")
    @DisplayName("DELETE /api/portfolio-groups/{id}/members/{customerId} -> 200 OK ve SuccessResponse mesaj")
    void removeMember_ok() throws Exception {
        doNothing().when(portfolioGroupService).removeMemberFromGroup(7, 300);

        mockMvc.perform(delete("/api/portfolio-groups/{groupId}/members/{customerId}", 7, 300))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess", is(true)))
                .andExpect(jsonPath("$.message", not(emptyOrNullString())));
    }

    @Test
    @WithMockUser(authorities = "client:read:all")
    @DisplayName("GET /api/portfolio-groups/{id}/members -> 200 OK ve SuccessResponse<List<GroupMemberResponse>>")
    void getActiveMembers_ok() throws Exception {
        GroupMemberResponse m1 = GroupMemberResponse.builder().customerId(100).customerCode("C100").fullName("A B").build();
        GroupMemberResponse m2 = GroupMemberResponse.builder().customerId(200).customerCode("C200").fullName("C D").build();
        when(portfolioGroupService.getActiveMembersByGroupId(9)).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/portfolio-groups/{groupId}/members", 9))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess", is(true)))
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[0].customerId", is(100)))
                .andExpect(jsonPath("$.result[1].customerId", is(200)));
    }
}

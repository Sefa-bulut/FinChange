package com.example.finchange.portfoliogroup.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class GroupResponse {
    private Integer id;
    private String groupName;
    private String status;
    private Integer ownerUserId;
    private LocalDateTime createdAt;
}
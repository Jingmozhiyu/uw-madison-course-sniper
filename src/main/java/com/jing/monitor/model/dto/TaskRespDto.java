package com.jing.monitor.model.dto;

import com.jing.monitor.model.StatusMapping;
import lombok.Data;

@Data
public class TaskRespDto {
    private Long id; // 🔥 Added for frontend operations
    private String sectionId;
    private String courseDisplayName;
    private StatusMapping status;
    private boolean enabled;
}